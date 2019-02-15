package com.wolf.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wolf.db.MQDB;
import com.wolf.javabean.Consumer;
import com.wolf.javabean.Producer_Consumer;
import com.wolf.javabean.Publisher_Subscriber;
import com.wolf.javabean.Subscriber;

//前端展示
public class MQview {

	private MQDB mqDB;
//	
//	public Object showQueueAll(){
//		Map<String,Object> map = new HashMap<String,Object>();
//		map.put("consumerData", getConsumerData());
//		map.put("subscriberData", getSubscriberData());
//		return map;
//	}
	
//	public Object getConsumerData(){
//		List<Map<String,Object>> consumerLM = new ArrayList<Map<String,Object>>();
//		if(!mqDB.isDB()){
//			boolean lockFlag = false;
//			try {
//				lockFlag = Producer_Consumer.INSTANCE.consumerQueueLocks.writeLock().tryLock(10000, TimeUnit.SECONDS);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			try{
//				for(int i=0;i<Producer_Consumer.INSTANCE.consumerQueueCount;i++){
//					Consumer consumer = Producer_Consumer.INSTANCE.consumerQueue[i];
//					Map<String,Object> map = new HashMap<String,Object>();
//					map.put("id", consumer.getId());
//					map.put("name", consumer.getName());
//					map.put("status", consumer.getStatus());
//					map.put("message", consumer.getQueueMessageListData());
//					consumerLM.add(map);
//				}
//			}catch(Exception e){
//				e.printStackTrace();
//			}finally{
//				if(lockFlag){
//					Producer_Consumer.INSTANCE.consumerQueueLocks.writeLock().unlock();
//				}
//			}
//		}
//		return consumerLM;
//	}
//	
//	public Object getSubscriberData(){
//		List<Map<String,Object>> subscriberLM = new ArrayList<Map<String,Object>>();
//		if(!mqDB.isDB()){
//			try{
//				List<Subscriber> subscriberQueue = Publisher_Subscriber.INSTANCE.subscriberQueue;
//				Iterator<Subscriber> it = subscriberQueue.iterator();
//				while(it.hasNext()){
//					Subscriber subscriber = it.next();
//					Map<String,Object> map = new HashMap<String,Object>();
//					map.put("id", subscriber.getId());
//					map.put("name", subscriber.getName());
//					map.put("status", subscriber.getStatus());
//					map.put("message", subscriber.getTopicMessageListData());
//					subscriberLM.add(map);
//				}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
//		return subscriberLM;
//	}
	
}
