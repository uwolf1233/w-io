package com.wolf.dept.sql;

public class Sqls {

	public static String getSystemDeptsSql = "select * from system_dept where systemname = 'system'";
	public static String insertDeptSql = "insert into system_dept(id,name,depttype,deptproperty,systemname,pid,isend) values"
			+"(?,?,?,?,?,?,?)";
	public static String getSystemDepttypeSql = "select * from system_depttype where systemname = 'system'";
	public static String getSystemDeptPropertySql = "select * from system_deptproperty where systemname = 'system'";
	public static String updateDeptSql = "update system_dept set name=?,depttype=? ,deptproperty=?,isend=? where id=? and systemname=?";
	public static String deleteDeptSql = "delete from system_dept where id = ?";
	
	public static String deleteSystemUserDeptSql = "delete from system_user_dept where systemname = 'system' and deptid = ?";
	public static String insertSystemUserDeptSql = "insert into system_user_dept(deptid,userid,systemname) values(?,?,'system')";
	
	public static String getUsersFromDept = "select userid from system_user_dept where systemname='system' and deptid=?";
	
	public static String userGetDeptIdSql = "select deptid from system_user_dept where userid = ? and systemname = ?";
}
