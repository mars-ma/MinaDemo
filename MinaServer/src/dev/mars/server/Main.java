package dev.mars.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import dev.mars.server.bean.SocketMessage;
import dev.mars.server.remote.socket.mina.BaseCodecFactory;
import dev.mars.server.remote.socket.mina.ConcretSecret;
import dev.mars.server.remote.socket.mina.ServerKeepAliveFilter;
import dev.mars.server.utils.LogUtils;

public class Main {
	public static ExecutorService service = Executors.newCachedThreadPool();
	public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private static AtomicInteger aliveSessions = new AtomicInteger();
	private static Vector<IoSession> sessions = new Vector<>();
	private static AtomicInteger totalReceived = new AtomicInteger(0);
	
	private static final int PORT = 8889;

	public static void main(String[] args) {
		LogUtils.DT("cpus:"+Runtime.getRuntime().availableProcessors());
		final IoAcceptor acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors());
		acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
				Constants.READ_IDLE_TIMEOUT);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE,
				Constants.WRITE_IDLE_TIMEOUT);
		acceptor.getFilterChain().addLast("threadpool", new ExecutorFilter(Executors.newCachedThreadPool()));
		acceptor.getFilterChain().addLast(
				"BaseFilter",
				new ProtocolCodecFilter(new BaseCodecFactory(Charset
						.forName("UTF-8"), new ConcretSecret())));
		//acceptor.getFilterChain().addLast("KeepAlive", new ServerKeepAliveFilter());
		acceptor.getSessionConfig().setReadBufferSize(2*1024*1024);
		acceptor.setHandler(new IoHandlerAdapter() {
			@Override
			public void sessionOpened(IoSession session) throws Exception {
				super.sessionOpened(session);
				LogUtils.DT("新增Session 当前存活" + aliveSessions.addAndGet(1));
				sessions.add(session);
			}

			@Override
			public void sessionClosed(IoSession session) throws Exception {
				super.sessionClosed(session);
				LogUtils.DT("关闭Session 当前存活" + aliveSessions.decrementAndGet());
				sessions.remove(session);
			}

			@Override
			public void messageReceived(IoSession session, Object message)
					throws Exception {
				super.messageReceived(session, message);
				LogUtils.DT("总计收到:"+totalReceived.incrementAndGet());
				SocketMessage s = new SocketMessage();
				s.setBody("server");
				session.write(s);
			}

			@Override
			public void exceptionCaught(IoSession session, Throwable cause)
					throws Exception {
				super.exceptionCaught(session, cause);
				if (session != null) {
					session.closeOnFlush();
					sessions.remove(session);
				}
				LogUtils.DT("Session异常并关闭");
			}
		});

		service.execute(new Runnable() {
			@Override
			public void run() {
				try {
					acceptor.bind(new InetSocketAddress(PORT));
					LogUtils.DT("服务端已绑定端口:" + PORT);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

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

				acceptor.unbind();
				LogUtils.DT("Server 停止监听");
				break;
			} else if ("show".equals(str)) {
				showAliveSessions();
			} else if("clear".equals(str)){
				totalReceived.set(0);
				LogUtils.DT("收到消息计数器已重置");
			}
		}
	}

	private static void showAliveSessions() {
		LogUtils.DT("当前存活 : " + aliveSessions.get());
		Iterator<IoSession> iterator = sessions.iterator();
		while (iterator.hasNext()) {
			IoSession session = iterator.next();
			if (!session.isConnected()) {
				sessions.remove(session);
				LogUtils.DT("清理无效session:"+session.getId());
			}
		}
		LogUtils.DT("清理后存活 : " + aliveSessions.get());
		
		LogUtils.DT("总计收到消息:"+totalReceived.get());
	}
}
