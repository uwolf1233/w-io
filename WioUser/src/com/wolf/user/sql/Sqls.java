package com.wolf.user.sql;

public class Sqls {

	public static String getSystemUsersSql = "select * from system_users where systemname = 'system'";
	public static String insertUserSql = "insert into system_users(id,name,systemname,pid,isend) values(?,?,?,?,?)";
	public static String updateUserSql = "update system_users set name=?,isend=? where id=? and systemname=?";
	public static String deleteUserSql = "delete from system_users where id = ?";
	
	public static String deleteSystemDeptUserSql = "delete from system_user_dept where systemname = 'system' and userid = ?";
	public static String insertSystemDeptUserSql = "insert into system_user_dept(deptid,userid,systemname) values(?,?,'system')";
	
	public static String deleteSystemRoleUserSql = "delete from system_user_role where systemname = 'system' and userid = ?";
	public static String insertSystemRoleUserSql = "insert into system_user_role(roleid,userid,systemname) values(?,?,'system')";
	
	public static String getRolessFromUser = "select roleid from system_user_role where systemname='system' and userid=?";
	
	public static String hasUserSql = "select count(*) c from system_users where name = ?";
}
