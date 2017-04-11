package dev.mars.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.AbstractIoSession;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import dev.mars.client.bean.SocketMessage;
import dev.mars.client.remote.socket.mina.BaseCodecFactory;
import dev.mars.client.remote.socket.mina.ConcretSecret;
import dev.mars.client.utils.LogUtils;

public class Main {
	public static ExecutorService service = Executors.newCachedThreadPool();
	public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private static AtomicInteger aliveSessions = new AtomicInteger();
	private static Vector<IoSession> sessions = new Vector<>();

	private static volatile long SLEEP_TIME = 0;
	public static AtomicLong MAX_TIME = new AtomicLong(0);
	public static AtomicLong MIN_TIME = new AtomicLong(Long.MAX_VALUE);
	public static AtomicLong TOTAL_TIME = new AtomicLong(0);
	private static AtomicInteger totalReceived = new AtomicInteger();
	public static AtomicBoolean send = new AtomicBoolean(false);

	public static void main(String[] args) {
		System.out.println("你的电脑cpu数量为：" + Runtime.getRuntime().availableProcessors());
		NioSocketConnector connector = new NioSocketConnector(Runtime.getRuntime().availableProcessors() + 1);
		connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, Constants.READ_IDLE_TIMEOUT);
		connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE, Constants.WRITE_IDLE_TIMEOUT);
		connector.getSessionConfig().setWriteTimeout(2000);
		connector.getSessionConfig().setSendBufferSize(1024 * 1024);
		connector.getSessionConfig().setReadBufferSize(2 * 1024 * 1024);
		connector.getSessionConfig().setReceiveBufferSize(2 * 1024 * 1024);
		connector.getSessionConfig().setTcpNoDelay(true);
		LogUtils.DT("session 最大缓冲区:" + connector.getSessionConfig().getMaxReadBufferSize() + " b");
		LogUtils.DT("session 最小缓冲区:" + connector.getSessionConfig().getMinReadBufferSize() + " b");

		// LoggingFilter loggingFilter = new LoggingFilter();
		// connector.getFilterChain().addLast("logging", loggingFilter);
		connector.getFilterChain().addLast("threadpool", new ExecutorFilter(Executors.newCachedThreadPool()));
		connector.getFilterChain().addLast("BaseFilter",
				new ProtocolCodecFilter(new BaseCodecFactory(Charset.forName("UTF-8"), new ConcretSecret())));
		// connector.getFilterChain().addLast("KeepAlive", new
		// ClientKeepAliveFilter());
		// 设置连接超时检查时间
		connector.setConnectTimeoutCheckInterval(5000);
		connector.setConnectTimeoutMillis(10000); // 10秒后超时
		connector.setHandler(new IoHandlerAdapter() {
			@Override
			public void sessionClosed(IoSession session) throws Exception {
				super.sessionClosed(session);
				LogUtils.DT("session:" + session.getId() + " 已关闭");
				aliveSessions.decrementAndGet();
			}

			@Override
			public void sessionOpened(IoSession session) throws Exception {
				// TODO Auto-generated method stub
				super.sessionOpened(session);
				LogUtils.DT("session:" + session.getId() + " 已建立");
				aliveSessions.addAndGet(1);
				sessions.add(session);
			}

			@Override
			public void messageReceived(IoSession session, Object message) throws Exception {
				super.messageReceived(session, message);
				Long sendTime = (Long) session.getAttribute("sendtime");
				totalReceived.incrementAndGet();
				if (sendTime != null) {
					long sendTimeL = sendTime.longValue();
					long duration = System.currentTimeMillis()-sendTimeL;
					if(duration>MAX_TIME.get()){
						MAX_TIME.set(duration);
					}
					countDownLatch.countDown();
					LogUtils.DT("session:" + session.getId() + " receive use "
							+duration + " ms ");
				}
			}

			@Override
			public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
				super.exceptionCaught(session, cause);
			}
		});

		int times = Integer.parseInt(args[0]);
		int count[] = {0};
		long start =System.currentTimeMillis();
		addConnection(times, count,connector,start);

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
				for (IoSession session : sessions) {
					session.closeOnFlush();
				}
				LogUtils.DT("所有Sessions终止连接");
				break;
			} else if ("show".equals(str)) {
				showAliveSessions();
				LogUtils.DT("总计收到消息:" + totalReceived.get());
			} else if (str != null && str.startsWith("setsleep:")) {
				String num = str.substring("setsleep:".length());
				LogUtils.DT("设置新的发送间隔:" + num);
				SLEEP_TIME = Long.parseLong(num);
			} else if ("send".equals(str)) {
				if (send.get()) {
					send.set(false);
				} else {
					MAX_TIME.set(0);
					send.set(true);
					new Thread(){
						public void run() {
							clearDiedSessionsAndSend();
						};
					}.start();
					
				}
			} else if ("clear".equals(str)) {
				totalReceived.set(0);
			}
		}
	}
	static CountDownLatch  countDownLatch;
	private static void clearDiedSessionsAndSend() {
//		while (send.get()) {
			// TODO Auto-generated method stub
			Iterator<IoSession> iterator = sessions.iterator();
			LogUtils.DT("当前总计存活Sessions:" + sessions.size());
			final AtomicInteger sendSuccess = new AtomicInteger(0);
			countDownLatch = new CountDownLatch(sessions.size());
			AtomicInteger count = new AtomicInteger(0);
			while (iterator.hasNext()) {

				final IoSession session = iterator.next();
				if (session.isConnected()) {

					service.execute(new Runnable() {

						@Override
						public void run() {
							SocketMessage s = new SocketMessage();
							s.setBody("test");
							WriteFuture writeFuture = session.write(s);
							writeFuture.addListener(new IoFutureListener<WriteFuture>() {

								@Override
								public void operationComplete(WriteFuture arg0) {
									// TODO Auto-generated method stub
									session.setAttribute("sendtime", Long.valueOf(System.currentTimeMillis()));
									if (!arg0.isDone() || !arg0.isWritten()) {
										LogUtils.DT("session:" + session.getId() + " 发送失败");
									} else {
										sendSuccess.addAndGet(1);
									}
								}
							});
						}
					});

				} else {
					countDownLatch.countDown();
					sessions.remove(session);
					LogUtils.DT("清理无效session:" + session.getId());
				}

//				if (count.incrementAndGet() % 100 == 0) {
//					LogUtils.DT("已发送 " + count.get() + " 休眠 " + SLEEP_TIME + " MS");
//					try {
//						Thread.sleep(SLEEP_TIME);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}

			}
			LogUtils.DT("清理后还有Sessions:" + sessions.size());
			LogUtils.DT("正在发送");
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LogUtils.DT("发送成功:" + sendSuccess.get()+" 最大延迟:"+MAX_TIME.get());
//			try {
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	
	}

	private static void showAliveSessions() {
		LogUtils.DT("当前存活 : " + aliveSessions.get());
	}

	private static void addConnection(final int times, final int[] count, final NioSocketConnector connector, final long start) {
		ConnectFuture connectFuture = connector.connect(new InetSocketAddress(Constants.REMOTE_IP, Constants.PORT));
		connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
			@Override
			public void operationComplete(ConnectFuture ioFuture) {
				if (ioFuture.isDone()) {
					count[0]=count[0]+1;
					LogUtils.DT("已建立"+count[0]+"个session");
					if(count[0]<times){
						addConnection(times, count, connector,start);
					}else{
						LogUtils.DT("耗时:"+(System.currentTimeMillis()-start));
					}
				}
			}
		});
	}

}
