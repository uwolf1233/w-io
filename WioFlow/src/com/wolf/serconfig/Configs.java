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
		HikariConfig hikariConfig = new HikariConfig();
		//postgresql，临时配置
//		hikariConfig.setDriverClassName("org.postgresql.ds.PGSimpleDataSource");
//		hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/MyWio");
//		hikariConfig.setUsername("postgres");
//		hikariConfig.setPassword("123");
//      hikariConfig.addDataSourceProperty("cachePrepStmts", OtherServer.cachePrepStmts);
//      hikariConfig.addDataSourceProperty("prepStmtCacheSize", OtherServer.prepStmtCacheSize);
//      hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", OtherServer.prepStmtCacheSqlLimit);
		
		hikariConfig.setJdbcUrl(OtherServer.dburl);
        hikariConfig.setUsername(OtherServer.dbusername);
        hikariConfig.setPassword(OtherServer.dbuserpass);
        hikariConfig.addDataSourceProperty("cachePrepStmts", OtherServer.cachePrepStmts);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", OtherServer.prepStmtCacheSize);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", OtherServer.prepStmtCacheSqlLimit);

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
