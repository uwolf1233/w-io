package com.wolf.role.sql;

public class Sqls {

	public static String getSystemRolesSql = "select * from system_role where systemname = 'system'";
	public static String insertRoleSql = "insert into system_role(id,name,systemname,pid,isend) values(?,?,?,?,?)";
	public static String updateRoleSql = "update system_role set name=?,isend=? where id=? and systemname=?";
	public static String deleteRoleSql = "delete from system_role where id = ?";
	
	public static String deleteSystemRoleUserSql = "delete from system_user_role where systemname = 'system' and roleid = ?";
	public static String insertSystemRoleUserSql = "insert into system_user_role(roleid,userid,systemname) values(?,?,'system')";
	
	public static String deleteSystemRolePermiSql = "delete from system_role_permi where systemname = 'system' and roleid = ?";
	public static String insertSystemRolePermiSql = "insert into system_role_permi(permiid,roleid,systemname) values(?,?,'system')";
	
	public static String getPermisssFromRole = "select permiid from system_role_permi where systemname='system' and roleid=?";
	
	public static String getDataFromUseridSql = "select roleid from system_user_role where userid = ? and systemname = ?";
	
}
