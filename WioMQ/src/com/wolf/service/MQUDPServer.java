package com.wolf.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import com.wolf.javabean.MQ.MQData;

public class MQUDPServer implements Runnable{

	public static int port = 8006;
	public static String systemtype = "windows";
	public static int udpgroup = 2;

	private final NioEventLoopGroup group = new NioEventLoopGroup(udpgroup);
	
	public void init(){
		Bootstrap boot = new Bootstrap();
		Class channelclass = null;
		if(systemtype.equals("linux")){
			channelclass = EpollDatagramChannel.class;
		}else{
			channelclass = NioDatagramChannel.class;
		}
		try {
			boot.channel(channelclass)
			.group(group)
			.option(ChannelOption.SO_BROADCAST, true)
			.handler(new ChannelInitializer<DatagramChannel>() {

				@Override
				protected void initChannel(DatagramChannel ch) throws Exception {
					// TODO Auto-generated method stub
					ChannelPipeline cp = ch.pipeline();
					cp.addLast(new MQUDPHandle());
					cp.addLast(new MQHandle());
				}
				
			})
			.bind(port).sync().channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			group.shutdownGracefully();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		init();
	}

	public static void main(String[] args) {
		//new Thread(new MQServer()).start();
	}
	
}
