package com.wolf.center;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.javabean.ServerBean;
import com.wolf.javabean.SystemNet;
import com.wolf.locks.DistributedLock;
import com.wolf.server.HttpServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class CenterServer implements Runnable{

	//public static ConcurrentMap<String,List<CenterHandle>> serverMap = new ConcurrentHashMap<String,List<CenterHandle>>();//key:请求路径
	//public static ConcurrentMap<String,String[]> serverLockMap = new ConcurrentHashMap<String,String[]>();//key:请求路径,value:锁名称
	public static ConcurrentMap<String,DistributedLock> locObjkMap = new ConcurrentHashMap<String,DistributedLock>();//key:锁名称,value:锁对象
	public static ConcurrentMap<String,ServerBean> serverMap = new ConcurrentHashMap<String,ServerBean>();
	public static int clientPort = 0;
	public static int clientBossNum = 0;
	public static int clientWorkNum = 0;
	
	//请求转发通道
	public void init(){
		ServerBootstrap boot = new ServerBootstrap();
//		EventLoopGroup boss = new NioEventLoopGroup(clientBossNum);
//		EventLoopGroup worker = new NioEventLoopGroup(clientWorkNum);
		System.out.println("注册中心服务正在启动.....");
		Class channelclass = null;
		EventLoopGroup bossGroup = null;
		EventLoopGroup workerGroup = null;
		if(HttpServer.systemtype.equals("linux")){
			bossGroup = new EpollEventLoopGroup(clientBossNum); 
			workerGroup = new EpollEventLoopGroup(clientWorkNum);
			channelclass = EpollServerSocketChannel.class;
		}else{
			bossGroup = new NioEventLoopGroup(clientBossNum); 
			workerGroup = new NioEventLoopGroup(clientWorkNum);
			channelclass = NioServerSocketChannel.class;
		}
		try{
			boot.group(bossGroup, workerGroup);
			boot.channel(channelclass);
			boot.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {//采用protobuf3作为传输的技术
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(SystemNet.Datas.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new CenterHandle());//注册中心handle
					ch.pipeline().addLast(new HttpServletResponseHandle());//需要返回前端数据相关处理的handle
					ch.pipeline().addLast(new ServerSendHandle());//服务之间相互调用
					ch.pipeline().addLast(new WebSocketResponse());//处理websocket返回
					ch.pipeline().addLast(new PathDataCacheHandle());//业务端主推的缓存
				}
			}).option(ChannelOption.SO_BACKLOG, 256);
//				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
//            .handler(new LoggingHandler(LogLevel.DEBUG))
//            .childHandler(new LoggingHandler(LogLevel.DEBUG));
			System.out.println("启动注册中心服务:"+clientPort);
			boot.bind(clientPort).sync().channel().closeFuture().sync();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getClientPort() {
		return clientPort;
	}

	public static void setClientPort(int clientPort) {
		CenterServer.clientPort = clientPort;
	}

	public static int getClientBossNum() {
		return clientBossNum;
	}

	public static void setClientBossNum(int clientBossNum) {
		CenterServer.clientBossNum = clientBossNum;
	}

	public static int getClientWorkNum() {
		return clientWorkNum;
	}

	public static void setClientWorkNum(int clientWorkNum) {
		CenterServer.clientWorkNum = clientWorkNum;
	}
	
}
