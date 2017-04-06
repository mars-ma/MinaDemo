package dev.mars.server.remote.socket.mina;

import dev.mars.server.utils.ISecretUtil;
import dev.mars.server.utils.SecretUtilImp2;

/**
 * Created by ma.xuanwei on 2016/12/30.
 */

public class ConcretSecret implements ISecret {

    ISecretUtil secretUtil = new SecretUtilImp2();

    /**
     * 消息内容加密算法
     *
     * @param key
     * @param content
     * @return
     */
    @Override
    public String encrypt( String content) {
        try {
            return secretUtil.encrypt(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 消息内容解密算法
     *
     * @param key
     * @param encryptedContent
     * @return
     */
    @Override
    public String decrypt( String encryptedContent) {
        try {
            return secretUtil.decrypt(encryptedContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
