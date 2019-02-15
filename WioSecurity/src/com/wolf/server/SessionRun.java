package com.wolf.server;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.wolf.javabean.Session.SessionData;
import com.wolf.session.MySession;
import com.wolf.session.SessionMg;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class SessionRun implements Runnable{

	public static int bossNum = 0;
	public static int workerNum = 0;
	public static int port = 0;
	public static String systemtype = "";
	
	public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossNum); 
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerNum);//线程池可以慢慢调试
        try {
            ServerBootstrap b = new ServerBootstrap(); 
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) 
                    .childHandler(new ChannelInitializer<SocketChannel>() { 
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                        	//ch.pipeline().addLast(new HttpTestHandle());
                        	ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));//心跳机制，判断是否掉线
                        	ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
        					ch.pipeline().addLast(new ProtobufDecoder(SessionData.getDefaultInstance()));
        					ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
        					ch.pipeline().addLast(new ProtobufEncoder());
        					ch.pipeline().addLast(new SessionHandle());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 256) //服务端可连接队列
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//保持连接
            System.out.println("server start");
            System.out.println("启动Session服务:"+port);
            ChannelFuture f = b.bind(port).sync(); 
            f.channel().closeFuture().sync();//同步关闭
        } finally {
            workerGroup.shutdownGracefully();//优雅推出netty线程池
            bossGroup.shutdownGracefully();//优雅推出netty线程池
        }
    }
	
	public static void main(String[] args) {
		InputStream in = null;
		try {
			in = SessionRun.class.getResourceAsStream("/WebConfig.xml");
			if(in != null){
				SAXReader reader = new SAXReader();
				Document document = reader.read(in);
				Element configE = document.getRootElement();
				port = Integer.parseInt(configE.element("port").getText());
				Element bossNumE = configE.element("bossNum");
				Element workerNumE = configE.element("workerNum");
				bossNum = Integer.parseInt(bossNumE.getText());
				workerNum = Integer.parseInt(workerNumE.getText());
				Element systemtypeE = configE.element("systemtype");
				systemtype = systemtypeE.getText();
				try {
					Element sessionTimeE = configE.element("sessionTime");
					MySession.sessionTime = Integer.parseInt(sessionTimeE.getText());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Element poolsNumE = configE.element("poolsNum");
					SessionMg.INSTANCE.poolsNum = Integer.parseInt(poolsNumE.getText());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Element poolNumE = configE.element("poolNum");
					SessionMg.INSTANCE.poolNum = Integer.parseInt(poolNumE.getText());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Element sessionScanMinuteE = configE.element("sessionScanMinute");
					SessionMg.INSTANCE.sessionScanMinute = Long.parseLong(sessionScanMinuteE.getText());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			SessionMg.INSTANCE.init(1);
			SessionRun sr = new SessionRun();
			new Thread(sr).start();
			SessionMg.INSTANCE.initScan();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public static List<String> sessionIdList = new ArrayList<String>();
	
//	public void initCreateSession(){
//		new Thread(new CreateSession()).start();
//		new Thread(new GetSession()).start();
//	}
	
//	class CreateSession implements Runnable{//新线程模拟获取session，配合GetSession测试session执行情况
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
//		      
//		    service.scheduleAtFixedRate(new Runnable() {  
//		          
//		        @Override  
//		        public void run() {
//		        	MySession session = SessionMg.INSTANCE.createSession();
//		        	System.out.println("已获取到session,sessionId="+session.getSessionId());
//					try {
//						session.setAttr("123", ByteString.copyFrom("abc", "UTF-8"));
//						sessionIdList.add(session.getSessionId());
//					} catch (UnsupportedEncodingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//		        	
//		        }
//		    }, 10, 15, TimeUnit.SECONDS);
//			
//		}
//	}
	
//	class GetSession implements Runnable{//测试session获取
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
//		      
//		    service.scheduleAtFixedRate(new Runnable() {  
//		          
//		        @Override  
//		        public void run() {
//		        	try {
//						System.out.println("list长度="+sessionIdList.size());
//						Iterator<String> it = sessionIdList.iterator();
//						while(it.hasNext()){
//							String id = it.next();
//							MySession session = SessionMg.INSTANCE.getSession(id);
//							if(session == null){
//								System.out.println("sessionId="+id+"已被回收");
//								return;
//							}
//							System.out.println("获取到的数据="+new String(session.getAttr("123").toByteArray()).toString());
//						}
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//		        	
//		        }
//		    }, 15, 15, TimeUnit.SECONDS);
//		}
//		
//	}
	
}
