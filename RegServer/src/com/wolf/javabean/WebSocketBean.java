package com.wolf.javabean;

import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.Websocketms;
import com.wolf.server.OtherServer;

import io.netty.channel.Channel;

public class WebSocketBean {

	private String path;
	private String inMessage;//进来的消息
	
	private WebSocketBeanFilterI webSocketBeanFilterI;
	
	public void setWebSocketBeanFilterI(WebSocketBeanFilterI webSocketBeanFilterI){
		this.webSocketBeanFilterI = webSocketBeanFilterI;
	}
	
	public void close(){
		if(webSocketBeanFilterI == null){
			return;
		}
		webSocketBeanFilterI.close();
	}
	
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
		this.id = id;
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
			writeWebsocket(channel, wms);
		}
	}
	
	private boolean writeWebsocket(Channel channel,Websocketms.Builder wms){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(wms);
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
					channel.writeAndFlush(wms);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebSocketBean other = (WebSocketBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
}
