package com.wolf.javabean;

import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.Websocketms;
import com.wolf.server.OtherServer;

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
			writeSocket(channel, datasBuilder);
		}
	}
	
	private boolean writeSocket(Channel channel,Datas.Builder datasBuilder){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datasBuilder);
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
					channel.writeAndFlush(datasBuilder);
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
