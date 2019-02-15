package com.wolf.Service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPSendMessage implements Runnable{
	
	public static String ip = "localhost";
	public static int port = 8006;
	public static Channel channel = null;
	public static String systemtype = "windows";
	public static int groupNum = 2;

	public void init(){
		Bootstrap boot = new Bootstrap();
		Class channelclass = null;
		if(systemtype.equals("linux")){
			channelclass = EpollDatagramChannel.class;
		}else{
			channelclass = NioDatagramChannel.class;
		}
		NioEventLoopGroup group = new NioEventLoopGroup(groupNum);
		try {
			boot.group(group)
			.channel(channelclass)
			.option(ChannelOption.SO_BROADCAST, true)
			.handler(new ChannelInitializer<DatagramChannel>() {

				@Override
				protected void initChannel(DatagramChannel ch) throws Exception {
					// TODO Auto-generated method stub
				}
				
			});
			channel = boot.bind(0).channel();
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
//		//builder.setType("sendQueueMessage");
//		builder.setType("sendTopicMessage");
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
//			DatagramPacket dp = new DatagramPacket(Unpooled.wrappedBuffer(builder.build().toByteArray()),
//					new InetSocketAddress("127.0.0.1", port));
//			channel.writeAndFlush(dp);
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
//		UDPSendMessage test = new UDPSendMessage();
//		test.init();
//	}
	
}
