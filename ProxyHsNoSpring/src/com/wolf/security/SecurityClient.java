package com.wolf.security;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.wolf.javabean.Session.SessionData;
import com.wolf.javabean.SessionLine;

public class SecurityClient implements Runnable{

	public static String Ip;
	public static int Port;
	public static Channel sessionChannel;
	
	public void init(){
		Bootstrap boot = new Bootstrap();
		NioEventLoopGroup group = new NioEventLoopGroup(2);
		try {
			boot.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					// TODO Auto-generated method stub
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(SessionData.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new SecurityHandle());
				}
			});
			sessionChannel = boot.connect(Ip,Port).sync().channel();
			System.out.println("启动会话服务:"+Ip+":"+Port);
			sessionChannel.closeFuture().sync();
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
	
	public static void main(String[] args) {
		SecurityClient.Ip = "localhost";
		SecurityClient.Port = 8003;
		new Thread(new SecurityClient()).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SessionLine sessionLine = new SessionLine();
		sessionLine.create(null);
		String sessionId = sessionLine.getSessionId();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object o = sessionLine.hasSession();
		if(o != null){
			System.out.println(o.toString());
		}else if(o == null){
			System.out.println("o is null");
		}else{
			System.out.println("sessionNull");
		}
		
		//sessionLine.setAttr("abc", "ddd123123aaa");
		//sessionLine.setAttr("abc1", "ddd454545aaa");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		o = sessionLine.getAttr("abc");
		System.out.println("abc-----"+o.toString());
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		o = sessionLine.hasSession();
		System.out.println("2----------------");
		if(o != null){
			System.out.println(o.toString());
		}else if(o == null){
			System.out.println("o is null");
		}else{
			System.out.println("sessionNull");
		}
		System.out.println("2----------------");
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sessionLine.clearAttr();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		o = sessionLine.getAttr("abc1");
		System.out.println("abc1-----"+o.toString());
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		o = sessionLine.hasSession();
		System.out.println("3----------------");
		if(o != null){
			System.out.println(o.toString());
		}else if(o == null){
			System.out.println("o is null");
		}else{
			System.out.println("sessionNull");
		}
		System.out.println("3----------------");
	}
	
}
