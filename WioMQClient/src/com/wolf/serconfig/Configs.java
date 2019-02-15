package com.wolf.serconfig;

import java.sql.Connection;
import java.sql.SQLException;

import com.wolf.db.MQDBImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Configs{
	
	private Configs(){}
	
	public static Configs INSTANCE = new Configs();
	
	public static HikariDataSource ds = null;
	
	public static String url = "";
	public static String username = "";
	public static String password = "";
	public static String cachePrepStmts = "";
	public static String prepStmtCacheSize = "";
	public static String prepStmtCacheSqlLimit = "";
	
	//数据库配置
	public static void dbconfig(){
		
		HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);

        ds = new HikariDataSource(hikariConfig);
	}

	public Connection getCon() {
		// TODO Auto-generated method stub
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
