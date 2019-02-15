package com.wolf.permi.sql;

public class Sqls {

	public static String getSystemPermisSql = "select * from system_permi where systemname = 'system'";
	
	public static String getRolePermiSql = "select t3.datas,t3.datatype from system_role_permi t1 join system_permi t2 "
			+ "on t1.permiid = t2.id and isend = '1' " 
			+"join system_permi_data t3 on t1.permiid = t3.permiid where t1.roleid in (${roleid}) and t3.dataclass = ?";
	
	public static String save_system_permiSql = "insert into system_permi values(?,?,?,?,?)";
	public static String save_system_permi_dataSql = "insert into system_permi_data values(?,?,?,?)";
	public static String save_system_role_permiSql = "insert into system_role_permi values(?,?,?)";
	
}
