package dev.mars.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.session.IoSession;

import dev.mars.client.bean.SocketMessage;
import dev.mars.client.remote.socket.SocketClient;
import dev.mars.client.remote.socket.SocketClient.IServerMessageHandler;
import dev.mars.client.remote.socket.SocketConnectionListener;
import dev.mars.client.remote.socket.mina.MinaSocketClient;
import dev.mars.client.utils.LogUtils;

public class Main {
	public static ExecutorService service = Executors.newCachedThreadPool();
	public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private static AtomicInteger aliveSessions = new AtomicInteger();
	private static Vector<SocketClient> clients = new Vector<>();

	private static final int PORT = 8888;

	public static AtomicLong MAX_TIME = new AtomicLong(0);
	public static AtomicLong MIN_TIME = new AtomicLong(Long.MAX_VALUE);
	public static AtomicLong TOTAL_TIME = new AtomicLong(0);

	public static void main(String[] args) {
		int onceConnections = 100;
		int times = 10;
		for (int i = 0; i < times; i++) {
			CountDownLatch countDownLatch = new CountDownLatch(onceConnections);
			for (int j = 0; j < onceConnections; j++) {
				addConnections(countDownLatch);
			}
			try {
				countDownLatch.await();
				LogUtils.DT("已创建" + (onceConnections * (i + 1)) + " 个客户端");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		service.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000 * 60 * 10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LogUtils.DT("当前存活 : " + aliveSessions.get());
			}
		});
		Scanner input = new Scanner(System.in);
		while (true) {
			String str = input.next();
			if ("exit".equals(str)) {
				for (SocketClient session : clients) {
					session.closeConnection();
				}
				LogUtils.DT("所有Clients终止连接");
				break;
			} else if ("show".equals(str)) {
				showAliveSessions();
			} else {

			}
		}
	}

	private static void showAliveSessions() {
		LogUtils.DT("当前存活 : " + aliveSessions.get());
	}

	private static void addConnections(final CountDownLatch countDownLatch) {
		final long start = System.currentTimeMillis();
		final SocketClient socketClient = new MinaSocketClient();
		socketClient.setIP(Constants.REMOTE_IP);
		socketClient.setPort(Constants.PORT);
		socketClient
				.setSocketConnectionListener(new SocketConnectionListener() {

					@Override
					public void onIdle(String string) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onConnectionFailed(String str) {
						// TODO Auto-generated method stub
						System.out.println("onConnectionFailed " + str);
						long time = System.currentTimeMillis() - start;
						if (time < MIN_TIME.get()) {
							MIN_TIME.set(time);
						}
						if (time > MAX_TIME.get()) {
							MAX_TIME.set(time);
						}
						TOTAL_TIME.addAndGet(time);
						countDownLatch.countDown();
					}

					@Override
					public void onConnectionClosed() {
						// TODO Auto-generated method stub
						aliveSessions.decrementAndGet();
						showAliveSessions();
					}

					@Override
					public void onConnected() {
						// TODO Auto-generated method stub
						long time = System.currentTimeMillis() - start;
						if (time < MIN_TIME.get()) {
							MIN_TIME.set(time);
						}
						if (time > MAX_TIME.get()) {
							MAX_TIME.set(time);
						}
						TOTAL_TIME.addAndGet(time);
						aliveSessions.addAndGet(1);
						clients.add(socketClient);
						showAliveSessions();
						countDownLatch.countDown();
					}
				});
		socketClient.connect();
	}
}
