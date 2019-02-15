package com.wolf.javabean;

import com.wolf.server.ControllerProxy;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketBean {
	
	private String id;
	private Channel httpChannel;//这是和前端连接的channel，内部的channel需要去获取
	private String path;//路径，可切换
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public void sendMessage(String message,Channel channel){//这个channel是通过负载均衡获取的channel
		if(channel == null){
			TextWebSocketFrame twf = new TextWebSocketFrame("503服务超载");
			writer(channel, twf);
			return;
		}
		TextWebSocketFrame twf = new TextWebSocketFrame(message);
		writer(channel, twf);
	}
	public Channel getHttpChannel() {
		return httpChannel;
	}
	public void setHttpChannel(Channel httpChannel) {
		this.httpChannel = httpChannel;
	}
	
	private boolean writer(Channel channel,TextWebSocketFrame twf){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(twf);
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
					channel.writeAndFlush(twf);
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
