package dev.mars.server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;


/**
 * Created by ma.xuanwei on 2017/2/20.
 */

public class SecretUtilImp implements ISecretUtil{
    static {
        System.loadLibrary("libDes");
    }

    /**
     *
     * @param encryptedStr UTF-8格式的密文
     * @return UTF-8格式的明文
     */
    @Override
    public String decrypt(String encryptedStr){
        if(encryptedStr==null||encryptedStr.length()==0)
            return "";
        return desDecrypt(encryptedStr);
    }

    public native String desDecrypt(String source);

    /**
     * @param source UTF-8格式的字符串
     * @return UTF-8的字符串
     */
    @Override
    public String encrypt(String source){
        if(source==null||source.length()==0)
            return "";
        try {
            source = new String(source.getBytes(),"UTF-8");
            return desEncrypt(source);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public native  byte[] desDecryptBytes(byte[] bytes);

    public native String desEncrypt(String source);

    public native byte[] desEncryptBytes(byte[] bytes);
}
