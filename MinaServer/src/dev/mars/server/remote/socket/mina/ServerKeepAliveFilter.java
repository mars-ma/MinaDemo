package dev.mars.server.remote.socket.mina;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

import java.util.Date;

import dev.mars.server.Constants;
import dev.mars.server.bean.KeepAliveMessage;
import dev.mars.server.utils.LogUtils;

/**
 * 通过Mina框架实现TCP通讯
 * Created by ma.xuanwei on 2016/12/21.
 */

public class ServerKeepAliveFilter extends IoFilterAdapter{
    private KeepAliveMessage keepAliveMessage = new KeepAliveMessage();

    @Override
    public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if(keepAliveMessage.equals(writeRequest.getMessage())){
            //如果发送的消息是心跳包，拦截该事件
//        	LogUtils.DT(session.getId()+" 向服务器发送心跳包");
        }else {
            super.messageSent(nextFilter, session, writeRequest);
        }
    }

    @Override
    public void messageReceived(NextFilter nextFilter, final IoSession session, Object message) throws Exception {
        if(keepAliveMessage.equals(message)){
            //如果收到服务器返回的心跳包，拦截该事件
        	WriteFuture writeFuture =session.write(keepAliveMessage);
        	writeFuture.addListener(new IoFutureListener<WriteFuture>() {

				@Override
				public void operationComplete(WriteFuture arg0) {
					// TODO Auto-generated method stub
					if(arg0.isDone()&&!arg0.isWritten()){
						LogUtils.DT("session:"+session.getId()+" 回复心跳包失败");
					}
				}
			});
//        	LogUtils.DT(session.getId()+" 收到服务器心跳包");
        }else {
            super.messageReceived(nextFilter, session, message);
        }
    }

    @Override
    public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        if(status==IdleStatus.WRITER_IDLE){
            //session.write(keepAliveMessage);
        }else if(status==IdleStatus.READER_IDLE&&session.getIdleCount(IdleStatus.READER_IDLE)== Constants.READ_IDLE_CLOSE_TIMES){
            //READ_IDLE_CLOSE_TIMES次Read空闲就关闭session
            session.closeOnFlush();
            System.out.println("服务端 : "+session.getId()+" 未收到客户端心跳包，主动关闭");
        }else{
            nextFilter.sessionIdle(session,status);
        }
    }
}
