package com.wolf.server;

import com.wolf.javabean.TranNet.TransDatas;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class LineTransHandle extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// TODO Auto-generated method stub
		try{
			TransDatas tds = (TransDatas)msg;
			String serverId = tds.getTrans().getServerId();
			LineTransServer.transTaskMap.get(serverId).returnTrans(tds);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("已和事务调度器建立连接");
		super.channelActive(ctx);
	}

	
	
}
