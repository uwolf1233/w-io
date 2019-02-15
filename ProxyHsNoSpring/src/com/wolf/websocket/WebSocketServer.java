package com.wolf.websocket;

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
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import com.wolf.server.HttpServer;
import com.wolf.server.HttpServerHandler;

public class WebSocketServer implements Runnable{

	public static int bossGroupNum;
	public static int workerGroupNum;
	public static int port;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void start() throws Exception {
//        EventLoopGroup bossGroup = new NioEventLoopGroup(bossGroupNum); 
//        EventLoopGroup workerGroup = new NioEventLoopGroup(workerGroupNum);//线程池可以慢慢调试
		System.out.println("WebSocket服务正在启动.....");
		Class channelclass = null;
		EventLoopGroup bossGroup = null;
		EventLoopGroup workerGroup = null;
		if(HttpServer.systemtype.equals("linux")){
			bossGroup = new EpollEventLoopGroup(bossGroupNum); 
			workerGroup = new EpollEventLoopGroup(workerGroupNum);
			channelclass = EpollServerSocketChannel.class;
		}else{
			bossGroup = new NioEventLoopGroup(bossGroupNum); 
			workerGroup = new NioEventLoopGroup(workerGroupNum);
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
                        	ch.pipeline().addLast(new HttpServerCodec());
                        	ch.pipeline().addLast(new HttpObjectAggregator(65535));
                        	ch.pipeline().addLast(new ChunkedWriteHandler());
                        	ch.pipeline().addLast(new HttpContentCompressor());
                        	ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                            ch.pipeline().addLast(new WebSocketHandle());

                        }
                    }).option(ChannelOption.SO_BACKLOG, 256) //服务端可连接队列
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//保持连接
            System.out.println("server start");
            System.out.println("启动WebSocket服务:"+port);
            ChannelFuture f = b.bind(port).sync(); 
            f.channel().closeFuture().sync();//同步关闭
        } finally {
            workerGroup.shutdownGracefully();//优雅推出netty线程池
            bossGroup.shutdownGracefully();//优雅推出netty线程池
        }
    }

	public static int getBossGroupNum() {
		return bossGroupNum;
	}

	public static void setBossGroupNum(int bossGroupNum) {
		WebSocketServer.bossGroupNum = bossGroupNum;
	}

	public static int getWorkerGroupNum() {
		return workerGroupNum;
	}

	public static void setWorkerGroupNum(int workerGroupNum) {
		WebSocketServer.workerGroupNum = workerGroupNum;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		WebSocketServer.port = port;
	}

}
