package com.wolf.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.wolf.javabean.MQ.MQData;

public class SubscriberTest1 {

	public static String ip = "localhost";
	public static int port = 8005;
	public static Channel channel = null;
	
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
					ch.pipeline().addLast(new ProtobufDecoder(MQData.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new SubscriberHandle());
				}
			});
			channel = boot.connect(ip,port).sync().channel();
			System.out.println("连接服务:"+ip+":"+port);
			send();
			channel.closeFuture().sync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			group.shutdownGracefully();
		}
	}
	
	//测试代码
		public void send(){
			
			try {
				MQData.Builder builder = MQData.newBuilder();
				builder.setType("createTopic");
				builder.setQueuename("queue4");
				builder.getSubscriberBuilder().setId("456");
				builder.getSubscriberBuilder().setName("def");
				channel.writeAndFlush(builder);
				
				Thread.sleep(2000);
				
				MQData.Builder builder1 = MQData.newBuilder();
				builder1.setType("regSubscriber");
				builder1.setQueuename("queue4");
				builder1.getSubscriberBuilder().setId("456");
				builder1.getSubscriberBuilder().setName("def");
				channel.writeAndFlush(builder1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//			System.out.println("123脱离注册");
//			MQData.Builder builder1 = MQData.newBuilder();
//			builder1.setType("unRegSubscriber");
//			builder1.getConsumerBuilder().setId("123");
//			builder1.getConsumerBuilder().setName("abc");
//			channel.writeAndFlush(builder1);
//			
//			MQData.Builder builder2 = MQData.newBuilder();
//			builder2.setType("regSubscriber");
//			builder2.getConsumerBuilder().setId("123");
//			builder2.getConsumerBuilder().setName("abc");
//			channel.writeAndFlush(builder2);
		}
	
	public static void main(String[] args) {
		SubscriberTest1 test = new SubscriberTest1();
		test.init();
	}
	
}
