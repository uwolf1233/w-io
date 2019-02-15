package com.wolf.test;

import java.util.UUID;

import com.wolf.javabean.MQ.MQData;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class ConsumerTest1 {

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
					ch.pipeline().addLast(new ConsumerHandle());
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
			MQData.Builder builder0 = MQData.newBuilder();
			builder0.setType("createQueue");
			builder0.setQueuename("queue2");
			channel.writeAndFlush(builder0);
			
			Thread.sleep(2000);
			
			MQData.Builder builder = MQData.newBuilder();
			builder.setType("regConsumer");
			builder.setQueuename("queue2");
			builder.getConsumerBuilder().setId("456");
			builder.getConsumerBuilder().setName("abc");
			channel.writeAndFlush(builder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ConsumerTest1 test = new ConsumerTest1();
		test.init();
	}
	
}
