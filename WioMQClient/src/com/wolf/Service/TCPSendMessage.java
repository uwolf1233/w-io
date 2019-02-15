package com.wolf.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import com.wolf.javabean.MQ.MQData;

public class TCPSendMessage implements Runnable{
	
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
					ch.pipeline().addLast(new TranLineHandle());
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
			//group.shutdownGracefully();//断线重连不需要优雅关闭
			line();
		}
	}
	
	public int a = 0;
	
	public void line(){
		try {
			a++;
			if(a>=5){
				return;//实在连接不上
			}
			Thread.sleep(3000);
			System.out.println("正在尝试第"+a+"次重连...");
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
//		builder.setType("sendQueueMessage");
//		//builder.setType("sendTopicMessage");
//		builder.getProducerBuilder().setId(UUID.randomUUID().toString().replace("-", ""));
//		builder.getProducerBuilder().setName("abc");
//		String data = "abc123";
//		ByteArrayOutputStream baos = null;
//		ObjectOutputStream oos = null;
//		try {
//			baos = new ByteArrayOutputStream();
//			oos = new ObjectOutputStream(baos);
//			oos.writeObject(data);
//			ByteString bs = ByteString.copyFrom(baos.toByteArray());
//			builder.addData(bs);
//			channel.writeAndFlush(builder);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//			try {
//				if(baos != null){
//					baos.close();
//					baos = null;
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			try {
//				if(oos != null){
//					oos.close();
//					oos = null;
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	public static void main(String[] args) {
//		TCPSendMessage test = new TCPSendMessage();
//		test.init();
//	}
	
}
