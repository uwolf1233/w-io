package com.wolf.serconfig;

import java.sql.Connection;
import java.sql.SQLException;

import com.wolf.server.DBCon;
import com.wolf.server.OtherServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Configs implements DBCon{
	
	private Configs(){}
	
	public static Configs INSTANCE = new Configs();
	
	public static HikariDataSource ds = null;
	
	//数据库配置
	public static void dbconfig(){
//		HikariConfig config = new HikariConfig();
//		config.setMaximumPoolSize(20);
//		config.setDataSourceClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//		config.addDataSourceProperty("serverName", "localhost");
//		config.addDataSourceProperty("port", "1433");
//		config.addDataSourceProperty("databaseName", "WIORole");
//		config.addDataSourceProperty("user", "sa");
//		config.addDataSourceProperty("password", "123");
//
//		ds = new HikariDataSource(config);
		
//		hikariConfig.setJdbcUrl("jdbc:sqlserver://localhost:1433;DatabaseName=Wios");
//        hikariConfig.setUsername("sa");
//        hikariConfig.setPassword("123");
//        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "300");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		
		HikariConfig hikariConfig = new HikariConfig();
		
        hikariConfig.setJdbcUrl(OtherServer.dburl);
        hikariConfig.setUsername(OtherServer.dbusername);
        hikariConfig.setPassword(OtherServer.dbuserpass);
        hikariConfig.addDataSourceProperty("cachePrepStmts", OtherServer.cachePrepStmts);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", OtherServer.prepStmtCacheSize);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", OtherServer.prepStmtCacheSqlLimit);

        ds = new HikariDataSource(hikariConfig);
	}

	@Override
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
