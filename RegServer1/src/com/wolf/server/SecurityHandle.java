package com.wolf.server;

import java.util.concurrent.BlockingQueue;

import com.wolf.javabean.Session.SessionData;
import com.wolf.javabean.SessionLine;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SecurityHandle extends ChannelInboundHandlerAdapter{

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(msg instanceof SessionData){
			SessionData sessionData = (SessionData)msg;
			String id = sessionData.getId();
			if(SessionLine.SessionLineMap.containsKey(id)){
//				Object o = SessionLine.SessionLineMap.get(id);
//				if(o != null && o instanceof SessionLine){
//					SessionLine sl = (SessionLine)o;
//					//sl.sessionDataQueue.put(sessionData);
//				}else if(o != null && o instanceof BlockingQueue){
//					BlockingQueue queue = (BlockingQueue)o;
//					//queue.put(sessionData);
//				}
				SessionLine.SessionLineMap.get(id).put(sessionData);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	
	
}
