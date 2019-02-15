package com.wolf.role.dao;

import java.util.List;

import com.wolf.jdbcs.MyJdbc;
import com.wolf.role.javaBean.Datas;
import com.wolf.role.javaBean.Permi;
import com.wolf.role.javaBean.Roles;
import com.wolf.role.javaBean.Users;

public class RolesDao {

	private RolesDao(){}
	
	public static RolesDao INSTANCE = new RolesDao();
	
	public static String datas_tableName = "datas";
	public static String permi_tableName = "permi";
	public static String roles_tableName = "roles";
	public static String users_tableName = "users";
	
	public void getRoles(){
		MyJdbc.INSTANCE.queryForList("select * from "+datas_tableName, null, Datas.class);
		MyJdbc.INSTANCE.queryForList("select * from "+permi_tableName, null, Permi.class);
		MyJdbc.INSTANCE.queryForList("select * from "+roles_tableName, null, Roles.class);
		MyJdbc.INSTANCE.queryForList("select * from "+users_tableName, null, Users.class);
	}
	
}
