package com.wolf.service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.wolf.javabean.Publisher_Subscriber;
import com.wolf.javabean.Subscriber;

import io.netty.channel.Channel;

public class RegSubscriber {

	private RegSubscriber(){}
	
	public static RegSubscriber INSTANCE = new RegSubscriber();
	
	public void reg(Channel channel,String id,String name,String queueName){
		Subscriber subscriber = new Subscriber()
			.setId(id)
			.setName(name)
			.setChannel(channel,0)
			.setStatus(new AtomicInteger(1));
		Publisher_Subscriber publisher_Subscriber = Publisher_Subscriber.hasAndGet(queueName);
		if(publisher_Subscriber == null){
			return;
		}
		publisher_Subscriber.subscriberQueue.add(subscriber);
	}
	
	public void unReg(String id,String queueName){
		Publisher_Subscriber publisher_Subscriber = Publisher_Subscriber.hasAndGet(queueName);
		List<Subscriber> subscriberQueue = publisher_Subscriber.subscriberQueue;
		Iterator<Subscriber> it = subscriberQueue.iterator();
		while(it.hasNext()){
			Subscriber subscriber = it.next();
			if(subscriber.getId().equals(id)){
				subscriber.getStatus().set(-1);
				it.remove();
				subscriber.setChannel(null,0);
			}
		}
	}
	
}
