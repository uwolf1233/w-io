package com.wolf.center;

import java.util.Map;

import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.HttpSession;
import com.wolf.javabean.SystemNet.Websocketms;
import com.wolf.server.ControllerProxy;
import com.wolf.websocket.WebSocketHandle;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketResponse extends ChannelInboundHandlerAdapter{

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;//接收到的proto
		if(datas.getHandletype().equals("websocketresponse")){
			HttpSession session = datas.getHttpBean().getRequest().getSession();
			String sessionId = session.getSessionId();
			//Map<String,String> sessionMap = session.getSessionMap();
			if(sessionId == null || sessionId.equals("null")){
				return;
			}
//			MySession mySession = SessionMsg.INSTANCE.getSession(sessionId);
//			mySession.setServerName(session.getServerName());
//			mySession.getAttr().putAll(sessionMap);//将session数据放入注册中心的session
			Websocketms websocketms = datas.getHttpBean().getWebsocketms();
			Channel webSocketChannel = WebSocketHandle.channelMap.get(websocketms.getId());
			if(webSocketChannel!=null && webSocketChannel.isActive() && webSocketChannel.isOpen()){
				if(webSocketChannel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
					webSocketChannel.writeAndFlush(new TextWebSocketFrame(websocketms.getSendMessage()));
				}else{
					try {
						int i = 0;
						while(!webSocketChannel.isWritable()){
							Thread.sleep(4);
							if(i == ControllerProxy.requestTimeOut*1000){
								break;
							}
							i++;
						}
						if(webSocketChannel.isWritable()){
							webSocketChannel.writeAndFlush(new TextWebSocketFrame(websocketms.getSendMessage()));
						}else{
							return;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					
				}
			}
		}else{
			ctx.fireChannelRead(datas);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	
	
}
