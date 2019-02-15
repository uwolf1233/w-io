package com.wolf.javabean;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import com.wolf.Service.SubscriberServer;
import com.wolf.javabean.MQ.MQData;
import com.wolf.listener.MessageListener;

public class Subscriber{

	private String id;
	private String name;
	private Channel channel;
	private MessageListener messageListener;
	private String queueName;
	
	public static ConcurrentMap<String, Subscriber> subscriberMap = new ConcurrentHashMap<String, Subscriber>();
	
	public void putMg(){
		subscriberMap.put(id, this);
	}
	
	public void removeMg(){
		subscriberMap.put(id, this);
	}
	
	public void regSubscriber(String name) throws Exception{
		if(id == null || id.length() == 0){
			id = UUID.randomUUID().toString().replace("-", "");
		}
		this.name = name;
		channel = SubscriberServer.channel;
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("regSubscriber");
		builder.setQueuename(queueName);
		builder.getSubscriberBuilder().setId(id);
		builder.getSubscriberBuilder().setName(name);
		channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if(future.isSuccess()){
					messageListener.reg("success");
				}else{
					messageListener.reg("error");
				}
			}
		});
	}
	
	public void channelInSubscriber(){
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("ChannelInSubscriber");
		builder.getSubscriberBuilder().setId(id);
		builder.getSubscriberBuilder().setName(name);
		channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if(future.isSuccess()){
					messageListener.channelIn("success");
				}else{
					messageListener.channelIn("error");
				}
			}
		});
	}
	
	public void unRegSubscriber(){
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("unRegSubscriber");
		builder.getSubscriberBuilder().setId(id);
		builder.getSubscriberBuilder().setName(name);
		channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if(future.isSuccess()){
					messageListener.unReg("success");
				}else{
					messageListener.unReg("error");
				}
			}
		});
	}
	
	public void subscriberUnchannel(){
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("subscriberUnchannel");
		builder.getSubscriberBuilder().setId(id);
		builder.getSubscriberBuilder().setName(name);
		channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if(future.isSuccess()){
					messageListener.unchannel("success");
				}else{
					messageListener.unchannel("error");
				}
			}
		});
	}
	
	public void returnTopicMessage(Object[] os){
		try {
			messageListener.readMessage(os,id);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Subscriber setChannel(Channel channel) {
		this.channel = channel;return this;
	}

	public MessageListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Channel getChannel() {
		return channel;
	}
	
}
