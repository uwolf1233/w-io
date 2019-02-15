package com.wolf.user.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.TwoTranListener;
import com.wolf.server.MethodMapping;
import com.wolf.user.dao.UserDao;
import com.wolf.user.javaBean.StaticBeans;
import com.wolf.user.javaBean.UserBean;

public class UserController {

	@MethodMapping(path="/systemShow",interfacetype="h",threadPoolNumMax=2)//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,SessionLine sessionLine){
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/user_getAllUser",threadPoolNumMax=5,interfacetype="sh")
	public void getAllUser(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseKV("userDatas", UserDao.INSTANCE.getAllUser(rrb));//转过的不要再转，否则前端会解析不到
	}
	
	@MethodMapping(path="/user_getUserFromIdHttp",threadPoolNumMax=5,interfacetype="h")
	public void getUserFromIdHttp(ReqResBean rrb,SessionLine sessionLine){
		String id = rrb.getRequestMap().get("id");
		//rrb.setResponseKV("jsondata", StaticBean.gson.toJson(RoleDao.INSTANCE.getAllRole(rrb)));//jsondata,jsonArraydata
	}
	
	@MethodMapping(path="/user_addUser",threadPoolNumMax=5,interfacetype="sh",lockNames="userHandleLock"
			,geWait=8,trans=true)
	public void addUser(final ReqResBean rrb,SessionLine sessionLine){
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(rrb.getRequestMap());
		String id = UUID.randomUUID().toString().replace("-", "");
		map.put("id", id);
		String[] ids = new String[]{id};
		UserDao.INSTANCE.addUser(map,rrb,ids);
		if(map.get("type").equals("success")){
			rrb.thransThread(1);//注意死锁
			rrb.setTwoTranListener(new TwoTranListener() {
				
				@Override
				public void commitOrRollback(String type) {
					// TODO Auto-generated method stub
					if(type.equals("commit")){
						UserDao.INSTANCE.refreshUsers(rrb);
					}
				}
			});
		}
		rrb.setResponseData(StaticBeans.gson.toJson(map));
	}
	
	@MethodMapping(path="/user_getOneUserRole",interfacetype="h")
	public void getOneDeptRole(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseKV("data", UserDao.INSTANCE.getUserOneData(rrb.getRequestMap().get("id"), rrb));
	}
	
	@MethodMapping(path="/user_updateUser",interfacetype="h",trans=true,lockNames="userHandleLock")
	public void updateDept(ReqResBean rrb,SessionLine sessionLine){
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(rrb.getRequestMap());
		map.remove("pid");
		int z = UserDao.INSTANCE.updateUser(map,rrb);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			UserDao.INSTANCE.refreshUsers(rrb); //不能在update里面刷新，因为有事务
		}
		String id = map.get("id")+"";
		map.clear();
		map.put("retBean", UserBean.getFromIds(new String[]{id}, rrb));
		map.put("type", "success");
		rrb.setResponseData(StaticBeans.gson.toJson(map));//返回数据
	}
	
	@MethodMapping(path="/user_deleteDatas",interfacetype="h",trans=true,lockNames="userHandleLock")
	public void deleteDatas(ReqResBean rrb,SessionLine sessionLine){
		String ids = rrb.getRequestMap().get("id");
		String idArray[] = ids.split(",");
		int z = UserDao.INSTANCE.delete(idArray, rrb);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			UserDao.INSTANCE.refreshUsers(rrb);//不能在update里面刷新，因为有事务
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", "success");
		rrb.setResponseData(StaticBeans.gson.toJson(map));//返回数据
	}
	
	@MethodMapping(path="/user_userSetDept",interfacetype="h")
	public void userSetDept(ReqResBean rrb,SessionLine sessionLine){
		String deptids = rrb.getRequestMap().get("deptids");
		String userid = rrb.getRequestMap().get("userid");
		String deptid[] = deptids.split(",");
		int i = UserDao.INSTANCE.userSetDept(deptid, userid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		if(i > 0){
			map.put("type", "1");
		}else{
			map.put("type", "0");
		}
		rrb.setResponseMap(map);
	}
	
	@MethodMapping(path="/user_userSetRole",interfacetype="h")
	public void userSetRole(ReqResBean rrb,SessionLine sessionLine){
		String roleids = rrb.getRequestMap().get("roleids");
		String userid = rrb.getRequestMap().get("userid");
		String roleid[] = roleids.split(",");
		int i = UserDao.INSTANCE.userSetRole(roleid, userid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		if(i > 0){
			map.put("type", "1");
		}else{
			map.put("type", "0");
		}
		rrb.setResponseMap(map);
	}
	
	@MethodMapping(path="/user_userGetRoles",interfacetype="h")
	public void deptGetUsers(ReqResBean rrb,SessionLine sessionLine){
		String userid = rrb.getRequestMap().get("userid");
		String s = UserDao.INSTANCE.userGetRoles(userid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		map.put("data", s);
		rrb.setResponseData(s);
	}
	
	@MethodMapping(path="/user_getOneUserFromName",interfacetype="s")
	public void getOneUserFromName(ReqResBean rrb,SessionLine sessionLine){
		try {
			String name = rrb.getRequestMap().get("name");
			String systemname = rrb.getRequestMap().get("systemname");
			UserBean userBean = UserDao.INSTANCE.getMapFromName(name, systemname, rrb);
			if(userBean == null){
				rrb.setResponseKV("type", "error");
			}else{
				rrb.setResponseKV("userDatas", StaticBeans.gson.toJson(userBean));
				rrb.setResponseKV("type", "success");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.setResponseKV("type", "error");
		}
	}
	
	@MethodMapping(path="/user_hasUserFromName",interfacetype="s")
	public void hasUserFromName(ReqResBean rrb,SessionLine sessionLine){
		try{
			String name = rrb.getRequestMap().get("name");
			String systemname = rrb.getRequestMap().get("systemname");
			UserBean userBean = UserDao.INSTANCE.getMapFromName(name, systemname, rrb);
			if(userBean != null){
				rrb.setResponseData("success");
			}else{
				rrb.setResponseData("error");
			}
		}catch(Exception e){
			e.printStackTrace();
			rrb.setResponseData("error");
		}
	}
	
}
