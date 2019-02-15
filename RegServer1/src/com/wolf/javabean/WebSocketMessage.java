package com.wolf.javabean;

import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.Websocketms;

import io.netty.channel.Channel;

public class WebSocketMessage {

	private String type;
	private String message;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void send(Channel channel,String path,String id){//发送时，判断发送的数据类型
		if(type.equals("String")){
			//message
			Datas.Builder datasBuilder = Datas.newBuilder();
			HttpBean.Builder httpBeanBuilder = datasBuilder.getHttpBeanBuilder();
			Websocketms.Builder wms = httpBeanBuilder.getWebsocketmsBuilder();
			datasBuilder.setHandletype("websocketresponse");
			wms.setPath(path);
			wms.setId(id);
			wms.setSendMessage(message);
			channel.writeAndFlush(datasBuilder);
		}
	}
	
}
