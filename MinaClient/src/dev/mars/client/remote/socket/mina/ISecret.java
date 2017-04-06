package dev.mars.client.remote.socket.mina;

/**
 * Created by ma.xuanwei on 2016/12/30.
 */

public interface ISecret {
    /**
     * 消息内容加密算法
     * @param key
     * @param content
     * @return
     */
    String encrypt(String content);

    /**
     * 消息内容解密算法
     * @param key
     * @param encryptedContent
     * @return
     */
    String decrypt(String encryptedContent);
}
