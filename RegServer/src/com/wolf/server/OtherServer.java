package com.wolf.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.RegBean;
import com.wolf.javabean.WioThreadPool;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class OtherServer {

    public static ConcurrentMap<String,Map<String,Object>> controllerClass = new ConcurrentHashMap<String,Map<String,Object>>();
    public static String serverName = "";
    public static int threadPoolNum = 0;
    public static ExecutorService pool = null;//业务池
    public static Channel regChannel;
    public static int threadPoolNumMax = 0;
    public static String ip = "";
    public static String port = "";
    public static int groupNum = 0;
    public static String regIp = "";
    public static int regPort = 0;
    
    public static String dburl = "";
    public static String dbusername = "";
    public static String dbuserpass = "";
    public static String cachePrepStmts = "";
    public static String prepStmtCacheSize = "";
    public static String prepStmtCacheSqlLimit = "";
    private NioEventLoopGroup group;
    public static int timeOut = 4;
	
	public void init(){
		if(pool==null){
			pool = Executors.newFixedThreadPool(threadPoolNum);
		}
		Bootstrap boot = new Bootstrap();
		if(group==null){
			group = new NioEventLoopGroup(groupNum);
		}
		try {
			boot.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {
					// TODO Auto-generated method stub
					ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
					ch.pipeline().addLast(new ProtobufDecoder(SystemNet.Datas.getDefaultInstance()));
					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
					ch.pipeline().addLast(new ProtobufEncoder());
					ch.pipeline().addLast(new OtherHandle());//用于判断是否注册成功
					ch.pipeline().addLast(new HttHandle());//用于处理http相关请求
					ch.pipeline().addLast(new ServerChannelHandle());//用于处理服务器内部请求
					ch.pipeline().addLast(new WebSocketHandle());//用于处理websocket请求
				}
				
			});
			boot.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			regChannel = boot.connect(regIp,regPort).sync().channel();
			regs();
			if(regChannel.isActive() && regChannel.isOpen()){
				a = 0;
			}
			regChannel.closeFuture().sync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("连接失败");
		}finally{
			//group.shutdownGracefully();
			line();
		}
	}
	
	public int a = 0;
	
	public void line(){
		try {
			a++;
			if(a>=20){
				group.shutdownGracefully();
				return;//实在连接不上
			}
			Thread.sleep(3000);
			System.out.println("正在尝试第"+a+"次重连...");
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	
	private void regs(){
		Datas.Builder datas = Datas.newBuilder();
		datas.setHandletype("RegBean");
		RegBean.Builder rb = datas.getRegBeanBuilder();
		rb.setId(UUID.randomUUID().toString().replace("-", ""));
		rb.setIp(ip);
		rb.setPort(port);
		rb.setRegtype("1");
		rb.setServerName(serverName);
		rb.setRequestnum(threadPoolNumMax);
		RefController.INSTANCE.controllerSetting(rb);
		datas.setRegBean(rb);
		writeServer(regChannel, datas);//发送服务器数据进行注册
	}
	
	//public static void main(String[] args) {
	public static void mains(InputStream in){
		try {
			//String path = OtherServer.class.getClass().getResource("/").getPath();
			//File file = new File(configpath);
			//path = path.substring(1,path.length());
			if(in!=null){
				SAXReader reader = new SAXReader();
				Document document = reader.read(in);
				Element configE = document.getRootElement();
				ip = configE.element("ip").getText();
				port = configE.element("port").getText();
				serverName = configE.element("serverName").getText();
				threadPoolNum = Integer.parseInt(configE.element("threadPoolNum").getText());
				threadPoolNumMax = Integer.parseInt(configE.element("threadPoolNumMax").getText());
				groupNum = Integer.parseInt(configE.element("groupNum").getText());
				regIp = configE.element("regIp").getText();
				regPort = Integer.parseInt(configE.element("regPort").getText());
				LineTransServer.transIp = configE.element("transIp").getText();
				LineTransServer.transPort = Integer.parseInt(configE.element("transPort").getText());
				LogServer.logIp = configE.element("logIp").getText();
				LogServer.logPort = Integer.parseInt(configE.element("logPort").getText());
				dburl = configE.element("dburl").getText();
				dbusername = configE.element("dbusername").getText();
				dbuserpass = configE.element("dbuserpass").getText();
				cachePrepStmts = configE.element("cachePrepStmts").getText();
				prepStmtCacheSize = configE.element("prepStmtCacheSize").getText();
				prepStmtCacheSqlLimit = configE.element("prepStmtCacheSqlLimit").getText();
				
				SecurityClient.Ip = configE.element("sessionIp").getText();
				SecurityClient.Port = Integer.parseInt(configE.element("sessionPort").getText());
				//ExecutorService pool = WioThreadPool.pool;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("初始化异常");
		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void execServer(){
		new Thread(new LineTransServer()).start();
		new Thread(new LogServer()).start();
		new Thread(new SecurityClient()).start();
		new OtherServer().init();
	}
	
	private boolean writeServer(Channel channel,Datas.Builder datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == OtherServer.timeOut*1000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(datas);
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
	
}
