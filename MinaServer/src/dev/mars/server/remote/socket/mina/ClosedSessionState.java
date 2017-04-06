package dev.mars.server.remote.socket.mina;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

import java.net.InetSocketAddress;

import dev.mars.server.bean.SocketMessage;
import dev.mars.server.remote.socket.SendListener;

import static dev.mars.server.remote.socket.SocketClient.SessionStatus.CONNECTED;
import static dev.mars.server.remote.socket.SocketClient.SessionStatus.CONNECTING;

/**
 * Created by ma.xuanwei on 2017/1/4.
 */

public class ClosedSessionState extends SessionState {

    ClosedSessionState(MinaSocketClient client) {
        super(client);
    }

    /**
     * 关闭连接
     */
    @Override
    public IoFuture closeConnection() {
        //已经在关闭状态，什么都不做
        return null;
    }

    /**
     * 请求连接
     */
    @Override
    public ConnectFuture connect() {
        minaSocketClient.setSessionState(minaSocketClient.sessionStateFactory.newState(CONNECTING));
        minaSocketClient.connectFuture =(minaSocketClient.connector.connect(new InetSocketAddress(minaSocketClient.getIP(), minaSocketClient.getPort())));
        minaSocketClient.connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
            @Override
            public void operationComplete(ConnectFuture ioFuture) {
                if (!ioFuture.isConnected() || ioFuture.isCanceled()) {
                    minaSocketClient.session = null;
                    minaSocketClient.setSessionState(new ClosedSessionState(minaSocketClient));
                    if (minaSocketClient.mMinaSocketConnectionListener != null) {
                        minaSocketClient.mMinaSocketConnectionListener.onConnectionFailed(ioFuture.getSession().getId()+" "+ioFuture.getException().getMessage());
                    }
                } else {
                    minaSocketClient.setSessionState(minaSocketClient.sessionStateFactory.newState(CONNECTED));
                    minaSocketClient.session = ioFuture.getSession();
                }
            }
        });
        return minaSocketClient.getConnectFuture();
    }

    /**
     * 发送消息
     *
     * @param msg
     * @param listener
     * @param tryConnect 是否在无连接状态下请求连接
     */
    @Override
    public void send(final SocketMessage msg, final SendListener listener, final boolean tryConnect) {
        if (tryConnect) {
            ConnectFuture future = connect();
            future.addListener(new IoFutureListener<ConnectFuture>() {
                @Override
                public void operationComplete(ConnectFuture ioFuture) {
                    if (minaSocketClient.getStatus() == CONNECTED){
//                    	try {
//							Thread.sleep(3000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
                        minaSocketClient.getSessionState().send(msg, listener, tryConnect);
                    }else {
                        if (listener != null) {
                            listener.onSendFailed("发送失败");
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onSendFailed("发送失败，网络异常。");
            }
        }
    }
}
