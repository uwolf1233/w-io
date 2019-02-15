package com.wolf.websocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.reflect.TypeToken;
import com.wolf.ChannelWriter.LogWriter;
import com.wolf.center.CenterHandle;
import com.wolf.center.CenterServer;
import com.wolf.channel.LoadStrategyImpl;
import com.wolf.javabean.GsonBean;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.LogBean.Logs;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.ServerBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.Websocketms;
import com.wolf.javabean.WebSocketBean;
import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.log.LogServer;
import com.wolf.server.ControllerProxy;
import com.wolf.server.HttpServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

//WebSocket的第一次请求是http，所以得转为httprequest
public class WebSocketHandle extends ChannelInboundHandlerAdapter{

	private WebSocketServerHandshaker handshaker;
	public static ConcurrentMap<String,Channel> channelMap = new ConcurrentHashMap<String, Channel>();
	public LogsBean logsBean = null;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		Channel channel = ctx.channel();
		channelMap.put(uuid, channel);
		AttributeKey<WebSocketBean> channelIdAttr = AttributeKey.valueOf("websocketBean");
		WebSocketBean websocketBean = new WebSocketBean();
		websocketBean.setId(uuid);
		websocketBean.setHttpChannel(channel);
		channel.attr(channelIdAttr).set(websocketBean);
		
		String mainId = UUID.randomUUID().toString().replace("-", "");
		logsBean = new LogsBean(mainId, "", "", "", "Websocket", HttpServer.ip);
		LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("客户端连接进来", "1",true);
		LogWriter.logWriter.write(logDatasBuilder);
		//super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("close websocket");
		AttributeKey<WebSocketBean> channelIdAttr = AttributeKey.valueOf("websocketBean");
		WebSocketBean wsb = ctx.channel().attr(channelIdAttr).get();
		Datas.Builder datasBuilder = Datas.newBuilder();
		Websocketms.Builder websocketmsBuilder = datasBuilder.getHttpBeanBuilder().getWebsocketmsBuilder();
		websocketmsBuilder.setId(wsb.getId());
		final String path = wsb.getPath();
		websocketmsBuilder.setPath(path);
		websocketmsBuilder.setType("close");
		ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
		ServerBean serverBean = serverMap.get(wsb.getPath());
		List<CenterHandle> list = serverBean.getChList();
		AtomicInteger[] aiArray = new AtomicInteger[1];
		LineLog.Builder lineLogBuilder = datasBuilder.getLineLogBuilder();
		lineLogBuilder.setMainId(logsBean.getMainId());
		lineLogBuilder.setParentId("");
		lineLogBuilder.setId("");
		Channel channel = LoadStrategyImpl.INSTANCE.getServerChannel(list, path,aiArray,lineLogBuilder,0);
		datasBuilder.setHandletype("websocket");
		//channel.writeAndFlush(datasBuilder);
		writeWwbSocket(channel, datasBuilder);
		if(aiArray[0]!=null){
			int cur = aiArray[0].incrementAndGet();
			System.out.println("websocket已还原负载,当前请求的"+path+"负载为"+cur);
		}
		System.out.println("proxy端id为"+wsb.getId()+"路径为"+path+"的websocket连接关闭");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if(msg instanceof HttpRequest){
				HttpRequest request = (HttpRequest)msg;
				System.out.println(HttpUtil.isKeepAlive(request));
				if(!request.decoderResult().isSuccess() 
						|| !"websocket".equals(request.headers().get("Upgrade"))){
					ControllerProxy.INSTANCE().sendData(null, BAD_REQUEST+"", ctx.channel(), null);
					return;//如果http解析错误
				}
				WebSocketServerHandshakerFactory wsFactory = 
						new WebSocketServerHandshakerFactory("ws//localhost:5678/", null, false);//这个地址写什么无所谓，ws不受这里控制
				handshaker = wsFactory.newHandshaker(request);
				if(handshaker == null){
					WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
				}else{
					handshaker.handshake(ctx.channel(),request);
				}
				
			}else if(msg instanceof WebSocketFrame){
				handleWebSocketFrame(ctx, (WebSocketFrame)msg);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	//处理websocket各种消息
	private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
		try{
			if(frame instanceof TextWebSocketFrame){//是否text消息
				String request = ((TextWebSocketFrame)frame).text();
				System.out.println("得到的消息"+request);
				Map<String,Object> jsonMap = GsonBean.gson.fromJson(request, new TypeToken<Map<String,Object>>(){}.getType());
				Datas.Builder datasBuilder = Datas.newBuilder();
				LineLog.Builder lineLogBuilder = datasBuilder.getLineLogBuilder();
				lineLogBuilder.setMainId(logsBean.getMainId());

				LogDatas.Builder LogDatasBuilder = logsBean.getLogDatasBuilder("websocket接收到Text消息", "1",false);
				LogWriter.logWriter.write(LogDatasBuilder);//发送到日志服务器
				
				lineLogBuilder.setParentId(logsBean.getParentId());
				lineLogBuilder.setId(logsBean.getId());
				if(jsonMap.containsKey("sessionId")){
					String sessionId = jsonMap.get("sessionId")+"";
					//MySession session = SessionMsg.INSTANCE.getSession(sessionId);//获取session
					if(sessionId == null || sessionId.trim().equals("") || sessionId.equals("null")){
						SessionLine sessionLine = new SessionLine();
						sessionLine.create(null);
						sessionId = sessionLine.getSessionId();
					}
//					SessionLine sc = new SessionLine();
//					sc.setSessionId(sessionId);
//					Object o = sc.hasSession();
					//if(session!=null&&!session.isUnUse()){
//					if(o!=null&&o.equals("success")){
//						datasBuilder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(sessionId);
//						//datasBuilder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().putAllSession(session.getAttr());
//						//session.setIsusing(true);
//					}else{
//						ctx.channel().writeAndFlush(new TextWebSocketFrame("session is not found"));
//						LogDatas.Builder LogDatasBuilderNoSession = logsBean.getLogDatasBuilder("websocket session is not found", "1",false);
//						LogServer.logChannel.writeAndFlush(LogDatasBuilderNoSession);//发送到日志服务器
//						return;
//					}
					datasBuilder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(sessionId);
				}
				String path = jsonMap.get("path")+"";
				String message = jsonMap.get("message")+"";
				String type = jsonMap.get("type")+"";//如果有关闭指令，则为逻辑关闭
				
//				if(getProxyHsConfig(type, path, ctx.channel())){
//					return;
//				}
				
				AttributeKey<WebSocketBean> channelIdAttr = AttributeKey.valueOf("websocketBean");
				WebSocketBean wsb = ctx.channel().attr(channelIdAttr).get();
				if(type.equals("message")){//如果是message类型，就从已有的websocketbean里面获取路径，前端可以不需要传递路径,websocket暂时不支持入口锁，可以由业务服务调用进行锁操作
					path = wsb.getPath();
				}else if(type.equals("open")){
					wsb.setPath(path);
				}else if(type.equals("close")){
					ctx.close();
					return;
				}
				ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
				if(!serverMap.containsKey(path)){
					writeWwbSocket(ctx.channel(),new TextWebSocketFrame("path not found"));
					return;
				}
				ServerBean serverBean = serverMap.get(path);
				if(!ControllerProxy.sendType("w", path,serverBean)){
					writeWwbSocket(ctx.channel(),new TextWebSocketFrame("路径不是websocket类型，不允许请求"));
					return;//server请求的路径如果不是server请求类型，则不允许进入
				}
				final String pathfinal = path;
				
				HttpBean.Builder httpBeanBuilder = datasBuilder.getHttpBeanBuilder();
				datasBuilder.setHandletype("websocket");//设置为websocket类型
				httpBeanBuilder.setIsws(true);
				Websocketms.Builder websocketms = httpBeanBuilder.getWebsocketmsBuilder();
				websocketms.setPath(path);
				websocketms.setSendMessage(message);
				
				websocketms.setId(wsb.getId());
				websocketms.setType(type);
				
				List<CenterHandle> list = serverBean.getChList();
				AtomicInteger[] aiArray = new AtomicInteger[1];
				Channel channel = LoadStrategyImpl.INSTANCE.getServerChannel(list, path,aiArray,lineLogBuilder,0);
				writeWwbSocket(channel, datasBuilder);
				if(aiArray[0] != null){
					int cur = aiArray[0].incrementAndGet();
					System.out.println("websocket已还原负载,当前请求的"+path+"负载为"+cur);
				}
			}else if(frame instanceof CloseWebSocketFrame){//是否关闭指令
				handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
			}else if(frame instanceof PingWebSocketFrame){//ping消息
				ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			}else if(frame instanceof BinaryWebSocketFrame){//二进制消息
				
			}
		}catch(Exception e){
			e.printStackTrace();
			writeWwbSocket(ctx.channel(),new TextWebSocketFrame("500 error"));
			throw new RuntimeException(e);
		}
	}
	
	private boolean writeWwbSocket(Channel channel,SystemNet.Datas.Builder datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == ControllerProxy.requestTimeOut*1000){
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
	
	private boolean writeWwbSocket(Channel channel,TextWebSocketFrame datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == ControllerProxy.requestTimeOut*1000){
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
