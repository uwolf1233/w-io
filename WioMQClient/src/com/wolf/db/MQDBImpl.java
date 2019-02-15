package com.wolf.db;

import java.sql.Connection;
import java.sql.SQLException;

import com.wolf.jdbcs.MyJdbc;
import com.wolf.serconfig.Configs;
import com.wolf.sql.Sqls;


public class MQDBImpl implements MQDB{
	
	public static MQDB INSTANCE = new MQDBImpl();
	
	private static boolean isDB = false;
	
	@Override
	public void setIsDB(boolean b){
		isDB = b;
	}
	
	@Override
	public boolean getIsDB(){
		return isDB;
	}

	@Override
	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		return Configs.ds.getConnection();
	}

	
	
}
