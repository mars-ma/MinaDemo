package dev.mars.server.bean;

import java.io.Serializable;

/**
 * Socket通信上层消息的基类
 * |0x5c|0x74|bodylength|body|
 * @author ma.xuanwei
 *
 */
public class SocketMessage implements Serializable{



	public static final byte HEADER1 = 0x5c;
	public static final byte HEADER2 = 0x74;

	private String body;


	public String getBody() {
		return body;
	}

	/**
	 * 设置消息体，一般用json解析
	 */
	public void setBody(String body) {
		this.body = body;
	}

}
