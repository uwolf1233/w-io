package com.wolf.javabean;

import io.netty.channel.Channel;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;

public class Publisher {

	private String id;
	private String name;
	
	public String getId() {
		return id;
	}
	public Publisher setId(String id) {
		this.id = id;return this;
	}
	public String getName() {
		return name;
	}
	public Publisher setName(String name) {
		this.name = name;return this;
	}
	
	public void createMessage(ByteString byteString,String queueName){
		TopicMessage topicMessage = new TopicMessage();
		topicMessage.setByteString(byteString);
		topicMessage.setId(UUID.randomUUID().toString().replace("-", ""));
		topicMessage.setPublisher(this);
		Publisher_Subscriber publisher_Subscriber = Publisher_Subscriber.hasAndGet(queueName);
		if(publisher_Subscriber != null){
			publisher_Subscriber.offerMessage(topicMessage, 5, TimeUnit.SECONDS);
		}else{
			System.out.println("发布者找不到名为:"+queueName+"的队列");
		}
	}
	
	public void createTranmessage(ByteString byteString,String queueName,String id,String transName,Channel channel,String ppid){//创建事务消息
		TranMessage tranMessage = new TranMessage()
			.setId(id)
			.setType("topic")
			.setPublisher(this)
			.setChannel(channel)
			.setReturnWait(false)
			.setQueueName(queueName)
			.setTransName(transName)
			.setByteString(byteString)
			.setPpid(ppid)
			.refresh();
		TranMessageMg.INSTANCE.addTranMessage(tranMessage);
	}
	
}
