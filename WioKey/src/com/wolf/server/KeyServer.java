package com.wolf.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import com.wolf.javabean.Key.Datas;
import com.wolf.keys.StringHandle;

public class KeyServer {

	public static int bossNum = 2;
	public static int workerNum = 2;
	public static int port = 8007;
	public static String systemtype = "windows";
	
	public void start() throws Exception {
		Class channelclass = null;
		EventLoopGroup bossGroup = null;
		EventLoopGroup workerGroup = null;
		
		if(systemtype.equals("linux")){
			bossGroup = new EpollEventLoopGroup(bossNum); 
			workerGroup = new EpollEventLoopGroup(workerNum);
			channelclass = EpollServerSocketChannel.class;
		}else{
			bossGroup = new NioEventLoopGroup(bossNum); 
			workerGroup = new NioEventLoopGroup(workerNum);
			channelclass = NioServerSocketChannel.class;
		}
		
        try {
            ServerBootstrap b = new ServerBootstrap(); 
            b.group(bossGroup, workerGroup)
                    .channel(channelclass) 
                    .childHandler(new ChannelInitializer<SocketChannel>() { 
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                        	//ch.pipeline().addLast(new HttpTestHandle());
                        	ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));//心跳机制，判断是否掉线
                        	ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
        					ch.pipeline().addLast(new ProtobufDecoder(Datas.getDefaultInstance()));
        					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
        					ch.pipeline().addLast(new ProtobufEncoder());
        					ch.pipeline().addLast(new StringHandle());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 256) //服务端可连接队列
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//保持连接
            System.out.println("server start");
            System.out.println("启动加密解密服务:"+port);
            ChannelFuture f = b.bind(port).sync(); 
            f.channel().closeFuture().sync();//同步关闭
        } finally {
            workerGroup.shutdownGracefully();//优雅推出netty线程池
            bossGroup.shutdownGracefully();//优雅推出netty线程池
        }
    }
	
	public static void main(String[] args) {
		try {
			StringHandle.init();
			new KeyServer().start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
