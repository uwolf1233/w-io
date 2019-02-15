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

public class Producer_Consumer {

	private static Map<String, Producer_Consumer> Producer_ConsumerMap = new HashMap<String, Producer_Consumer>();
	private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(false);
	
	private String queueName;
	private AtomicBoolean isRun = new AtomicBoolean(false);
	
	private void start(){
		if(isRun.get()){
			return;
		}
		isRun.set(true);
		MQThreadFactory.es.execute(new TakeMessage());
	}
	
	public void isRun(boolean b){//提供给外部
		isRun.set(b);
	}
	
	public static Producer_Consumer hasAndGet(String name){
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
				if(Producer_ConsumerMap.containsKey(name)){
					Producer_Consumer producer_Consumer = Producer_ConsumerMap.get(name);
					return producer_Consumer;
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
				if(!Producer_ConsumerMap.containsKey(name)){
					Producer_Consumer producer_Consumer = new Producer_Consumer();
					producer_Consumer.setQueueName(name);
					Producer_ConsumerMap.put(name, producer_Consumer);
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
	
	public static int consumerQueueNum = 1000;//默认1000
	
	public Set<Producer> producerList = new HashSet<Producer>();
	public ReentrantReadWriteLock consumerQueueLocks = new ReentrantReadWriteLock(false);
	public Consumer[] consumerQueue = new Consumer[consumerQueueNum];
	public int consumerQueueCount = 0;
	public BlockingQueue<QueueMessage> queueMessages = new ArrayBlockingQueue<QueueMessage>(1000);
	
	public boolean channelUnConsumer(String id){//连接断开，但还在注册中
		Consumer consumer = getConsumerFromId(id);
		consumer.unChannel();
		System.out.println(consumer.getId()+"的消费者断线");
		return true;
	}
	
	public boolean ChannelInConsumer(Channel channel,String id){//连接挂靠
		Consumer consumer = getConsumerFromId(id);
		if(consumer == null){
			return false;
		}
		if(consumer.getStatus().get() == 1){
			consumer.setChannel(channel, 1);
			consumer.getMessage();
		}
		return true;
	}
	
	public void offerMessage(QueueMessage e,int timeout,TimeUnit unit){
		try {
			System.out.println("有消息进入"+queueName+"队列");
			if(MQDBImpl.INSTANCE.getIsDB()){
				try {
					MQDBImpl.INSTANCE.saveQueueMessage(e);
				} catch (Exception e1) {
					MQDBImpl.INSTANCE.saveLog("消息ID为:"+e.getId()+",生产者ID为:"+e.getProducer().getId()+",生产者名字为:"+e.getProducer().getName()+"消息持久化异常");
					return;
				}
			}else{
				queueMessages.offer(e, timeout, unit);
			}
			start();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public boolean offerMessage(List<QueueMessage> es) {
		boolean b = true;
		try {
			System.out.println("有消息进入"+queueName+"队列");
			for(QueueMessage e : es){
				if(MQDBImpl.INSTANCE.getIsDB()){
					try {
						MQDBImpl.INSTANCE.saveQueueMessage(e);
					} catch (Exception e1) {
						MQDBImpl.INSTANCE.saveLog("消息ID为:"+e.getId()+"消息持久化异常");
						return false;
					}
				}else{
					b = queueMessages.add(e);//这一步有隐藏bug，需要在后期连接数据库后做处理
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
	
	public void takeMessage(){//异步
		Consumer consumer = getOneAndRemoveConsumer();
		if(consumer == null){
			return;
		}
		addConsumer(consumer);
		if(consumer!=null){
			System.out.println(consumer.getId()+"准备进入监听");
			if(consumer.listenerMessage(this) == null){
				if(consumer.getStatus().get() == -1){//如果是已经关闭的
					remove(consumer);
				}
				return;//如果没有结果，发送数据
			}//监听
			consumer.getMessage();
			if(consumer.getStatus().get() == -1){
				remove(consumer);
			}
		}
	}
	
	class TakeMessage implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(isRun.get()){
				if(consumerQueueCount() > 0){
					takeMessage();
				}
			}
		}
		
	}
	
	public boolean addConsumer(Consumer consumer){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(consumerQueueCount == consumerQueue.length){
				return false;
			}
			if(consumerQueueCount == 0){
				//start();//启动当前队列
			}
			consumerQueue[consumerQueueCount] = consumer;
			consumerQueueCount++;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}
	
	public boolean addConsumer(int index,Consumer consumer){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(consumerQueueCount == 0){
				consumerQueue[0] = consumer;
				//start();//启动当前队列
			}else if(consumerQueueCount == consumerQueue.length){
				return false;
			}else{
				for(int i=consumerQueueCount;i>=index;i--){
					consumerQueue[i+1] = consumerQueue[i];
				}
				consumerQueue[index] = consumer;
			}
			consumerQueueCount++;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}
	
	public boolean remove(Consumer consumer){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(consumerQueueCount == 0){
				return false;
			}
			boolean ismove = false;
			for(int i=0;i<consumerQueueCount;i++){
				Consumer cos = consumerQueue[i];
				if(cos.equals(consumer) || cos == consumer){
					ismove = true;
					consumerQueue[i] = null;
				}
				if(ismove && consumerQueueCount < consumerQueue.length){
					consumerQueue[i] = consumerQueue[i+1];//往前移一个
				}
			}
			if(consumerQueueCount < consumerQueue.length){
				consumerQueue[consumerQueueCount] = null;
			}
			consumerQueueCount--;
			return ismove;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}
	
	public boolean remove(int index){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(consumerQueueCount == 0 || consumerQueueCount < index){
				return false;
			}
			consumerQueue[index] = null;
			for(int i=index;i<consumerQueueCount;i++){
				consumerQueue[i] = consumerQueue[i+1];//往前移一个
				consumerQueue[i+1] = null;
			}
			consumerQueueCount--;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}
	
	public int consumerQueueCount(){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		try {
			return consumerQueueCount;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}
	
	public Consumer getConsumerIndex(int index){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			return consumerQueue[index];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}
	
	public Consumer getOneAndRemoveConsumer(){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			if(consumerQueueCount == 0){
				isRun.set(false);
				return null;
			}
			Consumer consumer = consumerQueue[0];
			consumerQueue[0] = null;
			for(int i=0;i<consumerQueueCount;i++){
				consumerQueue[i] = consumerQueue[i+1];//往前移一个
				consumerQueue[i+1] = null;
			}
			consumerQueueCount--;
			return consumer;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isRun.set(false);//认为没有
			return null;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
		
	}
	
	public Consumer getConsumerFromId(String id){
		boolean lockFlag = false;
		try {
			lockFlag = consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			Consumer consumer = null;
			for(int i=0;i<consumerQueueCount;i++){
				Consumer cconsumer = consumerQueue[i];
				if(cconsumer.getId().equals(id)){
					consumer = cconsumer;
					break;
				}
			}
			return consumer;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(lockFlag){
				consumerQueueLocks.writeLock().unlock();
			}
		}
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
}
