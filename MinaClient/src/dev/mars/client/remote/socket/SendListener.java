package dev.mars.client.remote.socket;

/**
 * Created by ma.xuanwei on 2016/12/13.
 */

public interface SendListener {
    /**
     * 发送消息成功回调
     */
    public void onSendSuccess();

    /**
     * 发送消息失败
     * @param str
     */
    public void onSendFailed(String str);
}
