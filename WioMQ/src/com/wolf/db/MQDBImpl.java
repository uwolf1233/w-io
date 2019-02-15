package com.wolf.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Producer;
import com.wolf.javabean.Publisher;
import com.wolf.javabean.QueueMessage;
import com.wolf.javabean.TopicMessage;
import com.wolf.javabean.TranMessage;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.sqls.MQsqls;

public class MQDBImpl implements MQDB{
	
	public static MQDB INSTANCE = new MQDBImpl();
	
	private static boolean isDB = true;
	
	@Override
	public void setIsDB(boolean b){
		isDB = b;
	}
	
	@Override
	public boolean getIsDB(){
		return isDB;
	}

	@Override
	public boolean saveQueueMessage(QueueMessage queueMessage) throws Exception{
		// TODO Auto-generated method stub
		String sql = MQsqls.saveQueueMessageSql;
		Object[] os = new Object[5];
		os[0] = queueMessage.getId();
		os[1] = queueMessage.getByteString().toByteArray();
		os[2] = queueMessage.getStatus()+"";
		os[3] = queueMessage.getProducer().getId();
		os[4] = queueMessage.getProducer().getName();
		int i = MyJdbc.INSTANCE.update(sql, os);
		return i >= 1 ? true : false;
	}
	
	@Override
	public List<QueueMessage> getQueueMessageList() throws Exception{
		String sql = MQsqls.getTopQueueMessageSql;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(sql, null);
		List<QueueMessage> qmList = new ArrayList<QueueMessage>();
		for(Map<String,Object> map : dataLm){
			QueueMessage qm = new QueueMessage();
			qm.setProducer(new Producer());
			qm.setId(map.get("id")+"");
			qm.setByteString(ByteString.copyFrom((byte[])map.get("byteString")));
			qm.setStatus(Integer.parseInt(map.get("status")+""));
			qm.getProducer().setId(map.get("producerid")+"");
			qm.getProducer().setName(map.get("producername")+"");
			qmList.add(qm);
		}
		return qmList;
	}

	@Override
	public boolean updateQueueMessageStatus(//status = 1表示已经进消费者,0表示等待中，-1表示废弃
			QueueMessage queueMessage, String status) throws Exception{
		String sql = MQsqls.updateQueueMessageStatusSql;
		int i = MyJdbc.INSTANCE.update(sql, new Object[]{status,queueMessage.getId()});
		return i > 0;
	}
	
	public void updateQueueMessageStatus(List<QueueMessage> queueMessages, String status) throws Exception {
		String sql = MQsqls.updateQueueMessageStatusSql;
		List<Object[]> list = new ArrayList<Object[]>();
		for(QueueMessage queueMessage : queueMessages){
			Object[] os = new Object[]{status,queueMessage.getId()};
			list.add(os);
		}
		MyJdbc.INSTANCE.batchUpdate(sql, list);
	}

	@Override
	public boolean delQueueMessage(QueueMessage queueMessage) throws Exception{
		String sql = MQsqls.updateQueueMessageStatusSql;
		int i = MyJdbc.INSTANCE.update(sql, new Object[]{"-1",queueMessage.getId()});
		return i > 0;
	}

	@Override
	public boolean saveTopicMessage(TopicMessage topicMessage) throws Exception{
		String sql = MQsqls.saveTopicMessageSql;
		Object[] os = new Object[5];
		os[0] = topicMessage.getId();
		os[1] = topicMessage.getByteString().toByteArray();
		os[2] = topicMessage.getStatus()+"";
		os[3] = topicMessage.getPublisher().getId();
		os[4] = topicMessage.getPublisher().getName();
		int i = MyJdbc.INSTANCE.update(sql, os);
		return i >= 1 ? true : false;
	}

	@Override
	public List<TopicMessage> getTopicMessageList() throws Exception{
		String sql = MQsqls.getTopTopicMessageSql;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(sql, null);
		List<TopicMessage> qmList = new ArrayList<TopicMessage>();
		for(Map<String,Object> map : dataLm){
			TopicMessage qm = new TopicMessage();
			qm.setPublisher(new Publisher());
			qm.setId(map.get("id")+"");
			qm.setByteString(ByteString.copyFrom((byte[])map.get("byteString")));
			qm.setStatus(Integer.parseInt(map.get("status")+""));
			qm.getPublisher().setId(map.get("publisherid")+"");
			qm.getPublisher().setName(map.get("publishername")+"");
			qmList.add(qm);
		}
		return qmList;
	}

	@Override
	public boolean updateTopicMessageStatus(List<TopicMessage> topicMessages,
			String status) throws Exception{
		String sql = MQsqls.updateTopicMessageStatusSql;
		StringBuilder idsBuilder = new StringBuilder("");
		int i = 0;
		for(TopicMessage topicMessage : topicMessages){
			idsBuilder.append(i == 0 ? ("'"+topicMessage.getId()+"'") : (",'"+topicMessage.getId()+"'"));
			i++;
		}
		sql = sql.replace("#", idsBuilder.toString());
		int a = MyJdbc.INSTANCE.update(sql, new Object[]{status});
		return a > 0;
	}

	@Override
	public boolean delTopicMessage(TopicMessage topicMessage) throws Exception{
		String sql = MQsqls.updateTopicMessageStatusSql;
		int i = MyJdbc.INSTANCE.update(sql, new Object[]{"-1",topicMessage.getId()});
		return i > 0;
	}

	@Override
	public void saveLog(String logdata) {
		// TODO Auto-generated method stub
		String sql = MQsqls.savemqlogSql;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String logtime = format.format(new Date());
		MyJdbc.INSTANCE.update(sql, new Object[]{logdata,logtime});
	}
	
	@Override
	public void saveLog(String logdata,Exception e){
		String exceptionData = "";
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exceptionData = sw.toString();
			sw.close();
			pw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sql = MQsqls.savemqlogSql;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String logtime = format.format(new Date());
		MyJdbc.INSTANCE.update(sql, new Object[]{logdata+"--"+exceptionData,logtime});
	}

	@Override
	public boolean saveConsumerQueueMessage(String consumerid,
			String queuemessageid, String status) throws Exception{//status:0表示还没发，1表示已经发了
		String sql = MQsqls.saveConsumerQueueMessage;
		int i = MyJdbc.INSTANCE.update(sql, new Object[]{consumerid,queuemessageid,status});
		return i>0;
	}
	
	@Override
	public void saveConsumerQueueMessage(String consumerid,List<String> queuemessageids,String status) throws Exception{
		String sql = MQsqls.saveConsumerQueueMessage;
		List<Object[]> list = new ArrayList<Object[]>();
		for(String queuemessageid : queuemessageids){
			Object[] os = new Object[]{consumerid,queuemessageid,status};
			list.add(os);
		}
		MyJdbc.INSTANCE.batchUpdate(sql, list);
	}

	@Override
	public List<QueueMessage> getConsumerQueueMessage(String consumerid,
			String status) throws Exception{
		String sql = MQsqls.getConsumerQueueMessage;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(sql, new Object[]{consumerid,status});
		List<QueueMessage> qmList = new ArrayList<QueueMessage>();
		for(Map<String,Object> map : dataLm){
			QueueMessage qm = new QueueMessage();
			qm.setProducer(new Producer());
			qm.setId(map.get("id")+"");
			qm.setByteString(ByteString.copyFrom((byte[])map.get("byteString")));
			qm.setStatus(Integer.parseInt(map.get("status")+""));
			qm.getProducer().setId(map.get("producerid")+"");
			qm.getProducer().setName(map.get("producername")+"");
			qmList.add(qm);
		}
		return qmList;
	}
	
	@Override
	public void updateConsumerQueueMessage(String ids) throws Exception{
		String sql = MQsqls.updateConsumerQueueMessage;
		sql = sql.replace("#", ids);
		MyJdbc.INSTANCE.update(sql, null);
	}
	
	@Override
	public void saveSubscriberTopicMessage(String subscriberid,List<String> topicmessageids,String status) throws Exception{
		String sql = MQsqls.saveSubscriberTopicMessage;
		List<Object[]> list = new ArrayList<Object[]>();
		for(String topicmessageid : topicmessageids){
			Object[] os = new Object[]{subscriberid,topicmessageid,status};
			list.add(os);
		}
		MyJdbc.INSTANCE.batchUpdate(sql, list);
	}
	
	@Override
	public List<TopicMessage> getSubscriberTopicMessage(String subscriberid,String status) throws Exception{
		String sql = MQsqls.getSubscriberTopicMessage;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(sql, new Object[]{subscriberid,status});
		List<TopicMessage> qmList = new ArrayList<TopicMessage>();
		for(Map<String,Object> map : dataLm){
			TopicMessage tm = new TopicMessage();
			tm.setPublisher(new Publisher());
			tm.setId(map.get("id")+"");
			tm.setByteString(ByteString.copyFrom((byte[])map.get("byteString")));
			tm.setStatus(Integer.parseInt(map.get("status")+""));
			tm.getPublisher().setId(map.get("publisherid")+"");
			tm.getPublisher().setName(map.get("publishername")+"");
			qmList.add(tm);
		}
		return qmList;
	}
	
	@Override
	public void updateSubscriberTopicMessage(String ids) throws Exception{
		String sql = MQsqls.updateSubscriberTopicMessage;
		sql = sql.replace("#", ids);
		MyJdbc.INSTANCE.update(sql, null);
	}

	@Override
	public boolean saveTranMessage(TranMessage tranMessage) {
		String saveTranMessageSql = MQsqls.saveTranMessageSql;
		long endtime = tranMessage.getEndtime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		date.setTime(endtime);
		String et = format.format(date);
		String ppname = "";
		if(tranMessage.getType().equals("queue")){
			ppname = tranMessage.getProducer().getName();
		}else if(tranMessage.getType().equals("topic")){
			ppname = tranMessage.getPublisher().getName();
		}
		MyJdbc.INSTANCE.update(saveTranMessageSql, new Object[]{
				tranMessage.getId(),tranMessage.getType(),et,tranMessage.getQueueName(),tranMessage.getTransName(),
				tranMessage.getByteString().toByteArray(),tranMessage.getPpid(),ppname
		});
		return false;
	}
	
//	public static void main(String[] args) {
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//		long endtime = new Date().getTime();
//		System.out.println(endtime);
//		Date date2 = new Date();
//		date2.setTime(endtime);
//		System.out.println(format.format(date2));
//	}

	@Override
	public boolean updateTranMessage(String status, String transName,String queueName) {
		String updateTranMessageSql = MQsqls.updateTranMessageSql;
		int i = MyJdbc.INSTANCE.update(updateTranMessageSql, new Object[]{status,transName,queueName});
		return i > 0;
	}

	@Override
	public List<TranMessage> getTranMessageFromPpid(String ppid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TranMessage> getTranMessageFromTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTackTranMessageCount() {
		String getTackTranMessageCountSql = MQsqls.getTackTranMessageCountSql;
		int count = Integer.parseInt(MyJdbc.INSTANCE.queryForFields(getTackTranMessageCountSql, null, new String[]{"c"})[0]+"");
		return count;
	}

	@Override
	public List<TranMessage> getTranMessageFromQueueTT(String transName,
			String queueName) throws Exception{
		String getTranMessageFromTTSql = MQsqls.getTranMessageFromTTQueueSql;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(getTranMessageFromTTSql, new Object[]{transName,queueName});
		List<TranMessage> tmList = new ArrayList<TranMessage>();
		for(Map<String,Object> map : dataLm){
			TranMessage tm = new TranMessage();
			tmList.add(tm);
			tm.setId(map.get("id")+"");
			String type = map.get("type")+"";
			tm.setType(type);
			String endTime = map.get("endtime")+"";//时间
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date date = format.parse(endTime);
			tm.setEndtime(date.getTime());
			tm.setQueueName(map.get("queuename")+"");
			tm.setTransName(map.get("transname")+"");
			tm.setByteString(ByteString.copyFrom((byte[])map.get("byteString")));
			String ppid = map.get("ppid")+"";
			tm.setPpid(ppid);
			if(type.equals("queue")){
				Producer p = new Producer();
				p.setId(ppid);
				p.setName(map.get("ppname")+"");
				tm.setProducer(p);
			}else if(type.equals("topic")){
				Publisher p = new Publisher();
				p.setId(ppid);
				p.setName(map.get("ppname")+"");
				tm.setPublisher(p);
			}
		}
		//List<TranMessage> tmlist = MyJdbc.INSTANCE.queryForList(getTranMessageFromTTSql, new Object[]{transName,queueName}, TranMessage.class);
		return tmList;
	}

	@Override
	public List<TranMessage> getTranMessageFromTopicTT(String transName,
			String queueName) throws Exception{
		String getTranMessageFromTTTopicSql = MQsqls.getTranMessageFromTTTopicSql;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(getTranMessageFromTTTopicSql, new Object[]{transName,queueName});
		List<TranMessage> tmList = new ArrayList<TranMessage>();
		for(Map<String,Object> map : dataLm){
			TranMessage tm = new TranMessage();
			tmList.add(tm);
			tm.setId(map.get("id")+"");
			String type = map.get("type")+"";
			tm.setType(type);
			String endTime = map.get("endtime")+"";//时间
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date date = format.parse(endTime);
			tm.setEndtime(date.getTime());
			tm.setQueueName(map.get("queuename")+"");
			tm.setTransName(map.get("transname")+"");
			tm.setByteString(ByteString.copyFrom((byte[])map.get("byteString")));
			String ppid = map.get("ppid")+"";
			tm.setPpid(ppid);
			if(type.equals("queue")){
				Producer p = new Producer();
				p.setId(ppid);
				p.setName(map.get("ppname")+"");
				tm.setProducer(p);
			}else if(type.equals("topic")){
				Publisher p = new Publisher();
				p.setId(ppid);
				p.setName(map.get("ppname")+"");
				tm.setPublisher(p);
			}
		}
		return tmList;
//		List<TranMessage> tmlist = MyJdbc.INSTANCE.queryForList(getTranMessageFromTTTopicSql, new Object[]{transName,queueName}, TranMessage.class);
//		return tmlist;
	}

	@Override
	public List<Object[]> getTranMessagePpids(String ppid) {
		String getQueueTranMessagePpidsSql = MQsqls.getQueueTranMessagePpidsSql;
		List<Object[]> oslist = MyJdbc.INSTANCE.queryListForFields(getQueueTranMessagePpidsSql, new Object[]{ppid}, new String[]{"type"});
		return oslist;
	}

	@Override
	public List<Map<String, Object>> getTranMessageScan() {
		String getTranMessageScanSql = MQsqls.getTranMessageScanSql;
		List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForListMap(getTranMessageScanSql, null);
		return dataLm;
	}
	
}
