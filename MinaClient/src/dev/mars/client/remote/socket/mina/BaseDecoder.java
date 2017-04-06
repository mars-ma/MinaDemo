package dev.mars.client.remote.socket.mina;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import dev.mars.client.bean.SocketMessage;
import dev.mars.client.utils.BasicTypeConvertUtils;


public class BaseDecoder extends CumulativeProtocolDecoder{

	Charset charset;
	ISecret secret;


	public BaseDecoder(Charset charset, ISecret secret) {
		this.charset = charset;
		this.secret = secret;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean doDecode(IoSession session,IoBuffer in,
							   ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		if(in.remaining()>=4){
//			System.out.println("1.缓冲区目前数组长度:"+in.remaining());
			//当可读的缓冲区长度大于4时（前两个字节是占位符，后两个字节是长度）
			in.mark(); // 标记当前位置，方便reset
			byte[] header = new byte[4];
			in.get(header, 0, header.length);
//			System.out.println("receive header[0]:"+header[0]+"|header[1]:"+header[1]);
			if(header[0]== SocketMessage.HEADER1 && header[1]== SocketMessage.HEADER2){
//				System.out.println("header[2]:"+header[2]+",header[3]:"+header[3]);
				short bodyLength = BasicTypeConvertUtils.byteToShort(header[2],header[3]);
//				System.out.println("报文内容长度:"+bodyLength);
//				System.out.println("2.缓冲区目前数组长度:"+in.remaining());

				if(in.remaining()>=bodyLength){
					//可读取完整的报文
//					System.out.println(in.remaining()>=bodyLength);
					if(bodyLength==0){
						//心跳包处理
						SocketMessage msg = new SocketMessage();
						msg.setBody(null);
						out.write(msg);
					}else {
						byte[] body = new byte[bodyLength];
						in.get(body, 0, bodyLength);
						String bodyContent = new String(body, charset);
						if (secret != null) {
							//注意这里传入的是UTF-8编码的bodyContent字符串,是由服务器加密处理后传来的
							bodyContent = secret.decrypt(bodyContent);
							//这里得到的是UTF-8编码的原文字符串

						}
//						System.out.println("报文内容:" + bodyContent);
						SocketMessage msg = new SocketMessage();
						msg.setBody(bodyContent);
						out.write(msg);
//						System.out.println("3.缓冲区目前数组长度:" + in.remaining());
					}
					if(in.remaining()>0){
//						System.out.println("粘包，保留未消费数据");
						return true;
					}
//					System.out.println("不粘包，IoBuffer中的数据已消费");
				}else{
//					System.out.println("缓冲区未接收完全应用层报文，继续读取字节流");
					in.reset();
					return false;
				}

			}

		}
		return false;
	}


}
