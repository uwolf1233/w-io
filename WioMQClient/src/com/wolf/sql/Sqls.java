package com.wolf.sql;

public class Sqls {

	public static String saveTranproducerSql = "insert into tranproducer(producerid,producername,queuename,transname,transtatus,status)"
			+" values(?,?,?,?,?,'0')";
	public static String getProducerTranStatusSql = "select transtatus from tranproducer where transname = ?";
	public static String producerCommitOrRollbackSql = "update tranproducer set transtatus = ? where transname = ? and status = '0'";
	public static String updateProducerStatusSql = "update tranproducer set status = 1 where transname = ?";
	public static String tranproducerAndCurSql = "select producerid,producername,queuename,transname,transtatus from tranproducer where transname = ?";//一套事务名称必须唯一
	public static String getProducerToLineSql = "select distinct producerid from tranproducer";
	
	
	public static String saveTranpublisherSql = "insert into tranpublisher(publisherid,publishername,queuename,transname,transtatus,status)"
			+" values(?,?,?,?,?,'0')";
	public static String getPublisherTranStatusSql = "select transtatus from tranpublisher where transname = ?";
	public static String publisherCommitOrRollbackSql = "update tranpublisher set transtatus = ? where transname = ? and status = '0'";
	public static String updatePublisherStatusSql = "update tranpublisher set status = 1 where transname = ?";
	public static String tranpublisherAndCurSql = "select publisherid,publishername,queuename,transname,transtatus from tranpublisher where transname = ?";//一套事务名称必须唯一
	public static String getPublisherToLineSql = "select distinct publisherid from tranpublisher";
	
}
