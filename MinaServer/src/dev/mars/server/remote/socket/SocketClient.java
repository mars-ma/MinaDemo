package dev.mars.server.remote.socket;


import dev.mars.server.bean.SocketMessage;

/**
 * Created by ma.xuanwei on 2016/12/13.
 */

public abstract class SocketClient<T>{
    protected String destIP;
    protected int destPort;
    protected SocketConnectionListener socketConnectionListener;

    public SocketClient(){
    }

    protected static final String CONNECTION_OFFLINE = "网络未连接";

    /**
     * 设置连接监听器
     * @param listener
     */
    public void setSocketConnectionListener(SocketConnectionListener listener){
        this.socketConnectionListener = listener;
    }

    public SocketConnectionListener getSocketConnectionListener(){
        return socketConnectionListener;
    }

    /**
     * 设置IP
     * @param str
     */
    public void setIP(String str){
        this.destIP = str;
    }

    /**
     * 设置端口号
     * @param port
     */
    public void setPort(int port){
        destPort = port;
    }

    /**
     * 获取连接状态
     * @return
     */
    public abstract SessionStatus getStatus();

    /**
     * 连接
     */
    public abstract T connect();

    /**
     * 发送
     * @param msg
     */
    public abstract void send(SocketMessage msg, SendListener listener,boolean tryConnect);

    /**
     * 关闭连接
     */
    public abstract T closeConnection();


    /**
     * Created by ma.xuanwei on 2016/12/13.
     */
    public enum SessionStatus {
        ClOSED,
        CONNECTED,
        CONNECTING
    }

    public String getIP(){
        return destIP;
    }

    public int getPort(){
        return destPort;
    }

    public interface IServerMessageHandler{
        void handleMessage(SocketMessage message);
    }

    private IServerMessageHandler serverMessageHandler;

    public void setServerMessageHandler(IServerMessageHandler handler){
        this.serverMessageHandler = handler;
    }

    public IServerMessageHandler getServerMessageHandler(){
        return serverMessageHandler;
    }
}
