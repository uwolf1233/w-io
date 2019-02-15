package com.wolf.log;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.UUID;
import java.util.concurrent.Executors;

import com.wolf.ChannelWriter.LogWriter;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.server.TransServer;

public class LogServer implements Runnable{

	public static String logIp = "localhost";
	public static int logPort = 8002;
	public static Channel logChannel;
	
	public void init(){
		Bootstrap boot = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup(2);
		Class channelClass = NioSocketChannel.class;
		try {
			boot.group(group)
			.channel(channelClass)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					// TODO Auto-generated method stub
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(LogDatas.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new LogHandle());
				}
				
			});
			System.out.println("连接日志服务:"+logIp+":"+logPort);
			logChannel = boot.connect(logIp,logPort).sync().channel();
			if(LogWriter.logWriter.getChannel() == null){
				new Thread(LogWriter.logWriter.setLogWriter(logChannel)).start();
			}else{
				LogWriter.logWriter.setLogWriter(logChannel);
			}
			logChannel.closeFuture().sync();
		} catch (Exception e) {
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
	
}
