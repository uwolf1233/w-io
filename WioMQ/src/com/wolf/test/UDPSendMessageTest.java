package com.wolf.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.google.protobuf.ByteString;
import com.wolf.javabean.MQ.MQData;
import com.wolf.service.MQUDPHandle;

public class UDPSendMessageTest {
	
	public static String ip = "localhost";
	public static int port = 8006;
	public static Channel channel = null;

	public void init(){
		Bootstrap boot = new Bootstrap();
		NioEventLoopGroup group = new NioEventLoopGroup(2);
		try {
			boot.group(group)
			.channel(NioDatagramChannel.class)
			.option(ChannelOption.SO_BROADCAST, true)
			.handler(new ChannelInitializer<DatagramChannel>() {

				@Override
				protected void initChannel(DatagramChannel ch) throws Exception {
					// TODO Auto-generated method stub
				}
				
			});
			channel = boot.bind(0).channel();
			send("queue3");
			send("queue4");
			send("queue3");
			send("queue4");
			//channel.closeFuture().sync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			group.shutdownGracefully();
		}
	}
	
	//测试代码
	public void send(String queuename){
		MQData.Builder builder = MQData.newBuilder();
		//builder.setType("sendQueueMessage");
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
			DatagramPacket dp = new DatagramPacket(Unpooled.wrappedBuffer(builder.build().toByteArray()),
					new InetSocketAddress("127.0.0.1", port));
			channel.writeAndFlush(dp);
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
		UDPSendMessageTest test = new UDPSendMessageTest();
		test.init();
	}
	
}
