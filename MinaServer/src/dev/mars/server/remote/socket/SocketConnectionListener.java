package dev.mars.server.remote.socket;

/**
 * Created by ma.xuanwei on 2016/12/6.
 */

public interface SocketConnectionListener {

    public void onConnected();
    public void onConnectionClosed();
    public void onConnectionFailed(String str);
    public void onIdle(String string);
}
