package com.wolf.sqls;

public class MQsqls {

	public static String saveQueueMessageSql = "insert into queueMessage(id,byteString,status,producerid,producername) values(?,?,?,?,?)";
	public static String getQueueMessageSql = "select * from queueMessage where id = ?";
	public static String getTopQueueMessageSql = "select top 1 * from queueMessage where status = 0";
	public static String updateQueueMessageStatusSql = "update queueMessage set status = ? where id = ?";
	
	public static String saveTopicMessageSql = "insert into topicMessage(id,byteString,status,publisherid,publishername) values(?,?,?,?,?)";
	public static String getTopicMessageSql = "select * from topicMessage where id = ?";
	public static String getTopTopicMessageSql = "select top 300 * from topicMessage where status = 0";
	public static String updateTopicMessageStatusSql = "update topicMessage set status = ? where id in (#)";
	
	public static String savemqlogSql = "insert into mqlog(id,logdata,logtime) values(replace(newid(),'-',''),?,?)";
	
	public static String saveConsumerQueueMessage = "insert into consumerQueueMessage(consumerid,queuemessageid,status) values(?,?,?)";
	public static String getConsumerQueueMessage = "select * from queueMessage where id in (select queuemessageid from consumerQueueMessage where consumerid = ? and status = ?) and status = 1";
	public static String updateConsumerQueueMessage = "update consumerQueueMessage set status = 1 where queuemessageid in (#)";
	
	public static String saveSubscriberTopicMessage = "insert into subscriberTopicMessage(subscriberid,topicmessageid,status) values(?,?,?)";
	public static String getSubscriberTopicMessage = "select * from topicMessage where id in (select topicmessageid from subscriberTopicMessage where subscriberid = ? and status = ?) and status = 1";
	public static String updateSubscriberTopicMessage = "update subscriberTopicMessage set status = 1 where topicmessageid in (#)";
	
	public static String saveTranMessageSql = "insert into tranMessage(id,type,endtime,queueName,transName,byteString,ppid,ppname,status) "
			+ " values(?,?,?,?,?,?,?,?,'0')";
	public static String updateTranMessageSql = "update tranMessage set status = ? where transName = ? and status = 0 and queueName = ?";
	public static String getTranMessageFromPpidSql = "select * from tranMessage where ppid = ? and status = 0";
	public static String getTranMessageFromTimeSql = "select * from tranMessage where cast(endtime as datetime) <= CONVERT(varchar,GETDATE(),120) and status = 0";
	public static String getTackTranMessageCountSql = "select count(*) c from tranMessage where status = 0";
	public static String getTranMessageFromTTQueueSql = "select * from tranMessage where transName = ? and queueName = ? and status = 0 and type = 'queue'";
	public static String getTranMessageFromTTTopicSql = "select * from tranMessage where transName = ? and queueName = ? and status = 0 and type = 'topic'";
	public static String getQueueTranMessagePpidsSql = "select distinct type from tranMessage where status = 0 and ppid = ?";
	public static String getTranMessageScanSql = "select * from tranMessage where status = 0 and cast(endtime as datetime) <= CONVERT(varchar,GETDATE(),120)";
}
