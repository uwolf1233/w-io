package com.wolf.javabean;

import java.util.List;
import java.util.UUID;

import com.wolf.listener.DefaultProducerListener;
import com.wolf.listener.UserListener;

public class UsersBean {

	private String id;
	private String name;
	private Consumer consumer;
	private Producer producer;
	private String status;
	
	public UsersBean(String id,String name){
		try {
			this.id = id;
			this.name = name;
			consumer = new Consumer();
			consumer.setId(id);
			UserListener userListener = new UserListener();
			userListener.usersBean = this;
			consumer.setMessageListener(userListener);
			consumer.regConsumer("abc");
			consumer.putMg();
			producer = new Producer();
			producer.setId(id);
			producer.setName(name);
			producer.setMessageListener(new DefaultProducerListener());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendString(String message){
		producer.UDPSendMessage(message);
	}
	
	public void returnShow(List<Object> oList){
		//
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}












 
