package com.wolf.service;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Producer;
import com.wolf.javabean.Producer_Consumer;
import com.wolf.javabean.Publisher;
import com.wolf.javabean.Publisher_Subscriber;
import com.wolf.javabean.TranMessageMg;

import io.netty.channel.Channel;

public class AllInit {
	
	private AllInit(){};
	
	public static AllInit INSTANCE = new AllInit();

	public boolean createQueue(String queueName){
		return Producer_Consumer.create(queueName);
	}
	
	public boolean createTopic(String queueName){
		return Publisher_Subscriber.create(queueName);
	}
	
	public int regConsumer(Channel channel,String id,String name,String queueName){//注册消费者
		if(ChannelInConsumer(channel, id,queueName)){
			return 1;//如果消费者能连线
		}
		RegConsumer.INSTANCE.reg(channel, id, name,queueName);
		return 0;
	}
	
	public int regSubscriber(Channel channel,String id,String name,String queueName){//注册订阅者
		if(ChannelInSubscriber(channel, id, queueName)){
			return 1;//如果订阅者能连线
		}
		System.out.println(name+"注册在"+queueName);
		RegSubscriber.INSTANCE.reg(channel, id, name, queueName);
		return 0;
	}
	
	public boolean ChannelInConsumer(Channel channel,String id,String queueName){//消费者连线
		Producer_Consumer producer_Consumer = Producer_Consumer.hasAndGet(queueName);
		if(producer_Consumer == null){
			return false;
		}
		return producer_Consumer.ChannelInConsumer(channel, id);
	}
	
	public boolean ChannelInSubscriber(Channel channel,String id,String queueName){//订阅者连线
		Publisher_Subscriber publisher_Subscriber = Publisher_Subscriber.hasAndGet(queueName);
		if(publisher_Subscriber == null){
			return false;
		}
		return publisher_Subscriber.ChannelInSubscriber(channel, id);
	}
	
	public void unRegConsumer(String id,String queueName){//消费者脱离注册
		RegConsumer.INSTANCE.unReg(id,queueName);
	}
	
	public void unRegSubscriber(String id,String queueName){//订阅者脱离注册
		RegSubscriber.INSTANCE.unReg(id,queueName);
	}
	
	public void channelUnConsumer(String id,String queueName){//消费者离线，但不脱离注册
		Producer_Consumer producer_Consumer = Producer_Consumer.hasAndGet(queueName);
		if(producer_Consumer == null){
			return;
		}
		producer_Consumer.channelUnConsumer(id);
	}
	
	public void channelUnSubscriber(String id,String queueName){
		Publisher_Subscriber publisher_Subscriber = Publisher_Subscriber.hasAndGet(queueName);
		if(publisher_Subscriber == null){
			return;
		}
		publisher_Subscriber.channelunSubscriber(id);
	}
	
	public void sendQueueMessage(String id,String name,ByteString byteString,String queueName){//生产数据
		Producer producer = new Producer()//生产者
			.setId(id)
			.setName(name);
		producer.createMessage(byteString,queueName);
	}
	
	public void sendQueueTranMessage(String id,String messageId,String name,ByteString byteString,
			String queueName,String transName,Channel channel){//生产数据
		Producer producer = new Producer()//生产者
			.setId(id)
			.setName(name);
		producer.createTranmessage(byteString, queueName, messageId, transName, channel,id);
	}
	
	public void commitQueueTranMessage(String transName,String queueName){
		TranMessageMg.INSTANCE.commitQueue(transName, queueName);
	}
	
	public void rollbackQueueTranMessage(String transName,String queueName){
		TranMessageMg.INSTANCE.rollbackQueue(transName, queueName);
	}
	
	public void sendTopicMessage(String id,String name,ByteString byteString,String queueName){//发布数据
		Publisher publisher = new Publisher()//发布者
			.setId(id)
			.setName(name);
		publisher.createMessage(byteString,queueName);
	}
	
	public void sendTopicTranMessage(String id,String messageId,String name,ByteString byteString,
			String queueName,String transName,Channel channel){//生产数据
		Publisher publisher = new Publisher()//发布者
			.setId(id)
			.setName(name);
		publisher.createTranmessage(byteString, queueName, messageId, transName, channel,id);
	}
	
	public void commitTopicTranMessage(String transName,String queueName){
		TranMessageMg.INSTANCE.commitTopic(transName, queueName);
	}
	
	public void rollbackTopicTranMessage(String transName,String queueName){
		TranMessageMg.INSTANCE.rollbackTopic(transName, queueName);
	}
	
	public void queueTranLine(Channel channel,String ppid){
		TranMessageMg.INSTANCE.tranLine("queue", channel, ppid);
	}
	
	public void topicTranLine(Channel channel,String ppid){
		TranMessageMg.INSTANCE.tranLine("topic", channel, ppid);
	}
	
}
