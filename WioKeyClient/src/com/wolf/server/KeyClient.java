package com.wolf.server;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.wolf.javabean.WioKeyThreadPool;
import com.wolf.javabean.WioKeyThreadResult;
import com.wolf.javabean.Key.Datas;
import com.wolf.result.KeyGetResult;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class KeyClient implements Runnable{

	public static String ip = "localhost";
	public static int port = 8007;
	public static Channel channel = null;
	public static ExecutorService wtppool = WioKeyThreadPool.pool;
	
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
					ch.pipeline().addLast(new ProtobufDecoder(Datas.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new KeyClientHandle());
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		init();
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
	
	public static void emcrypt(String data,KeyGetResult getResult){
		WioKeyThreadResult wioThreadResult = new WioKeyThreadResult(new SynchronousQueue<Object>());
		KeyClientRun kcr = new KeyClientRun("emcrypt", new Object[]{data}, wioThreadResult,getResult);
		WioKeyThreadPool.pool.execute(kcr);
	}
	
	public static byte[] emcryptRun(String data,WioKeyThreadResult wioThreadResult){
		Datas.Builder builder = Datas.newBuilder();
		String id = UUID.randomUUID().toString().replace("-", "");
		builder.setId(id);
		builder.setType("2");
		builder.getEmcryptBuilder().setData(data);
		BlockingQueue<Object> queue = null;
		if(wioThreadResult == null){
			queue = new SynchronousQueue<Object>();//等待队列
		}else{
			queue = wioThreadResult.getQueue();
		}
		KeyClientHandle.retQueueMap.put(id, queue);
		send(builder, id);
		try {
			byte[] bytes = (byte[]) queue.poll(3, TimeUnit.SECONDS);
			KeyClientHandle.retQueueMap.remove(id);
			return bytes;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void dmcrypt(byte[] bytes,KeyGetResult getResult){
		WioKeyThreadResult wioThreadResult = new WioKeyThreadResult(new SynchronousQueue<Object>());
		KeyClientRun kcr = new KeyClientRun("dmcrypt", new Object[]{bytes}, wioThreadResult,getResult);
		WioKeyThreadPool.pool.execute(kcr);
	}
	
	public static String dmcryptRun(byte[] bytes,WioKeyThreadResult wioThreadResult){
		Datas.Builder builder = Datas.newBuilder();
		final String id = UUID.randomUUID().toString().replace("-", "");
		builder.setId(id);
		builder.setType("3");
		builder.getDmcryptBuilder().setData(ByteString.copyFrom(bytes));
		BlockingQueue<Object> queue = null;
		if(wioThreadResult == null){
			queue = new SynchronousQueue<Object>();//等待队列
		}else{
			queue = wioThreadResult.getQueue();
		}
		KeyClientHandle.retQueueMap.put(id, queue);
		send(builder, id);
		try{
			String s = (String)queue.poll(3, TimeUnit.SECONDS);
			KeyClientHandle.retQueueMap.remove(id);
			return s;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private static void send(Datas.Builder builder,final String id){
		if(channel!=null && channel.isOpen() && channel.isActive()){
			writerKey(channel, builder, id);
		}
	}
	
	private static boolean writerKey(Channel channel,Datas.Builder builder,final String id){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// TODO Auto-generated method stub
					if(!future.isSuccess()){
						KeyClientHandle.retQueueMap.remove(id);
					}
				}
			});
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == 4000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
						
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							// TODO Auto-generated method stub
							if(!future.isSuccess()){
								KeyClientHandle.retQueueMap.remove(id);
							}
						}
					});
					return true;
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	
	static class KeyClientRun implements Runnable{

		public String runfun = "";
		public Object[] param = null; 
		public WioKeyThreadResult wioThreadResult = null;
		public KeyGetResult getResult = null;
		
		public KeyClientRun(String runfun,Object[] param,WioKeyThreadResult wioThreadResult,KeyGetResult getResult){
			this.runfun = runfun;
			this.param = param;
			this.wioThreadResult = wioThreadResult;
			this.getResult = getResult;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(runfun.equals("emcrypt")){
				byte[] bytes = emcryptRun((String)param[0], wioThreadResult);
				if(getResult!=null){
					getResult.getBytes(bytes);
				}
			}else if(runfun.equals("dmcrypt")){
				String str = dmcryptRun((byte[])param[0], wioThreadResult);
				if(getResult!=null){
					getResult.getString(str);
				}
			}
		}
		
	} 
	
}









