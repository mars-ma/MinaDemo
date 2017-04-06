package dev.mars.server.remote.socket.mina;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.nio.charset.Charset;
import java.util.Date;

import dev.mars.server.Constants;
import dev.mars.server.bean.SocketMessage;
import dev.mars.server.remote.socket.SendListener;
import dev.mars.server.remote.socket.SocketClient;
import dev.mars.server.remote.socket.SocketConnectionListener;
import dev.mars.server.utils.TextUtils;

/**
 * MinaSocket Created by ma.xuanwei on 2016/12/13.
 */

public class MinaSocketClient extends SocketClient {
	NioSocketConnector connector;
	ConnectFuture connectFuture;
	IoSession session;
	SessionState sessionState;
	MinaSessionStateFactory sessionStateFactory = new MinaSessionStateFactory();
	MyIOHandler ioHandler;

	public MinaSocketClient() {
		super();
		init();
	}

	@Override
	public void setServerMessageHandler(IServerMessageHandler handler) {
		super.setServerMessageHandler(handler);
		ioHandler.setServerMessageHandler(handler);
	}

	/**
	 * 初始化
	 */
	private void init() {
		connector = new NioSocketConnector();
		connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
				Constants.READ_IDLE_TIMEOUT);
		connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE,
				Constants.WRITE_IDLE_TIMEOUT);
		connector.getFilterChain().addLast(
				"BaseFilter",
				new ProtocolCodecFilter(new BaseCodecFactory(Charset
						.forName("UTF-8"), new ConcretSecret())));
		connector.getFilterChain().addLast("KeepAlive", new ServerKeepAliveFilter());
		// 设置连接超时检查时间
		connector.setConnectTimeoutCheckInterval(5000);
		connector.setConnectTimeoutMillis(10000); // 10秒后超时
		ioHandler = new MyIOHandler(getServerMessageHandler());
		ioHandler.setSocketConnectionListener(mMinaSocketConnectionListener);
		connector.setHandler(ioHandler);
		setSessionState(sessionStateFactory.newState(SessionStatus.ClOSED));
	}

	protected SocketConnectionListener mMinaSocketConnectionListener = new SocketConnectionListener() {
		@Override
		public void onConnected() {
			setSessionState(sessionStateFactory
					.newState(SessionStatus.CONNECTED));
			if (socketConnectionListener != null) {
				socketConnectionListener.onConnected();
			}

		}

		@Override
		public void onConnectionClosed() {
			session = null;
			setSessionState(sessionStateFactory.newState(SessionStatus.ClOSED));
			if (socketConnectionListener != null)
				socketConnectionListener.onConnectionClosed();

		}

		@Override
		public void onConnectionFailed(final String str) {
			if (socketConnectionListener != null)
				socketConnectionListener.onConnectionFailed(str);
		}

		@Override
		public void onIdle(final String string) {
			if (socketConnectionListener != null)
				socketConnectionListener.onIdle(string);

		}
	};

	public void setSession(IoSession session) {
		this.session = session;
	}

	public IoSession getSession() {
		return session;
	}

	public void setSessionState(SessionState s) {
		sessionState = s;
	}

	public SessionState getSessionState() {
		return sessionState;
	}

	/**
	 * 获取连接状态
	 * 
	 * @return
	 */
	@Override
	public SessionStatus getStatus() {
		if (session == null || !session.isConnected()) {
			if (connectFuture != null && !connectFuture.isDone()
					&& !connectFuture.isCanceled()) {
				return SessionStatus.CONNECTING;
			} else {
				return SessionStatus.ClOSED;
			}
		} else {
			return SessionStatus.CONNECTED;
		}
	}

	/**
	 * 连接
	 */
	@Override
	public IoFuture connect() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					getSessionState().connect();
				}
			}).start();
		return null;
	}

	/**
	 * 发送
	 * 
	 * @param msg
	 */
	@Override
	public void send(final SocketMessage msg, final SendListener listener,
			final boolean tryConnect) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				getSessionState().send(msg, listener, tryConnect);
			}
		}).start();
	}

	/**
	 * 关闭连接
	 */
	@Override
	public IoFuture closeConnection() {
		return getSessionState().closeConnection();
	}

	public void setConnectFuture(ConnectFuture connectFuture) {
		this.connectFuture = connectFuture;
	}

	public ConnectFuture getConnectFuture() {
		return connectFuture;
	}

	protected class MinaSessionStateFactory {
		public SessionState newState(SessionStatus status) {
			switch (status) {
			case ClOSED:
				return new ClosedSessionState(MinaSocketClient.this);
			case CONNECTING:
				return new ConnectingSessionState(MinaSocketClient.this);
			case CONNECTED:
				return new ConnectedSessionState(MinaSocketClient.this);
			}
			return null;
		}
	}

}
