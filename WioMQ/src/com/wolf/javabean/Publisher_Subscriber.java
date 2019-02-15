package com.wolf.javabean;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.db.MQDBImpl;
import com.wolf.javabean.Producer_Consumer.TakeMessage;

public class Publisher_Subscriber {

//	private Publisher_Subscriber(){
//		new Thread(new ListenerMessage()).start();
//	}
	
	private static Map<String, Publisher_Subscriber> Publisher_SubscriberMap = new HashMap<String, Publisher_Subscriber>();
	private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(false);
	
	private String queueName;
	private AtomicBoolean isRun = new AtomicBoolean(false);
	
	//public static Publisher_Subscriber INSTANCE = new Publisher_Subscriber();
	
	public Set<Publisher> producerList = new HashSet<Publisher>();
	public List<Subscriber> subscriberQueue = new ArrayList<Subscriber>();
	public BlockingQueue<TopicMessage> topicMessages = new ArrayBlockingQueue<TopicMessage>(1000);
	
	private void start(){
		if(isRun.get()){
			return;
		}
		isRun.set(true);
		MQThreadFactory.es.execute(new ListenerMessage());
	}
	
	public void isRun(boolean b){//提供给外部
		isRun.set(b);
	}
	
	public static Publisher_Subscriber hasAndGet(String name){
		boolean flag = false;
		try {
			flag = rwLock.readLock().tryLock(5000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			if(flag){
				if(Publisher_SubscriberMap.containsKey(name)){
					Publisher_Subscriber publisher_Subscriber = Publisher_SubscriberMap.get(name);
					return publisher_Subscriber;
				}else{
					return null;
				}
			}else{
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(flag){
				rwLock.readLock().unlock();
			}
		}
	}
	
	public static boolean create(String name){
		boolean flag = false;
		try {
			flag = rwLock.writeLock().tryLock(5000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(flag){
				if(!Publisher_SubscriberMap.containsKey(name)){
					Publisher_Subscriber publisher_Subscriber = new Publisher_Subscriber();
					publisher_Subscriber.setQueueName(name);
					Publisher_SubscriberMap.put(name, publisher_Subscriber);
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(flag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void offerMessage(TopicMessage e,int timeout,TimeUnit unit){
		try {
			if(MQDBImpl.INSTANCE.getIsDB()){
				try {
					MQDBImpl.INSTANCE.saveTopicMessage(e);
				} catch (Exception e1) {
					MQDBImpl.INSTANCE.saveLog("消息ID为:"+e.getId()+",发布者ID为:"+e.getPublisher().getId()+",发布者名字为:"+e.getPublisher().getName()+"消息持久化异常");
				}
			}else{
				topicMessages.offer(e, timeout, unit);
			}
			if(!isRun.get()){
				start();
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public boolean offerMessage(List<TopicMessage> es) {
		boolean b = true;
		try {
			System.out.println("有消息进入"+queueName+"队列");
			for(TopicMessage e : es){
				if(MQDBImpl.INSTANCE.getIsDB()){
					MQDBImpl.INSTANCE.saveTopicMessage(e);
				}else{
					b = topicMessages.add(e);//这一步有隐藏bug，需要在后期连接数据库后做处理
				}
			}
			start();
			return b;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			b = false;
			return b;
		}finally{
			if(!b){
				
			}
		}
	}
	
	public boolean channelunSubscriber(String id){
		Iterator<Subscriber> it = subscriberQueue.iterator();
		boolean b = false;
		while(it.hasNext()){
			Subscriber subscriber = it.next();
			if(subscriber.getId().equals(id)){
				subscriber.unChannel();
				b = true;
				break;
			}
		}
		return b;
	}
	
	public boolean ChannelInSubscriber(Channel channel,String id){//连接挂靠
		Iterator<Subscriber> it = subscriberQueue.iterator();
		boolean b = false;
		while(it.hasNext()){
			Subscriber subscriber = it.next();
			if(subscriber.getId().equals(id)){
				subscriber.setChannel(channel,1);
				b = true;
				break;
			}
		}
		return b;
	}
	
	public void listener(){//异步
		while(isRun.get()){
			try {
				if(!MQDBImpl.INSTANCE.getIsDB()){
					if(topicMessages.peek() == null){
						isRun.set(false);
						break;
					}
					TopicMessage topicMessage = topicMessages.take();
					sendMessage(topicMessage);
				}else{
					try {
						List<TopicMessage> tmList = MQDBImpl.INSTANCE.getTopicMessageList();
						if(tmList.size() == 0){
							isRun.set(false);
							break;
						}
						try {
							//只要查询出来就被认为已经被订阅者接收,这里要注意，不管有没有消费者
							MQDBImpl.INSTANCE.updateTopicMessageStatus(tmList, "1");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sendMessage(tmList);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				listener();
			}
		}
	}
	
	public void sendMessage(TopicMessage topicMessage){
		Iterator<Subscriber> it = subscriberQueue.iterator();
		while(it.hasNext()){
			Subscriber subscriber = it.next();
			subscriber = subscriber.addMessage(topicMessage);
			if(subscriber != null){
				subscriber.sendMessage();
			}
		}
	}
	
	public void sendMessage(List<TopicMessage> topicMessages){//主要是数据库操作用
		Iterator<Subscriber> it = subscriberQueue.iterator();
		while(it.hasNext()){
			Subscriber subscriber = it.next();
			subscriber = subscriber.addMessage(topicMessages);
			if(subscriber != null){
				subscriber.sendMessage();
			}
		}
	}
	
	class ListenerMessage implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			listener();
		}
		
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
}
