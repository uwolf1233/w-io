package com.wolf.javabean;

import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.Websocketms;

import io.netty.channel.Channel;

public class WebSocketBean {

	private String path;
	private String inMessage;//进来的消息
	public String getInMessage() {
		return inMessage;
	}

	public void setInMessage(String inMessage) {
		this.inMessage = inMessage;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private boolean sendData = true;//是否发送
	private String id;
	private Channel channel;
	private WebSocketMessage wsMessage;
	
	public boolean isSendData() {
		return sendData;
	}

	public void setSendData(boolean sendData) {
		this.sendData = sendData;
	}

	public WebSocketBean(String path,String id){
		this.path = path;
	}
	
	public void send(WebSocketMessage message){//当前方法由组调用，不能修改，但可以直接调用
		if(sendData){
			//send
			message.send(channel,path,id);
		}
	}
	
	public void send(){
		if(sendData){
			HttpBean.Builder httpBeanBuilder = HttpBean.newBuilder();
			Websocketms.Builder wms = httpBeanBuilder.getWebsocketmsBuilder();
			wms.setPath(path);
			wms.setId(id);
			if(wsMessage.getType().equals("String")){
				wms.setSendMessage(wsMessage.getMessage());
			}
			channel.writeAndFlush(wms);
		}
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WebSocketMessage getWsMessage() {
		return wsMessage;
	}

	public void setWsMessage(WebSocketMessage wsMessage) {
		this.wsMessage = wsMessage;
	}
	
}
