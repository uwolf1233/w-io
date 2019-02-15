package com.wolf.sqls;

public class Sqls {

	public static String usercountSql = "select count(*) c from logusers where status != '-1' and name = ?";
	//public static String getUserPwdSql = "select id,pwd,status from logusers where status != '-1' and name = ?";
	//public static String updateUsersStatusSql = "update logusers set status = ? where name = ? and status = ?";
	public static String getUserPwdSql = "select t1.id,t1.pwd,t1.status,t2.logusersid,t2.sessionid from logusers t1 "
			+"left join logsession t2 on t1.id = t2.logusersid where status != '-1' and name = ?";
	public static String updateUsersStatusSql = "update logusers set status = ? where id = ?";
	public static String updateUsersNameStatusSql = "update logusers set status = ? where name = ?";
	
	public static String saveUserSql = "insert into logusers(id,name,pwd,status) values(?,?,?,?)";
	
	public static String getUserIdFromUserNameSql = "select id from logusers where name = ?";
	public static String userHasSessionSql = "select sessionid,logusersid from logsession where logusersid = ? and sessionid is not null";
	public static String insertSessionSql = "insert into logsession(logusersid,sessionid) values(?,?)";
	public static String removeSessionSql = "delete from logsession where logusersid = ?";
	public static String updateSessionSql = "update logsession set sessionid = ? where logusersid = ?";
}
