package dev.mars.client.remote.socket.mina;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;

import dev.mars.client.bean.SocketMessage;
import dev.mars.client.remote.socket.SendListener;

/**
 * Created by ma.xuanwei on 2017/1/4.
 */

public class ConnectedSessionState extends SessionState {
    ConnectedSessionState(MinaSocketClient client) {
        super(client);
    }

    /**
     * 关闭连接
     */
    @Override
    public IoFuture closeConnection() {
        return minaSocketClient. session.closeOnFlush();
    }

    /**
     * 请求连接
     */
    @Override
    public IoFuture connect() {
        return null;
    }

    /**
     * 发送消息
     *
     * @param msg
     * @param listener
     * @param tryConnect 是否在无连接状态下请求连接
     */
    @Override
    public void send(SocketMessage msg, final SendListener listener, boolean tryConnect) {
        WriteFuture writeFuture = minaSocketClient.session.write(msg);
        writeFuture.addListener(new IoFutureListener<WriteFuture>() {
            @Override
            public void operationComplete(WriteFuture ioFuture) {
                if (listener != null) {
                    if (ioFuture.isWritten()) {
                        listener.onSendSuccess();
                    } else {
                        listener.onSendFailed("发送失败");
                    }
                }
            }
        });
    }
}
