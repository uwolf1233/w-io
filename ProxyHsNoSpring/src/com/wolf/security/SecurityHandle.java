package com.wolf.security;

import java.util.concurrent.TimeUnit;

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
				SessionLine.SessionLineMap.get(id).sessionDataQueue.offer(sessionData, 2, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}

	
	
}
