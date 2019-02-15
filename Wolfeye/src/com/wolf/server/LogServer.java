package com.wolf.server;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.wolf.javabean.LogBean;
import com.wolf.javabean.LogBean.LogDatas;

public class LogServer {

	private static int bossNum = 0;
	private static int workerNum = 0;
	private static String systemtype = "";
	
	public void init(int port){
		ServerBootstrap boot = new ServerBootstrap();
		//EventLoopGroup boss = new NioEventLoopGroup(bossNum);
		//EventLoopGroup worker = new NioEventLoopGroup(workerNum);
		EventLoopGroup boss = null;
		EventLoopGroup worker = null;
		Class channelClass = null;
		System.out.println("当前为"+systemtype+"系统");
		if(systemtype.equals("linux")){
			boss = new EpollEventLoopGroup(bossNum);
			worker = new EpollEventLoopGroup(workerNum);
			channelClass = EpollServerSocketChannel.class;
		}else{
			boss = new NioEventLoopGroup(bossNum);
			worker = new NioEventLoopGroup(workerNum);
			channelClass = NioServerSocketChannel.class;
		}
		try{
			boot.group(boss, worker);
			boot.channel(channelClass);
			boot.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {//采用protobuf3作为传输的技术
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(LogDatas.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new LogHandle());
				}
			}).option(ChannelOption.SO_BACKLOG, 256);
			System.out.println("启动服务:"+port);
			boot.bind(port).sync().channel().closeFuture().sync();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		InputStream in = null;
		try {
//			String path = LogServer.class.getClass().getResource("/").getPath();
//			File file = new File(path+"/WebConfig.xml");
			//path = path.substring(1,path.length());
			in = LogServer.class.getResourceAsStream("/WebConfig.xml");
			if(in != null){
				SAXReader reader = new SAXReader();
				Document document = reader.read(in);
				Element configE = document.getRootElement();
				int port = Integer.parseInt(configE.element("port").getText());
				Element bossNumE = configE.element("bossNum");
				Element workerNumE = configE.element("workerNum");
				bossNum = Integer.parseInt(bossNumE.getText());
				workerNum = Integer.parseInt(workerNumE.getText());
				
				Element slogfilePathE = configE.element("slogfilePath");
				Element slogfileindexE = configE.element("slogfileindex");
				Element elogfilePathE = configE.element("elogfilePath");
				Element elogfileindexE = configE.element("elogfileindex");
				
				FileHandle.slogfilePath = slogfilePathE.getText();
				FileHandle.slogfileindex = slogfileindexE.getText();
				FileHandle.elogfilePath = elogfilePathE.getText();
				FileHandle.elogfileindex = elogfileindexE.getText();
				
				Element systemtypeE = configE.element("systemtype");
				systemtype = systemtypeE.getText();
				FileHandle.create();
				new LogServer().init(port);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
