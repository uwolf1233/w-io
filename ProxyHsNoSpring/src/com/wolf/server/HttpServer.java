package com.wolf.server;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.wolf.cache.MemorySee;
import com.wolf.cache.StaticFileCache;
import com.wolf.cache.StaticFileCacheLinux;
import com.wolf.center.CenterServer;
import com.wolf.channel.LoadStrategyImpl;
import com.wolf.locks.DistributedLockMg;
import com.wolf.log.LogServer;
import com.wolf.security.SecurityClient;
import com.wolf.websocket.WebSocketServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.util.ResourceLeakDetector;

public class HttpServer implements Runnable{

	//public static int httpObjectAggregatornum = 63365;//包装为response，request
	public static String cpath = "";
	public static String systemtype = "";
	public static int port = 0;
	public static String ip = "";
	public static int httpBossNum = 0;
	public static int httpWorkerNum = 0;
	public static String webSocketPath = "/wioWebsocket";//定义websocket初始连接
	
	public void start() throws Exception {ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
//        EventLoopGroup bossGroup = new NioEventLoopGroup(httpBossNum); 
//        EventLoopGroup workerGroup = new NioEventLoopGroup(httpWorkerNum);//线程池可以慢慢调试
		Class channelclass = null;
		EventLoopGroup bossGroup = null;
		EventLoopGroup workerGroup = null;
		if(systemtype.equals("linux")){
			bossGroup = new EpollEventLoopGroup(httpBossNum); 
			workerGroup = new EpollEventLoopGroup(httpWorkerNum);
			channelclass = EpollServerSocketChannel.class;
		}else{
			bossGroup = new NioEventLoopGroup(httpBossNum); 
			workerGroup = new NioEventLoopGroup(httpWorkerNum);
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
                        	//ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));//心跳机制，判断是否掉线
                        	ch.pipeline().addLast(new ChannelTrafficShapingHandler(1*1024*1024, 20*1024*1024, 10));
                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                        	//ch.pipeline().addLast(new HttpServerCodec());
                        	ch.pipeline().addLast(new HttpContentCompressor());
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            ch.pipeline().addLast(
                            		new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(10*1024*1024));
                            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            ch.pipeline().addLast(new HttpServerHandler());//自定义handle

                        }
                    }).option(ChannelOption.SO_BACKLOG, 256) //服务端可连接队列
//                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
//                    .handler(new LoggingHandler(LogLevel.DEBUG))
//                    .childHandler(new LoggingHandler(LogLevel.DEBUG))
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//保持连接
            System.out.println("server start");
            System.out.println("启动HTTP服务:"+port);
            ChannelFuture f = b.bind(port).sync(); 
            f.channel().closeFuture().sync();//同步关闭
        } finally {
            workerGroup.shutdownGracefully();//优雅推出netty线程池
            bossGroup.shutdownGracefully();//优雅推出netty线程池
        }
    }
	
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

	//读取配置，目前还在完善
    public static void init() throws Exception {
		System.out.println("HTTP服务正在启动.....");
    	try {
    		InputStream in = HttpServer.class.getResourceAsStream("/WebConfig.xml");
	        HttpServer server = new HttpServer();
	        loadWebConfig(in);
			DistributedLockMg.INSTANCE.scanDistributedLock();
			if(systemtype.equals("windows")){
				StaticFileCache.getStaticFile();
			}else{
				StaticFileCacheLinux.getStaticFile();
			}
			//SessionMsg.INSTANCE.scanSessions();
			new Thread(new MemorySee()).start();
			new Thread(new LogServer()).start();
			new Thread(server).start();
			new Thread(new CenterServer()).start();
			new Thread(new WebSocketServer()).start();
			new Thread(new SecurityClient()).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
    //读取配置，目前还在完善
    public static void loadWebConfig(InputStream in){
    	try {
			if (in != null){
				SAXReader reader = new SAXReader();
				Document document = reader.read(in);
				Element webConfigE = document.getRootElement();
				Element serverConfigE = webConfigE.element("serverConfig");
//				Element sessionE = serverConfigE.element("session");
//				Element sessionScanMinuteE = sessionE.element("sessionScanMinute");
				Element viewE = serverConfigE.element("view");
				Element maxReadContE = serverConfigE.element("maxReadCont");
				Element pathE = viewE.element("path");
				Element viewtypeE = viewE.element("viewtype");
				ip = serverConfigE.attributeValue("ip");
				port = Integer.parseInt(serverConfigE.attributeValue("port"));
				systemtype = serverConfigE.attributeValue("type");
				httpBossNum = Integer.parseInt(serverConfigE.attributeValue("httpBossNum"));
				httpWorkerNum = Integer.parseInt(serverConfigE.attributeValue("httpWorkerNum"));
				//SessionMsg.INSTANCE.setSessionScanMinute(Long.parseLong(sessionScanMinuteE.getText()));
				//SessionMsg.INSTANCE.init();
				ControllerProxy.viewPath = pathE.getText();
				String viewtype = viewtypeE.getText();
				ControllerProxy.viewFileType = viewtype.split(",");
				HttpServerHandler.maxReadCont = Integer.parseInt(maxReadContE.getText());
				
				Element clientConfigE = webConfigE.element("clientConfig");
				CenterServer.clientPort = Integer.parseInt(clientConfigE.attributeValue("port"));
				CenterServer.clientBossNum = Integer.parseInt(clientConfigE.attributeValue("clientBossNum"));
				CenterServer.clientWorkNum = Integer.parseInt(clientConfigE.attributeValue("clientWorkNum"));
				
				Element websocketConfigE = webConfigE.element("websocketConfig");
				WebSocketServer.port = Integer.parseInt(websocketConfigE.attributeValue("port"));
				WebSocketServer.bossGroupNum = Integer.parseInt(websocketConfigE.attributeValue("BossNum"));
				WebSocketServer.workerGroupNum = Integer.parseInt(websocketConfigE.attributeValue("WorkNum"));
				
				LogServer.logIp = webConfigE.element("logIp").getText();
				LogServer.logPort = Integer.parseInt(webConfigE.element("logPort").getText());
				
				Element loadStrategyE = webConfigE.element("loadStrategy");
				
				LoadStrategyImpl.INSTANCE.setType(Integer.parseInt(loadStrategyE.getText()));
				
				Element sessionIpE = webConfigE.element("sessionIp");
				SecurityClient.Ip = sessionIpE.getText();
				Element sessionPortE = webConfigE.element("sessionPort");
				SecurityClient.Port = Integer.parseInt(sessionPortE.getText());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public static String getCpath() {
		return cpath;
	}

	public static void setCpath(String cpath) {
		HttpServer.cpath = cpath;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		HttpServer.port = port;
	}

	public static String getIp() {
		return ip;
	}

	public static void setIp(String ip) {
		HttpServer.ip = ip;
	}

	public static int getHttpBossNum() {
		return httpBossNum;
	}

	public static void setHttpBossNum(int httpBossNum) {
		HttpServer.httpBossNum = httpBossNum;
	}

	public static int getHttpWorkerNum() {
		return httpWorkerNum;
	}

	public static void setHttpWorkerNum(int httpWorkerNum) {
		HttpServer.httpWorkerNum = httpWorkerNum;
	}

	public static String getWebSocketPath() {
		return webSocketPath;
	}

	public static void setWebSocketPath(String webSocketPath) {
		HttpServer.webSocketPath = webSocketPath;
	}
        
}
