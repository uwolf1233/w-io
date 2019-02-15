package com.wolf.Service;

import com.wolf.javabean.Producer;
import com.wolf.javabean.Publisher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TranLineHandle extends SimpleChannelInboundHandler{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object arg1)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("判断是否有需要重连的对象");
		Channel channel = ctx.channel();
		Publisher.allLineTran(channel);
		Producer.allLineTran(channel);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}

}
