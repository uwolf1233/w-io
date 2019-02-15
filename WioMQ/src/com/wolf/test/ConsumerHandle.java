package com.wolf.test;

import java.io.ObjectOutputStream;
import java.util.UUID;

import com.google.protobuf.ByteString;
import com.wolf.javabean.MQ.MQData;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConsumerHandle extends ChannelInboundHandlerAdapter{

	private static ConsumerI consumerI = new ConsumerImpl();
	public String id = "";
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		id = UUID.randomUUID().toString().replace("-", "");
		System.out.println(id+"连接");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(id+"断开连接");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(msg instanceof MQData){
			MQData mqdata = (MQData)msg;
			if(mqdata.getType().equals("returnQueueMessage")){
				int size = mqdata.getDataCount();
				for(int i=0;i<size;i++){
					ByteString bs = mqdata.getData(i);
					byte[] bytes = bs.toByteArray();
					consumerI.getDatas(bytes);
					System.out.println(id+"获得消息");
				}
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
