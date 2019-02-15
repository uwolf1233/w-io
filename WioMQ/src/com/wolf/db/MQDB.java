package com.wolf.db;

import java.util.List;
import java.util.Map;

import com.wolf.javabean.Consumer;
import com.wolf.javabean.QueueMessage;
import com.wolf.javabean.Subscriber;
import com.wolf.javabean.TopicMessage;
import com.wolf.javabean.TranMessage;

public interface MQDB {

	public void setIsDB(boolean b);
	
	public boolean getIsDB();
	
	public boolean saveQueueMessage(QueueMessage queueMessage) throws Exception;
	
	public List<QueueMessage> getQueueMessageList() throws Exception;
	
	public boolean updateQueueMessageStatus(QueueMessage queueMessage, String status) throws Exception;
	
	public void updateQueueMessageStatus(List<QueueMessage> queueMessages, String status) throws Exception;
	
	public boolean delQueueMessage(QueueMessage queueMessage) throws Exception;
	
	
	public boolean saveTopicMessage(TopicMessage topicMessage) throws Exception;
	
	public List<TopicMessage> getTopicMessageList() throws Exception;
	
	public boolean updateTopicMessageStatus(List<TopicMessage> topicMessages, String status) throws Exception;
	
	public boolean delTopicMessage(TopicMessage topicMessage) throws Exception;
	
	public void saveLog(String logdata);
	
	public void saveLog(String logdata,Exception e);
	
	public boolean saveConsumerQueueMessage(String consumerid,String queuemessageid,String status) throws Exception;
	
	public void saveConsumerQueueMessage(String consumerid,List<String> queuemessageids,String status) throws Exception;
	
	public List<QueueMessage> getConsumerQueueMessage(String consumerid,String status) throws Exception;
	
	public void updateConsumerQueueMessage(String ids) throws Exception;
	
	public void saveSubscriberTopicMessage(String subscriberid,List<String> topicmessageids,String status) throws Exception;
	
	public List<TopicMessage> getSubscriberTopicMessage(String subscriberid,String status) throws Exception;
	
	public void updateSubscriberTopicMessage(String ids) throws Exception;
	
	public boolean saveTranMessage(TranMessage tranMessage);
	
	public boolean updateTranMessage(String status,String transName,String queueName);
	
	public List<TranMessage> getTranMessageFromPpid(String ppid);//用于断线重连
	
	public List<TranMessage> getTranMessageFromTime();//用于判断超时
	
	public int getTackTranMessageCount();//判断等待的事务消息数量
	
	public List<TranMessage> getTranMessageFromQueueTT(String transName,String queueName) throws Exception;
	
	public List<TranMessage> getTranMessageFromTopicTT(String transName,String queueName) throws Exception;
	
	public List<Object[]> getTranMessagePpids(String ppid);
	
	public List<Map<String,Object>> getTranMessageScan();
}






