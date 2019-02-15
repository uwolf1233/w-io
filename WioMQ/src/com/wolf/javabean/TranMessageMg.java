package com.wolf.javabean;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.db.MQDBImpl;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.MQ.TranMessages;
import com.wolf.jdbcs.MyJdbc;

//持久化逻辑，生产消费举例
//1.先将消息加入事务消息表tranMessage
//2.然后将生产者id存入producerChannelMap，ppid为key对应channel为value，因为channel不存入数据库，ppid是producer或者publisherid，但请不要相同
//3.确认信息，通过ppid获取channel,找不到channel的不做判断
//4.断线重连，判断事务消息是否有和传入相同的ppid
//5.扫描重查事务，根据ppid获取channel
//6.事务反查，后续改为一个transName的事务只查询一次可以了
//7.后续将事务表数据移入历史记录表
public class TranMessageMg {

	private TranMessageMg(){
		initScan();
	}
	
	public static TranMessageMg INSTANCE = new TranMessageMg();
	
	private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(false);
	
	private TranMessage[] tranMessageQueue = new TranMessage[1000];
	
	private int tranMessageQueueCount = 0;
	
	public long tranMessageScanMinute = 10;
	
	public static ConcurrentMap<String, Channel> producerChannelMap = new ConcurrentHashMap<String, Channel>();
	public static ConcurrentMap<String, Channel> publishChannelMap = new ConcurrentHashMap<String, Channel>();

	public TranMessage[] getTranMessageQueue() {
		return tranMessageQueue;
	}

	public void setTranMessageQueue(TranMessage[] tranMessageQueue) {
		this.tranMessageQueue = tranMessageQueue;
	}

	public int getTranMessageQueueCount() {
		return tranMessageQueueCount;
	}

	public void setTranMessageQueueCount(int tranMessageQueueCount) {
		this.tranMessageQueueCount = tranMessageQueueCount;
	}
	
	public boolean addTranMessage(TranMessage tranMessage){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(lockFlag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					if(tranMessageQueueCount == tranMessageQueue.length){
						return false;
					}
					tranMessageQueue[tranMessageQueueCount] = tranMessage;
					tranMessageQueueCount++;
				}else{
					addChannel(tranMessage);//数据库持久化消息的时候用到
					MQDBImpl.INSTANCE.saveTranMessage(tranMessage);
				}
				if(tranMessage.getType().equals("queue")){
					returnSend(tranMessage,"tranQueueMessage");
				}else if(tranMessage.getType().equals("topic")){
					returnSend(tranMessage,"tranTopicMessage");
				}
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public boolean addTranMessage(int index,TranMessage tranMessage){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(lockFlag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					if(tranMessageQueueCount == 0){
						tranMessageQueue[0] = tranMessage;
					}else if(tranMessageQueueCount == tranMessageQueue.length){
						return false;
					}else{
						for(int i=tranMessageQueueCount;i>=index;i--){
							tranMessageQueue[i+1] = tranMessageQueue[i];
						}
						tranMessageQueue[index] = tranMessage;
					}
					tranMessageQueueCount++;
					addChannel(tranMessage);//数据库持久化消息的时候用到
					if(tranMessage.getType().equals("queue")){
						returnSend(tranMessage,"tranQueueMessage");
					}else if(tranMessage.getType().equals("topic")){
						returnSend(tranMessage,"tranTopicMessage");
					}
				}
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	private void addChannel(TranMessage tranMessage){
		if(tranMessage.getType().equals("queue")){
			producerChannelMap.put(tranMessage.getProducer().getId(), tranMessage.getChannel());
		}else if(tranMessage.getType().equals("topic")){
			publishChannelMap.put(tranMessage.getPublisher().getId(), tranMessage.getChannel());
		}
	}
	
	public void returnSend(TranMessage tranMessage,String type){//返回已收到消息
		MQData.Builder builder0 = MQData.newBuilder();
		builder0.setType(type);
		TranMessages.Builder tranMessages = TranMessages.newBuilder();
		tranMessages.setId(tranMessage.getId());
		tranMessages.setTranstatus("sendqueuesuccess");
		tranMessages.setTransname(tranMessage.getTransName());
		//builder0.getTranMessagesBuilderList().add(tranMessages);
		builder0.addTranMessages(tranMessages);
		tranMessage.getChannel().writeAndFlush(builder0);//去掉这个可以测试反查
		System.out.println("server消息"+tranMessage.getId()+"返回已接收成功");
	}
	
	public boolean remove(TranMessage tranMessage){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(tranMessageQueueCount == 0){
				return false;
			}
			boolean ismove = false;
			for(int i=0;i<tranMessageQueueCount;i++){
				TranMessage cos = tranMessageQueue[i];
				if(cos.equals(tranMessage) || cos == tranMessage){
					ismove = true;
					tranMessageQueue[i] = null;
				}
				if(ismove && tranMessageQueueCount < tranMessageQueue.length){
					tranMessageQueue[i] = tranMessageQueue[i+1];//往前移一个
				}
			}
			if(tranMessageQueueCount < tranMessageQueue.length){
				tranMessageQueue[tranMessageQueueCount] = null;
			}
			tranMessageQueueCount--;
			return ismove;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public boolean remove(int index){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			if(tranMessageQueueCount == 0 || tranMessageQueueCount < index){
				return false;
			}
			tranMessageQueue[index] = null;
			for(int i=index;i<tranMessageQueueCount;i++){
				tranMessageQueue[i] = tranMessageQueue[i+1];//往前移一个
				tranMessageQueue[i+1] = null;
			}
			tranMessageQueueCount--;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public boolean removeNoLock(int index){
		try {
			if(tranMessageQueueCount == 0 || tranMessageQueueCount < index){
				return false;
			}
			tranMessageQueue[index] = null;
			for(int i=index;i<tranMessageQueueCount;i++){
				tranMessageQueue[i] = tranMessageQueue[i+1];//往前移一个
				tranMessageQueue[i+1] = null;
			}
			tranMessageQueueCount--;
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public int tranMessageQueueCount(){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		try {
			if(lockFlag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					return tranMessageQueueCount;
				}else{
					return MQDBImpl.INSTANCE.getTackTranMessageCount();
				}
			}
			return 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public TranMessage getTranMessageIndex(int index){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			return tranMessageQueue[index];
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public TranMessage getOneAndRemoveTranMessage(){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			if(tranMessageQueueCount == 0){
				return null;
			}
			TranMessage tranMessage = tranMessageQueue[0];
			tranMessageQueue[0] = null;
			for(int i=0;i<tranMessageQueueCount;i++){
				tranMessageQueue[i] = tranMessageQueue[i+1];//往前移一个
				tranMessageQueue[i+1] = null;
			}
			tranMessageQueueCount--;
			return tranMessage;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
		
	}
	
	public TranMessage getTranMessageFromId(String id){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			TranMessage tranMessage = null;
			for(int i=0;i<tranMessageQueueCount;i++){
				TranMessage cTranMessage = tranMessageQueue[i];
				if(cTranMessage.getId().equals(id)){
					tranMessage = cTranMessage;
					break;
				}
			}
			return tranMessage;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void commitQueue(String transName,String queueName){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try{
			if(lockFlag){
				Producer_Consumer producer_Consumer = Producer_Consumer.hasAndGet(queueName);
				if(producer_Consumer==null){
					System.out.println("生产事务消息找不到名为:"+queueName+"的队列");
					return;
				}
				List<QueueMessage> es = new ArrayList<QueueMessage>();
				if(!MQDBImpl.INSTANCE.getIsDB()){
					List<Integer> removeIndexs = new ArrayList<Integer>();
					for(int i=0;i<tranMessageQueueCount;i++){
						System.out.println("事务消息提交队列="+queueName);
						TranMessage cTranMessage = tranMessageQueue[i];
						if(cTranMessage.getTransName()!=null && cTranMessage.getTransName().equals(transName)){
							if(cTranMessage.getType().equals("queue")){
								QueueMessage queueMessage = new QueueMessage();
								queueMessage.setByteString(cTranMessage.getByteString());
								queueMessage.setId(cTranMessage.getId());
								queueMessage.setProducer(queueMessage.getProducer());
								es.add(queueMessage);
								removeIndexs.add(i);//需要删除的位置组
							}
						}
					}
					if(producer_Consumer.offerMessage(es)){
						int removeIndexsSize = removeIndexs.size();
						for(int i=0;i<removeIndexsSize;i++){
							removeNoLock(removeIndexs.get(i));
						}
					}
				}else{
					List<TranMessage> tmList = MQDBImpl.INSTANCE.getTranMessageFromQueueTT(transName, queueName);
					boolean b = MQDBImpl.INSTANCE.updateTranMessage("1", transName, queueName);
					if(b){
						for(TranMessage cTranMessage : tmList){
							QueueMessage queueMessage = new QueueMessage();
							queueMessage.setByteString(cTranMessage.getByteString());
							queueMessage.setId(cTranMessage.getId());
							queueMessage.setProducer(cTranMessage.getProducer());
							es.add(queueMessage);
						}
						producer_Consumer.offerMessage(es);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void commitTopic(String transName,String queueName){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try{
			if(lockFlag){
				Publisher_Subscriber publisher_Subscriber = Publisher_Subscriber.hasAndGet(queueName);
				if(publisher_Subscriber==null){
					System.out.println("发布事务消息找不到名为:"+queueName+"的队列");
					return;
				}
				List<TopicMessage> es = new ArrayList<TopicMessage>();
				if(!MQDBImpl.INSTANCE.getIsDB()){
					List<Integer> removeIndexs = new ArrayList<Integer>();
					for(int i=0;i<tranMessageQueueCount;i++){
						System.out.println("事务消息提交队列="+queueName);
						TranMessage cTranMessage = tranMessageQueue[i];
						if(cTranMessage.getTransName()!=null && cTranMessage.getTransName().equals(transName)){
							if(cTranMessage.getType().equals("topic")){
								TopicMessage topicMessage = new TopicMessage();
								topicMessage.setByteString(cTranMessage.getByteString());
								topicMessage.setId(cTranMessage.getId());
								topicMessage.setPublisher(cTranMessage.getPublisher());
								es.add(topicMessage);
								removeIndexs.add(i);//需要删除的位置组
							}
						}
					}
					if(publisher_Subscriber.offerMessage(es)){
						int removeIndexsSize = removeIndexs.size();
						for(int i=0;i<removeIndexsSize;i++){
							removeNoLock(removeIndexs.get(i));
						}
					}
				}else{
					List<TranMessage> tmList = MQDBImpl.INSTANCE.getTranMessageFromTopicTT(transName, queueName);
					boolean b = MQDBImpl.INSTANCE.updateTranMessage("1", transName, queueName);
					if(b){
						for(TranMessage cTranMessage : tmList){
							TopicMessage topicMessage = new TopicMessage();
							topicMessage.setByteString(cTranMessage.getByteString());
							topicMessage.setId(cTranMessage.getId());
							topicMessage.setPublisher(cTranMessage.getPublisher());
							es.add(topicMessage);
						}
						publisher_Subscriber.offerMessage(es);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void rollbackQueue(String transName,String queueName){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try{
			if(lockFlag){
				int count = tranMessageQueueCount;//必须先换个引用，因为remove会减掉
				if(!MQDBImpl.INSTANCE.getIsDB()){
					List<Integer> removeIndexs = new ArrayList<Integer>();
					for(int i=0;i<count;i++){
						System.out.println("事务回滚队列="+queueName);
						TranMessage cTranMessage = tranMessageQueue[i];
						if(cTranMessage.getTransName()!=null && cTranMessage.getTransName().equals(transName)){
							if(cTranMessage.getType().equals("queue")){
								removeIndexs.add(i);//需要删除的位置组
							}
						}
					}
					int removeIndexsSize = removeIndexs.size();
					for(int i=0;i<removeIndexsSize;i++){
						removeNoLock(removeIndexs.get(i));
					}
				}else{
					MQDBImpl.INSTANCE.updateTranMessage("-1", transName, queueName);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void rollbackTopic(String transName,String queueName){
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try{
			if(lockFlag){
				int count = tranMessageQueueCount;//必须先换个引用，因为remove会减掉
				if(!MQDBImpl.INSTANCE.getIsDB()){
					List<Integer> removeIndexs = new ArrayList<Integer>();
					for(int i=0;i<count;i++){
						System.out.println("事务回滚队列="+queueName);
						TranMessage cTranMessage = tranMessageQueue[i];
						if(cTranMessage.getTransName()!=null && cTranMessage.getTransName().equals(transName)){
							if(cTranMessage.getType().equals("topic")){
								removeIndexs.add(i);//需要删除的位置组
							}
						}
					}
					int removeIndexsSize = removeIndexs.size();
					for(int i=0;i<removeIndexsSize;i++){
						removeNoLock(removeIndexs.get(i));
					}
				}else{
					MQDBImpl.INSTANCE.updateTranMessage("-1", transName, queueName);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void tranLine(String type,Channel channel,String ppid){//type:queue,toipc
		boolean lockFlag = false;
		try {
			lockFlag = rwLock.writeLock().tryLock(30000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try{
			if(lockFlag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					for(int i=0;i<tranMessageQueueCount;i++){
						TranMessage cTranMessage = tranMessageQueue[i];
						if(type.equals("queue") && cTranMessage.getProducer() != null && cTranMessage.getPpid().equals(ppid)){
							cTranMessage.setChannel(channel);
							producerChannelMap.put(cTranMessage.getProducer().getId(), channel);
							System.out.println("queue："+cTranMessage.getId()+"已重连");
						}else if(type.equals("topic") && cTranMessage.getPublisher() != null && cTranMessage.getPpid().equals(ppid)){
							cTranMessage.setChannel(channel);
							publishChannelMap.put(cTranMessage.getPublisher().getId(), channel);
							System.out.println("topic："+cTranMessage.getId()+"已重连");
						}
					}
				}else{
					List<Object[]> oslist = MQDBImpl.INSTANCE.getTranMessagePpids(ppid);
					for(Object[] os : oslist){
						if(os[0]!=null){
							String ctype = os[0]+"";
							if(type.equals(ctype)){
								if(type.equals("queue")){
									producerChannelMap.put(ppid, channel);
									System.out.println("queue："+ppid+"已重连");
								}else if(type.equals("topic")){
									publishChannelMap.put(ppid, channel);
									System.out.println("topic："+ppid+"已重连");
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(lockFlag){
				rwLock.writeLock().unlock();
			}
		}
	}
	
	public void initScan(){
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
		service.scheduleAtFixedRate(new Runnable() {  
	          
			private List<String> producerTransName = new ArrayList<String>();
			private List<String> publisherTransName = new ArrayList<String>();
			
	        @Override  
	        public void run() {
	        	boolean lockFlag = false;
	    		try {
	    			lockFlag = rwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
	    		} catch (InterruptedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    			return;
	    		}
	    		try{
	    			if(lockFlag){
	    				if(!MQDBImpl.INSTANCE.getIsDB()){
		    				for(int i=0;i<tranMessageQueueCount;i++){
		    					TranMessage cTranMessage = tranMessageQueue[i];
		    					if(!cTranMessage.isun()){
		    						//拿去判断,向发送者验证是否需要提交事务
		    						String transName = cTranMessage.getTransName();
		    						MQData.Builder builder0 = MQData.newBuilder();
		    						String type = cTranMessage.getType();
		    						if(type!=null && type.equals("queue")){//如果是生产消费
		    							if(producerTransName.contains(transName)){//如果存在同样的，则跳过
		    								builder0.clear();
		    								builder0 = null;
		    								continue;
		    							}
		    							System.out.println("需要确认");
		    							producerTransName.add(transName);
		    							builder0.setType("returnProducer");
		    							builder0.getProducerBuilder().setId(cTranMessage.getProducer().getId());
		    							builder0.getProducerBuilder().setName(cTranMessage.getProducer().getName());
		    						}else if(type!=null && type.equals("topic")){//如果是发布订阅
		    							if(publisherTransName.contains(transName)){//如果存在同样的，则跳过
		    								builder0.clear();
		    								builder0 = null;
		    								continue;
		    							}
		    							System.out.println("需要确认");
		    							publisherTransName.add(transName);
		    							builder0.setType("returnPublisher");
		    							builder0.getPublisherBuilder().setId(cTranMessage.getPublisher().getId());
		    							builder0.getPublisherBuilder().setId(cTranMessage.getPublisher().getName());
		    						}
		    						TranMessages.Builder tranMessages = TranMessages.newBuilder();
		    						tranMessages.setId(cTranMessage.getId());
		    						tranMessages.setTranstatus("waitstatus");
		    						tranMessages.setTransname(transName);
		    						builder0.addTranMessages(tranMessages);
		    						Channel channel = cTranMessage.getChannel();
		    						if(channel != null && channel.isActive() && channel.isOpen()){
		    							System.out.println("即将发送确认");
		    							cTranMessage.getChannel().writeAndFlush(builder0);
		    						}else{
		    							builder0.clear();
		    							builder0 = null;
		    						}
		    					}
		    				}
	    				}else{
	    					List<Map<String,Object>> dataLm = MQDBImpl.INSTANCE.getTranMessageScan();
	    					for(Map<String,Object> map : dataLm){
	    						MQData.Builder builder0 = MQData.newBuilder();
	    						String type = map.get("type")+"";
	    						String ppid = map.get("ppid")+"";
	    						String transName = map.get("transName")+"";
	    						Channel channel = null;
	    						if(type!=null && type.equals("queue")){//如果是生产消费
	    							builder0.setType("returnProducer");
	    							builder0.getProducerBuilder().setId(ppid);
	    							channel = producerChannelMap.get(ppid);
	    							System.out.println(ppid+"需要确认");
	    						}else if(type!=null && type.equals("topic")){//如果是发布订阅
	    							builder0.setType("returnPublisher");
	    							builder0.getPublisherBuilder().setId(ppid);
	    							channel = publishChannelMap.get(ppid);
	    							System.out.println(ppid+"需要确认");
	    						}
	    						TranMessages.Builder tranMessages = TranMessages.newBuilder();
	    						tranMessages.setId(map.get("id")+"");
	    						tranMessages.setTranstatus("waitstatus");
	    						tranMessages.setTransname(transName);
	    						builder0.addTranMessages(tranMessages);
	    						if(channel != null && channel.isActive() && channel.isOpen()){
	    							System.out.println("即将发送确认");
	    							channel.writeAndFlush(builder0);
	    						}else{
	    							builder0.clear();
	    							builder0 = null;
	    						}
	    					}
	    				}
	    			}
	    		}catch(Exception e){
	    			e.printStackTrace();
	    		}finally{
	    			producerTransName.clear();
	    			publisherTransName.clear();
	    			if(lockFlag){
	    				rwLock.writeLock().unlock();
	    			}
	    		}
	        }
	    }, 3, tranMessageScanMinute, TimeUnit.SECONDS);
	}
	
}
