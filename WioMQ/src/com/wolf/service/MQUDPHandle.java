package com.wolf.service;

import com.wolf.javabean.MQ.MQData;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;

public class MQUDPHandle extends SimpleChannelInboundHandler<DatagramPacket>{

	@Override
	protected void channelRead0(ChannelHandlerContext ch, DatagramPacket packet)
			throws Exception {
		// TODO Auto-generated method stub
		try {
			ByteBuf bf = packet.content();
			byte[] bytes = new byte[bf.readableBytes()];
			bf.readBytes(bytes);
			MQData mqdata = MQData.parseFrom(bytes);
			ch.fireChannelRead(mqdata);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			//ReferenceCountUtil.release(packet);
		}
	}

}
