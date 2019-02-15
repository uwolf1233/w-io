package com.wolf.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.wolf.javabean.MQ.MQData;

public class SubscriberServer implements Runnable{

	public static String ip = "localhost";
	public static int port = 8005;
	public static Channel channel = null;
	public static String systemtype = "windows";
	public static int groupNum = 2;
	
	public void init(){
		Bootstrap boot = new Bootstrap();
		NioEventLoopGroup group = new NioEventLoopGroup(groupNum);
		Class channelclass = null;
		if(systemtype.equals("linux")){
			channelclass = EpollSocketChannel.class;
		}else{
			channelclass = NioSocketChannel.class;
		}
		try {
			boot.group(group)
			.channel(channelclass)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					// TODO Auto-generated method stub
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(MQData.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new ReturnMessageHandle());
				}
			});
			channel = boot.connect(ip,port).sync().channel();
			System.out.println("连接服务:"+ip+":"+port);
			//send();
			channel.closeFuture().sync();
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
	
//	//测试代码
//	public void send(){
//		MQData.Builder builder = MQData.newBuilder();
//		builder.setType("regSubscriber");
//		builder.getSubscriberBuilder().setId("123");
//		builder.getSubscriberBuilder().setName("abc");
//		channel.writeAndFlush(builder);
//
////		System.out.println("123脱离注册");
////		MQData.Builder builder1 = MQData.newBuilder();
////		builder1.setType("unRegSubscriber");
////		builder1.getConsumerBuilder().setId("123");
////		builder1.getConsumerBuilder().setName("abc");
////		channel.writeAndFlush(builder1);
////		
////		MQData.Builder builder2 = MQData.newBuilder();
////		builder2.setType("regSubscriber");
////		builder2.getConsumerBuilder().setId("123");
////		builder2.getConsumerBuilder().setName("abc");
////		channel.writeAndFlush(builder2);
//	}
//	
//	public static void main(String[] args) {
//		SubscriberServer test = new SubscriberServer();
//		test.init();
//	}
	
}
