package com.wolf.service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.wolf.javabean.Consumer;
import com.wolf.javabean.Producer_Consumer;
import com.wolf.javabean.QueueMessage;

import io.netty.channel.Channel;

public class RegConsumer {

	private RegConsumer(){}
	
	public static RegConsumer INSTANCE = new RegConsumer();
	
	public void reg(Channel channel,String id,String name,String queueName){//异步
		Consumer consumer = new Consumer()
			.setId(id)
			.setName(name)
			.setChannel(channel,0)
			.setStatus(new AtomicInteger(1));
		try{
			Producer_Consumer producer_Consumer = Producer_Consumer.hasAndGet(queueName);
			if(producer_Consumer == null){
				return;
			}
			producer_Consumer.addConsumer(0,consumer);//添加消费
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void unReg(String id,String queueName){
		try {
			Producer_Consumer producer_Consumer = Producer_Consumer.hasAndGet(queueName);
			if(producer_Consumer == null){
				return;
			}
			Consumer consumer = producer_Consumer.getConsumerFromId(id);
			if(consumer!=null){
				consumer.getStatus().set(-1);//先设置为关闭再发虚假信息
				if(consumer.getHandleStatus().get() == 1){//如果是正在操作
					producer_Consumer.offerMessage(new QueueMessage(), 1, TimeUnit.SECONDS);//发送一条虚假信息让它通过
				}
				producer_Consumer.remove(consumer);//尝试删除
				consumer.setChannel(null,0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
