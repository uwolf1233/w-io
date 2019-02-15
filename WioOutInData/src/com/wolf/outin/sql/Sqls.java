package com.wolf.outin.sql;

public class Sqls {

	public static String outinSql = "select * from outin where name = ?";
	public static String outinfieldsSql = "select * from outinfields where outinid = ?";
	public static String outindbsSql = "select * from outindbs where outinid = ?";
	public static String getDBtableSql = "select name fieldname,cast(length as varchar) fieldlen from syscolumns Where ID=OBJECT_ID(?)";
	public static String getAllOutInSetSql = "select * from outindbs";
	
	public static String outinFromNameSql = "select * from outin where name = ?";
	
}
