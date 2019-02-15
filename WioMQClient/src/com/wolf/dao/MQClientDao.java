package com.wolf.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wolf.db.MQDBImpl;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.sql.Sqls;

public class MQClientDao {

	private MQClientDao(){}
	
	public static MQClientDao INSTANCE = new MQClientDao();
	
	public boolean saveProducerToDB(String producerid,String producername,String queuename,String transname,String transtatus) throws SQLException{
		String saveTranproducerSql = Sqls.saveTranproducerSql;
		return MyJdbc.INSTANCE.update(saveTranproducerSql, new Object[]{producerid,producername,queuename,transname,transtatus}, 
				MQDBImpl.INSTANCE.getConnection()) > 0;
	}
	
	public boolean producerCommitOrRollback(String transtatus,String transname) throws SQLException{
		String producerCommitOrRollbackSql = Sqls.producerCommitOrRollbackSql;
		return MyJdbc.INSTANCE.update(producerCommitOrRollbackSql, new Object[]{transtatus,transname}, MQDBImpl.INSTANCE.getConnection()) > 0;
	}
	
	public String getProducerTranStatus(String transname) throws SQLException{
		String getProducerTranStatusSql = Sqls.getProducerTranStatusSql;
		Object[] os = MyJdbc.INSTANCE.queryForFields(getProducerTranStatusSql, new Object[]{transname}, 
				new String[]{"transtatus"}, MQDBImpl.INSTANCE.getConnection());
		return os[0]+"";
	}
	
	public boolean updateProducerStatus(String transname) throws SQLException{
		String updateProducerStatusSql = Sqls.updateProducerStatusSql;
		return MyJdbc.INSTANCE.update(updateProducerStatusSql, new Object[]{transname}, MQDBImpl.INSTANCE.getConnection()) > 0;
	}
	
	public Object[] tranproducerAndCur(String fields[],String transname) throws SQLException{
		String tranproducerAndCurSql = Sqls.tranproducerAndCurSql;
		return MyJdbc.INSTANCE.queryForFields(tranproducerAndCurSql, new Object[]{transname}, fields, MQDBImpl.INSTANCE.getConnection());
	}
	
	public List<Object[]> getProducerToLine() throws SQLException{
		String getProducerToLineSql = Sqls.getProducerToLineSql;
		return MyJdbc.INSTANCE.queryListForFields(getProducerToLineSql, null, new String[]{"producerid"});
	}
	
	
	public boolean savePublisherToDB(String publisherid,String publishername,String queuename,String transname,String transtatus) throws SQLException{
		String saveTranpublisherSql = Sqls.saveTranpublisherSql;
		return MyJdbc.INSTANCE.update(saveTranpublisherSql, new Object[]{publisherid,publishername,queuename,transname,transtatus}, 
				MQDBImpl.INSTANCE.getConnection()) > 0;
	}
	
	public boolean publisherCommitOrRollback(String transtatus,String transname) throws SQLException{
		String publisherCommitOrRollbackSql = Sqls.publisherCommitOrRollbackSql;
		return MyJdbc.INSTANCE.update(publisherCommitOrRollbackSql, new Object[]{transtatus,transname}, MQDBImpl.INSTANCE.getConnection()) > 0;
	}
	
	public String getPublisherTranStatus(String transname) throws SQLException{
		String getPublisherTranStatusSql = Sqls.getPublisherTranStatusSql;
		Object[] os = MyJdbc.INSTANCE.queryForFields(getPublisherTranStatusSql, new Object[]{transname}, 
				new String[]{"transtatus"}, MQDBImpl.INSTANCE.getConnection());
		return os[0]+"";
	}
	
	public boolean updatePublisherStatus(String transname) throws SQLException{
		String updatePublisherStatusSql = Sqls.updatePublisherStatusSql;
		return MyJdbc.INSTANCE.update(updatePublisherStatusSql, new Object[]{transname}, MQDBImpl.INSTANCE.getConnection()) > 0;
	}
	
	public Object[] tranpublisherAndCur(String fields[],String transname) throws SQLException{
		String tranpublisherAndCurSql = Sqls.tranpublisherAndCurSql;
		return MyJdbc.INSTANCE.queryForFields(tranpublisherAndCurSql, new Object[]{transname}, fields, MQDBImpl.INSTANCE.getConnection());
	}
	
	public List<Object[]> getPublisherToLine() throws SQLException{
		String getPublisherToLineSql = Sqls.getPublisherToLineSql;
		return MyJdbc.INSTANCE.queryListForFields(getPublisherToLineSql, null, new String[]{"publisherid"});
	}
	
}
