package dev.mars.server.remote.socket.mina;

import java.net.URLDecoder;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import dev.mars.server.Constants;
import dev.mars.server.bean.SocketMessage;
import dev.mars.server.utils.TextUtils;

public class BaseEncoder extends ProtocolEncoderAdapter{
	
	Charset charset;
	ISecret secret;
	
	public BaseEncoder(Charset charset,ISecret secret) {
		// TODO Auto-generated constructor stub
		this.charset = charset;
		this.secret  = secret;
	}
	

	@Override
	public void encode(IoSession session, Object obj, ProtocolEncoderOutput output)
			throws Exception {
		SocketMessage msg = (SocketMessage) obj;
		IoBuffer buffer = IoBuffer.allocate(100).setAutoExpand(true);
		buffer.order(ByteOrder.BIG_ENDIAN);
		//put head
		buffer.put(SocketMessage.HEADER1);
		buffer.put(SocketMessage.HEADER2);
		
		String body = msg.getBody();
		short bodyLength = 0;
		if(TextUtils.isEmpty(body)){
			//发送的是心跳包
			buffer.putShort(bodyLength);
		}else {
			if (secret != null) {
				//注意这里传入的是UTF-16的body字符串
//				System.out.println("加密前:"+body);
				body = secret.encrypt( body);
//				System.out.println("加密后:"+body);
			}
			byte[] bodyBytes = body.getBytes(charset);
			bodyLength = (short) bodyBytes.length;
			buffer.putShort(bodyLength);
			buffer.put(bodyBytes);
		}
//		System.out.println("HEADER:"+ SocketMessage.HEADER1+"|"+ SocketMessage.HEADER2);
//		System.out.println("Length:"+bodyLength);
//		System.out.println("will send :"+buffer.toString());
		buffer.flip(); 		
		output.write(buffer); 
		
	}



}
