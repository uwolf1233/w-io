package com.wolf.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.google.protobuf.ByteString;
import com.wolf.javabean.MQ.MQData;

public class SendMessageTest {
	
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
				}
			});
			channel = boot.connect(ip,port).sync().channel();
			System.out.println("连接服务:"+ip+":"+port);
//			send("queue1");
//			send("queue2");
			for(int i=0;i<20;i++){
				new Thread(new TestThread()).start();
//				send("queue3");
//				send("queue4");
			}
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			group.shutdownGracefully();
		}
	}
	
	class TestThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			send("queue3");
			send("queue4");
		}
		
	}
	
	//测试代码
	public void send(String queuename){
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("sendTopicMessage");
		System.out.println("发送队列:"+queuename);
		builder.setQueuename(queuename);
		builder.getProducerBuilder().setId(UUID.randomUUID().toString().replace("-", ""));
		builder.getProducerBuilder().setName("abc");
		String data = "abc123";
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			ByteString bs = ByteString.copyFrom(baos.toByteArray());
			builder.addData(bs);
			channel.writeAndFlush(builder);
			System.out.println("推送");
			//Thread.sleep(5000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(baos != null){
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(oos != null){
					oos.close();
					oos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		SendMessageTest test = new SendMessageTest();
		test.init();
	}
	
}
