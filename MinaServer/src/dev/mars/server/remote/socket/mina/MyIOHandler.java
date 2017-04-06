package dev.mars.server.remote.socket.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import dev.mars.server.bean.SocketMessage;
import dev.mars.server.remote.socket.SocketClient;
import dev.mars.server.remote.socket.SocketConnectionListener;

/**
 * MinaSocket事件处理器
 */
public class MyIOHandler extends IoHandlerAdapter {

	private SocketConnectionListener socketConnectionListener;

	SocketClient.IServerMessageHandler handler;
	public MyIOHandler(SocketClient.IServerMessageHandler serverMessageHandler) {
		this.handler = serverMessageHandler;
	}

	public void setServerMessageHandler(SocketClient.IServerMessageHandler handler){
		this.handler = handler;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		super.exceptionCaught(session, cause);
		if(session!=null&&session.isConnected()){
			session.closeOnFlush();
		}
	}

	public MyIOHandler setSocketConnectionListener(SocketConnectionListener listener){
		this.socketConnectionListener = listener;
		return this;
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		try {
			SocketMessage socketMsg = (SocketMessage)message;
			System.out.println("Mina messageReceived : "+socketMsg.getBody());
			if(handler!=null){
				handler.handleMessage(socketMsg);
			}

		}catch (Exception ex){
		}
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		SocketMessage sm = (SocketMessage)message;
		System.out.println("Mina messageSent : "+sm.getBody());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		System.out.println(session.getId()+" sessionClosed");
		if(socketConnectionListener!=null){
			socketConnectionListener.onConnectionClosed();
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		super.sessionIdle(session, status);
//		System.out.println("sessionIdle " + status);
		if(socketConnectionListener!=null){
			socketConnectionListener.onIdle(status.toString());
		}
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionCreated(session);
//		System.out.println("sessionCreated");

	}

	/**
	 * 通信开始节点
	 * @param session
	 * @throws Exception
     */
	@Override
	public void sessionOpened(final IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionOpened(session);
//		System.out.println(session.getId()+" sessionOpened");
		if(socketConnectionListener!=null){
			socketConnectionListener.onConnected();
		}
	}
}
