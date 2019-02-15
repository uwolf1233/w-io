package com.wolf.javabean;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.Service.ConsumerServer;
import com.wolf.javabean.MQ.MQData;
import com.wolf.listener.MessageListener;

public class Consumer {
	private String id;
	private String name;
	private Channel channel;
	private String queueName;
	private MessageListener messageListener;
	
	public static ConcurrentMap<String,Consumer> consumerMap = new ConcurrentHashMap<String, Consumer>();
	
	public void putMg(){
		consumerMap.put(id, this);
	}
	
	public void removeMg(){
		consumerMap.remove(id);
	}
	
	public void regConsumer(String name) throws Exception{
		if(id == null || id.length() == 0){
			id = UUID.randomUUID().toString().replace("-", "");
		}
		this.name = name;
		channel = ConsumerServer.channel;
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("regConsumer");
		builder.setQueuename(queueName);
		builder.getConsumerBuilder().setId(id);
		builder.getConsumerBuilder().setName(name);
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
	
	public void channelInConsumer(){
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("ChannelInConsumer");
		builder.getConsumerBuilder().setId(id);
		builder.getConsumerBuilder().setName(name);
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
	
	public void unRegConsumer(){
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("unRegConsumer");
		builder.getConsumerBuilder().setId(id);
		builder.getConsumerBuilder().setName(name);
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
	
	public void consumerUnchannel(){
		if(channel == null || !channel.isOpen() || !channel.isActive()){
			throw new RuntimeException("channel失效");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("consumerUnchannel");
		builder.getConsumerBuilder().setId(id);
		builder.getConsumerBuilder().setName(name);
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
	
	public void returnQueueMessage(Object[] os){
		try {
			messageListener.readMessage(os,id);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Consumer setChannel(Channel channel) {
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
}
