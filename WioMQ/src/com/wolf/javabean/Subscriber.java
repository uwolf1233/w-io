package com.wolf.javabean;

import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.protobuf.ByteString;
import com.wolf.db.MQDBImpl;
import com.wolf.javabean.MQ.MQData;

public class Subscriber {

	private String id;
	private String name;
	private List<TopicMessage> topicMessageList = new ArrayList<TopicMessage>();
	private Channel channel;
	private ReentrantReadWriteLock wrLock = new ReentrantReadWriteLock(false);
	private AtomicInteger status;
	
	public Object getTopicMessageListData(){
		List<Map<String,Object>> topicmessageLM = new ArrayList<Map<String,Object>>();
		int topicMessageListSize = topicMessageList.size();
		for(int i=0;i<topicMessageListSize;i++){
			Map<String,Object> map = new HashMap<String,Object>();
			TopicMessage topicMessage = topicMessageList.get(i);
			map.put("messageId", topicMessage.getId());
			map.put("publisherId", topicMessage.getPublisher().getId());
			map.put("publisherName", topicMessage.getPublisher().getName());
			topicmessageLM.add(map);
		}
		return topicmessageLM;
	}
	
	public String getId() {
		return id;
	}
	public Subscriber setId(String id) {
		this.id = id;return this;
	}
	public String getName() {
		return name;
	}
	public Subscriber setName(String name) {
		this.name = name;return this;
	}
	
	public void unChannel(){
		boolean b = false;
		try {
			b = wrLock.writeLock().tryLock(3,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			channel = null;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(b){
				wrLock.writeLock().unlock();
			}
		}
	}
	
	public Subscriber addMessage(TopicMessage tm){
		boolean flag = false;
		try {
			flag = wrLock.writeLock().tryLock(3,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			if(flag){
				System.out.println(tm.getId()+"获取数据");
				topicMessageList.add(tm);return this;
			}
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(flag){
				wrLock.writeLock().unlock();
			}
		}	
	}
	
	public Subscriber addMessage(List<TopicMessage> tms){
		boolean flag = false;
		try {
			flag = wrLock.writeLock().tryLock(3,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			if(flag){
				List<String> list = new ArrayList<String>();
				for(TopicMessage tm : tms){
					System.out.println(tm.getId()+"获取数据");
					list.add(tm.getId());
				}
				MQDBImpl.INSTANCE.saveSubscriberTopicMessage(id, list, "0");
				return this;
			}
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			if(flag){
				wrLock.writeLock().unlock();
			}
		}	
	}
	
	public void sendMessage(){
		boolean flag = false;
		try{
			flag = wrLock.writeLock().tryLock(3,TimeUnit.SECONDS);
		}catch(InterruptedException e){
			e.printStackTrace();
			return;
		}
		try {
			if(flag){
				if(channel == null || !channel.isOpen() || !channel.isActive()){
					return;
				}
				List<TopicMessage> topicMessageLists = null;
				if(!MQDBImpl.INSTANCE.getIsDB()){
					topicMessageLists = topicMessageList;
				}else{
					topicMessageLists = MQDBImpl.INSTANCE.getSubscriberTopicMessage(id, "0");
				}
				if(topicMessageLists != null && topicMessageLists.size()>0 && channel != null && channel.isOpen() && channel.isActive()){
					MQData.Builder builder = MQData.newBuilder();
					for(TopicMessage ctopicMessage : topicMessageLists){
						if(ctopicMessage != null && ctopicMessage.getByteString() != null && ctopicMessage.getByteString().size() > 0){
							builder.addData(ctopicMessage.getByteString());
						}
					}
					builder.setType("returnTopicMessage");
					builder.getSubscriberBuilder().setId(id);
					builder.getSubscriberBuilder().setName(name);
					if(channel != null && channel.isOpen() && channel.isActive()){
						channel.writeAndFlush(builder);
					}else{
						builder.clear();
						builder = null;
					}
					if(MQDBImpl.INSTANCE.getIsDB()){
						StringBuilder builders = new StringBuilder("");
						int i = 0;
						for(TopicMessage topicMessage : topicMessageLists){
							builders.append(i == 0 ? ("'"+topicMessage.getId()+"'") : (",'"+topicMessage.getId()+"'"));
							i++;
						}
						MQDBImpl.INSTANCE.updateSubscriberTopicMessage(builders.toString());
					}
					topicMessageList.clear();
					topicMessageLists.clear();
					topicMessageLists = null;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(flag){
				wrLock.writeLock().unlock();
			}
		}
	}
	public Channel getChannel() {
		return channel;
	}
	public Subscriber setChannel(Channel channel,int status) {//status判断是否初始化，0为注册，1为重连
		if(channel != null && channel.isOpen() && channel.isActive()){
			this.channel = channel;
			if(status == 1){
				sendMessage();
			}
		}
		return this;
	}
	public AtomicInteger getStatus() {
		return status;
	}
	public Subscriber setStatus(AtomicInteger status) {
		this.status = status;return this;
	}
	
}
