package dev.mars.server.bean;

import org.json.JSONException;
import org.json.JSONObject;

import dev.mars.server.utils.TextUtils;

/**
 * 心跳包消息
 * body:{ "type":"0"}
 * @author ma.xuanwei
 *
 */
public class KeepAliveMessage extends SocketMessage {
    public KeepAliveMessage(){
        setBody("");
    }

    @Override
    public boolean equals(Object o) {
        try{
            SocketMessage other = (SocketMessage)o;
            if(TextUtils.isEmpty(other.getBody())) {
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}
