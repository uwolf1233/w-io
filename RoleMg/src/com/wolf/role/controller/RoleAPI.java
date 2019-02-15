package com.wolf.role.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.StaticBean;
import com.wolf.role.javaBean.Datas;
import com.wolf.role.javaBean.Permi;
import com.wolf.role.javaBean.Roles;
import com.wolf.role.javaBean.Users;
import com.wolf.role.locks.AllLocks;
import com.wolf.server.MethodMapping;

public class RoleAPI {

	@MethodMapping(path="/nrole_userGetRoles",interfacetype="s")
	public void userGetRoles(ReqResBean rrb,Map<String,String> sessionMap){
		boolean flag = false;
		Map<String,Object> map = new HashMap<String,Object>();
		String userid = rrb.getRequestMap().get("userid");
		try {
			flag = AllLocks.usersRwLock.readLock().tryLock(10, TimeUnit.SECONDS);
			if(flag){
				try {
					map.putAll(Users.getRolsMg(userid,rrb));
					rrb.setResponseKV("jsondata", StaticBean.gson.toJson(map));//jsondata,jsonArraydata
					flag = true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					flag = false;
					map.put("type", "error");
					rrb.setResponseKV("jsondata", StaticBean.gson.toJson(map));//jsondata,jsonArraydata
				}finally{
					AllLocks.usersRwLock.readLock().unlock();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
