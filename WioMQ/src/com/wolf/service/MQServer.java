package com.wolf.service;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.wolf.db.MQDBImpl;
import com.wolf.javabean.Producer_Consumer;
import com.wolf.javabean.TranMessage;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.TranMessageMg;
import com.wolf.serconfig.Configs;

import io.netty.bootstrap.ServerBootstrap;
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
import io.netty.handler.timeout.IdleStateHandler;

public class MQServer implements Runnable{
	
	public static int bossNum = 2;
	public static int workerNum = 4;
	public static int port = 8005;
	public static String systemtype = "windows";

	public void init(){
		ServerBootstrap boot = new ServerBootstrap();
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
		try{
			boot.group(bossGroup, workerGroup);
			boot.channel(channelclass);
			boot.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {//采用protobuf3作为传输的技术
					ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));//心跳机制，判断是否掉线
                	ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(MQData.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new MQHandle());
				}
			}).option(ChannelOption.SO_BACKLOG, 256);
			System.out.println("启动消息服务:"+port);
			boot.bind(port).sync().channel().closeFuture().sync();
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
		init();
	}

	public static void main(String[] args) {
		try {
			readConfig();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		new Thread(new MQServer()).start();
		new Thread(new MQUDPServer()).start();
	}
	
	private static void readConfig() throws Throwable{
		InputStream in = MQServer.class.getResourceAsStream("/Config.xml");
		if(in!=null){
			SAXReader reader = new SAXReader();
			Document document = reader.read(in);
			Element configE = document.getRootElement();
			
			Element tcpbossNumE = configE.element("tcpbossNum");
			bossNum = Integer.parseInt(tcpbossNumE.getText());
			Element tcpworkerNumE = configE.element("tcpworkerNum");
			workerNum = Integer.parseInt(tcpworkerNumE.getText());
			Element tcpportE = configE.element("tcpport");
			port = Integer.parseInt(tcpportE.getText());
			
			Element systemtypeE = configE.element("systemtype");
			String systemTypes = systemtypeE.getText();
			systemtype = systemTypes;
			
			Element udpgroupNumE = configE.element("udpgroupNum");
			MQUDPServer.udpgroup = Integer.parseInt(udpgroupNumE.getText());
			Element udpportE = configE.element("udpport");
			MQUDPServer.port = Integer.parseInt(udpportE.getText());
			MQUDPServer.systemtype = systemTypes;
			
			Element tranMessagemaxTimeE = configE.element("tranMessagemaxTime");
			TranMessage.maxTime = Integer.parseInt(tranMessagemaxTimeE.getText());
			Element tranMessageQueueE = configE.element("tranMessageQueue");
			int tranMessageQueue = Integer.parseInt(tranMessageQueueE.getText());
			TranMessageMg.INSTANCE.setTranMessageQueue(new TranMessage[tranMessageQueue]);
			
			Element tranMessageScanMinuteE = configE.element("tranMessageScanMinute");
			int tranMessageScanMinute = Integer.parseInt(tranMessageScanMinuteE.getText());
			TranMessageMg.INSTANCE.tranMessageScanMinute = tranMessageScanMinute;
			
			Element consumerQueueNumE = configE.element("consumerQueueNum");
			int consumerQueueNum = Integer.parseInt(consumerQueueNumE.getText());
			Producer_Consumer.consumerQueueNum = consumerQueueNum;
			
			Element isdbE = configE.element("isdb");
			MQDBImpl.INSTANCE.setIsDB(Boolean.parseBoolean(isdbE.getText()));
			if(!MQDBImpl.INSTANCE.getIsDB()){
				return;
			}
			
			Element dburlE = configE.element("dburl");
			Configs.url = dburlE.getText();
			Element dbusernameE = configE.element("dbusername");
			Configs.username = dbusernameE.getText();
			Element dbuserpassE = configE.element("dbuserpass");
			Configs.password = dbuserpassE.getText();
			Element cachePrepStmtsE = configE.element("cachePrepStmts");
			Configs.cachePrepStmts = cachePrepStmtsE.getText();
			Element prepStmtCacheSizeE = configE.element("prepStmtCacheSize");
			Configs.prepStmtCacheSize = prepStmtCacheSizeE.getText();
			Element prepStmtCacheSqlLimitE = configE.element("prepStmtCacheSqlLimit");
			Configs.prepStmtCacheSqlLimit = prepStmtCacheSqlLimitE.getText();
			
			Configs.dbconfig();
		}
	}
	
}
