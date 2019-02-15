package com.wolf.javabean;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.protobuf.ByteString;
import com.wolf.db.MQDBImpl;
import com.wolf.javabean.MQ.MQData;

import io.netty.channel.Channel;

public class Consumer {

	private String id;
	private String name;
	private List<QueueMessage> queueMessageList = new ArrayList<QueueMessage>();
	private Channel channel;
	private ReentrantReadWriteLock wrLock = new ReentrantReadWriteLock(false);
	private AtomicInteger status;//-1为关闭，1为可用
	private AtomicInteger handleStatus = new AtomicInteger(0);//0没有任何操作，1为正在操作
	//private BlockingQueue<QueueMessage> queueMessages;//由Producer_Consumer设置
	
	public Object getQueueMessageListData(){
		List<Map<String,Object>> queuemessageLM = new ArrayList<Map<String,Object>>();
		int queueMessageListSize = queueMessageList.size();
		for(int i=0;i<queueMessageListSize;i++){
			Map<String,Object> map = new HashMap<String,Object>();
			QueueMessage queueMessage = queueMessageList.get(i);
			map.put("messageId", queueMessage.getId());
			map.put("producerId", queueMessage.getProducer().getId());
			map.put("producerName", queueMessage.getProducer().getName());
			queuemessageLM.add(map);
		}
		return queuemessageLM;
	}
	
	public String getId() {
		return id;
	}
	public Consumer setId(String id) {
		this.id = id;return this;
	}
	public String getName() {
		return name;
	}
	public Consumer setName(String name) {
		this.name = name;return this;
	}
	
	public Consumer setChannel(Channel channel,int status){//status判断是否初始化，0为注册，1为重连
		if(channel != null && channel.isOpen() && channel.isActive()){
			this.channel = channel;
			if(status == 1){
				getMessage();
			}
		}
		return this;
	}
	
	public void unChannel(){
		boolean b = false;
		try{
			b = wrLock.writeLock().tryLock(10,TimeUnit.SECONDS);
		}catch(InterruptedException e){
			e.printStackTrace();
			return;
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
	
	public Consumer listenerMessage(Producer_Consumer p_c){
		handleStatus.set(1);//进入操作
		System.out.println(id+"进入监听");
		if(status.get() == -1){
			return null;
		}
		if(!MQDBImpl.INSTANCE.getIsDB()){
			try {
				if(p_c.queueMessages.peek() == null){
					System.out.println("没有消息，停止监听");
					p_c.isRun(false);
					return null;
				}
				QueueMessage qm = p_c.queueMessages.take();
				if(qm.getId() == null){//可能是虚假信息
					if(status.get() != -1){//判断是否关闭
						return listenerMessage(p_c);//如果不是当前的消费者关闭，继续监听,但本消息需要废弃，因为非当前监听的消费者会后面判断status
					}
				}
				addMessage(qm);
				return this;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}else{//如果是查询数据库
			try {
				List<QueueMessage> list = MQDBImpl.INSTANCE.getQueueMessageList();
				if(list.size() > 0){
					for(QueueMessage qm : list){
						if(qm.getId() == null || qm.getId() == null || qm.getId().trim().equals("")){//可能是虚假信息
							if(status.get() == -1){//判断是否关闭
								//listenerMessage(p_c);//如果不是当前的消费者关闭，继续监听
								System.out.println("已经关闭的消费者");
								break;
							}
						}
					}
					if(status.get() == -1){
						System.out.println("已经关闭的消费者");
						return null;
					}
					MQDBImpl.INSTANCE.updateQueueMessageStatus(list, "1");
					addMessage(list);//如果不出意外，消息全部加入待发送队列
					System.out.println("queue消息已经进入消费者");
					return this;
				}else{
					p_c.isRun(false);
					return null;
				}
			} catch (Exception e) {
				MQDBImpl.INSTANCE.saveLog("数据库查询生产消息异常",e);
				return null;
			}
		}
	}
	
	private void addMessage(QueueMessage qm){
		try {
			wrLock.writeLock().tryLock(3,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			if(!MQDBImpl.INSTANCE.getIsDB()){
				queueMessageList.add(qm);
			}else{
				MQDBImpl.INSTANCE.saveConsumerQueueMessage(id, qm.getId(), "0");
			}
			System.out.println(id+"--加入消息，当前消息数"+queueMessageList.size());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			wrLock.writeLock().unlock();
		}	
	}
	
	private void addMessage(List<QueueMessage> qms){
		try {
			wrLock.writeLock().tryLock(3,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			if(!MQDBImpl.INSTANCE.getIsDB()){
				queueMessageList.addAll(qms);
			}else{
				List<String> queuemessageids = new ArrayList<String>();
				for(QueueMessage queueMessage : qms){
					queuemessageids.add(queueMessage.getId());
				}
				MQDBImpl.INSTANCE.saveConsumerQueueMessage(id, queuemessageids, "0");
			}
			System.out.println(id+"--加入消息，当前消息数"+queueMessageList.size());
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			wrLock.writeLock().unlock();
		}	
	}
	
	public void getMessage(){
		try{
			wrLock.writeLock().tryLock(10,TimeUnit.SECONDS);
		}catch(InterruptedException e){
			e.printStackTrace();
			return;
		}
		try {
			List<QueueMessage> queueMessageLists = null;
			if(!MQDBImpl.INSTANCE.getIsDB()){
				queueMessageLists = queueMessageList;
			}else{
				queueMessageLists = MQDBImpl.INSTANCE.getConsumerQueueMessage(id, "0");
			}
			if(queueMessageLists!=null && queueMessageLists.size()>0 && channel != null && channel.isOpen() && channel.isActive()){
				MQData.Builder builder = MQData.newBuilder();
				for(QueueMessage queueMessage : queueMessageLists){
					if(queueMessage != null && queueMessage.getByteString() != null && queueMessage.getByteString().size() > 0){
						builder.addData(queueMessage.getByteString());
					}
				}
				builder.setType("returnQueueMessage");
				builder.getConsumerBuilder().setId(id);
				builder.getConsumerBuilder().setName(name);
				if(channel != null && channel.isOpen() && channel.isActive()){
					channel.writeAndFlush(builder);
				}else{
					builder.clear();
					builder = null;
				}
				if(MQDBImpl.INSTANCE.getIsDB()){
					StringBuilder builders = new StringBuilder("");
					int i = 0;
					for(QueueMessage queueMessage : queueMessageLists){
						builders.append(i == 0 ? ("'"+queueMessage.getId()+"'") : (",'"+queueMessage.getId()+"'"));
						i++;
					}
					MQDBImpl.INSTANCE.updateConsumerQueueMessage(builders.toString());
				}
				queueMessageList.clear();
				queueMessageLists.clear();
				queueMessageLists = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			wrLock.writeLock().unlock();
		}
	}
	public AtomicInteger getStatus() {
		return status;
	}
	public Consumer setStatus(AtomicInteger status) {
		this.status = status;return this;
	}
	public AtomicInteger getHandleStatus() {
		return handleStatus;
	}
	public void setHandleStatus(AtomicInteger handleStatus) {
		this.handleStatus = handleStatus;
	}
	public Channel getChannel() {
		return channel;
	}
	
}
