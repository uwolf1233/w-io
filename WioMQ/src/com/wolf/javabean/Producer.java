package com.wolf.javabean;

import io.netty.channel.Channel;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;

public class Producer {

	private String id;
	private String name;
	
	public String getId() {
		return id;
	}
	public Producer setId(String id) {
		this.id = id;return this;
	}
	public String getName() {
		return name;
	}
	public Producer setName(String name) {
		this.name = name;return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Producer other = (Producer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public void createMessage(ByteString byteString,String queueName){//创建消息
		QueueMessage queueMessage = new QueueMessage();
		queueMessage.setByteString(byteString);
		queueMessage.setId(UUID.randomUUID().toString().replace("-", ""));
		queueMessage.setProducer(this);
		Producer_Consumer producer_Consumer = Producer_Consumer.hasAndGet(queueName);
		if(producer_Consumer!=null){
			producer_Consumer.offerMessage(queueMessage, 5, TimeUnit.SECONDS);//消息进入消息队列
		}else{
			System.out.println("生产者找不到名为:"+queueName+"的队列");
		}
	}
	
	public void createTranmessage(ByteString byteString,String queueName,String id,String transName,Channel channel,String ppid){//创建事务消息
		TranMessage tranMessage = new TranMessage()
			.setId(id)
			.setType("queue")
			.setProducer(this)
			.setChannel(channel)
			.setReturnWait(false)
			.setQueueName(queueName)
			.setTransName(transName)
			.setByteString(byteString)
			.refresh()
			.setPpid(ppid);
		TranMessageMg.INSTANCE.addTranMessage(tranMessage);
	}
	
}
