package com.wolf.role.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.role.dao.RoleDao;
import com.wolf.role.javaBean.RoleBean;
import com.wolf.role.javaBean.StaticBeans;
import com.wolf.server.MethodMapping;
import com.wolf.serverLine.ServerRequestBean;

public class RoleController {

	@MethodMapping(path="/systemShow",interfacetype="h",threadPoolNumMax=2)//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,SessionLine sessionLine){
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/role_getAllRole",threadPoolNumMax=5,interfacetype="sh")
	public void getAllRole(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseKV("roleDatas", RoleDao.INSTANCE.getAllRole(rrb));//转过的不要再转，否则前端会解析不到
		//rrb.setResponseData(StaticBean.gson.toJson(RoleDao.INSTANCE.getAllRole(rrb)));
		//rrb.setResponseKV("jsondata", StaticBean.gson.toJson(RoleDao.INSTANCE.getAllRole(rrb)));//jsondata,jsonArraydata
	}
	
	@MethodMapping(path="/role_getRoleFromIdHttp",threadPoolNumMax=5,interfacetype="h")
	public void getRoleFromIdHttp(ReqResBean rrb,SessionLine sessionLine){
		String id = rrb.getRequestMap().get("id");
		//rrb.setResponseKV("jsondata", StaticBean.gson.toJson(RoleDao.INSTANCE.getAllRole(rrb)));//jsondata,jsonArraydata
	}
	
	@MethodMapping(path="/role_addRole",threadPoolNumMax=5,interfacetype="h",lockNames="roleHandleLock"
			,geWait=8,trans=true)
	public void addUser(ReqResBean rrb,SessionLine sessionLine){
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(rrb.getRequestMap());
		String id = UUID.randomUUID().toString().replace("-", "");
		map.put("id", id);
		String[] ids = new String[]{id};
		int z = RoleDao.INSTANCE.addRole(map,rrb,ids);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			RoleDao.INSTANCE.refreshRoles(rrb);//不能在add里面写查询，因为有事务
			map.clear();
			map.put("retBean", RoleBean.getFromIds(ids, rrb));
			map.put("type", "success");
		}
		rrb.setResponseData(StaticBeans.gson.toJson(map));
	}
	
	@MethodMapping(path="/role_getOneRoleRole",interfacetype="h")
	public void getOneDeptRole(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseKV("data", RoleDao.INSTANCE.getRoleOneData(rrb.getRequestMap().get("id"), rrb));
	}
	
	@MethodMapping(path="/role_updateRole",interfacetype="h",trans=true,lockNames="roleHandleLock")
	public void updateDept(ReqResBean rrb,SessionLine sessionLine){
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(rrb.getRequestMap());
		map.remove("pid");
		int z = RoleDao.INSTANCE.updateRole(map,rrb);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			RoleDao.INSTANCE.refreshRoles(rrb); //不能在update里面刷新，因为有事务
		}
		String id = map.get("id")+"";
		map.clear();
		map.put("retBean", RoleBean.getFromIds(new String[]{id}, rrb));
		map.put("type", "success");
		rrb.setResponseData(StaticBeans.gson.toJson(map));//返回数据
	}
	
	@MethodMapping(path="/role_deleteDatas",interfacetype="h",trans=true,lockNames="roleHandleLock")
	public void deleteDatas(ReqResBean rrb,SessionLine sessionLine){
		String ids = rrb.getRequestMap().get("id");
		String idArray[] = ids.split(",");
		int z = RoleDao.INSTANCE.delete(idArray, rrb);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			RoleDao.INSTANCE.refreshRoles(rrb);//不能在update里面刷新，因为有事务
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", "success");
		rrb.setResponseData(StaticBeans.gson.toJson(map));//返回数据
	}
	
	@MethodMapping(path="/role_roleSetUser",interfacetype="h")
	public void roleSetUser(ReqResBean rrb,SessionLine sessionLine){
		String userids = rrb.getRequestMap().get("userids");
		String roleid = rrb.getRequestMap().get("roleid");
		String userid[] = userids.split(",");
		int i = RoleDao.INSTANCE.roleSetUser(userid, roleid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		if(i > 0){
			map.put("type", "1");
		}else{
			map.put("type", "0");
		}
		rrb.setResponseMap(map);
	}
	
	@MethodMapping(path="/role_roleSetPermi",interfacetype="h")
	public void roleSetPermi(ReqResBean rrb,SessionLine sessionLine){
		String permiids = rrb.getRequestMap().get("permiids");
		String roleid = rrb.getRequestMap().get("roleid");
		String permiid[] = permiids.split(",");
		int i = RoleDao.INSTANCE.roleSetPermi(permiid, roleid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		if(i > 0){
			map.put("type", "1");
		}else{
			map.put("type", "0");
		}
		rrb.setResponseMap(map);
	}
	
	@MethodMapping(path="/role_roleGetPermis",interfacetype="h")
	public void roleGetPermis(ReqResBean rrb,SessionLine sessionLine){
		String userid = rrb.getRequestMap().get("roleid");
		String s = RoleDao.INSTANCE.roleGetPermis(userid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		map.put("data", s);
		rrb.setResponseData(s);
	}
	
//	@MethodMapping(path="/role_getDataFromUserid",interfacetype="s")
//	public void getDataFromUserid(ReqResBean rrb,SessionLine sessionLine){
//		String userid = rrb.getRequestMap().get("userid");
//		String roleids = RoleDao.INSTANCE.getDataFromUserid(userid, rrb);
//		rrb.setResponseData("roleid:"+roleids);
//	}
	
	@MethodMapping(path="/role_getPermiFromUserid",interfacetype="sh")
	public void getPermiFromUserid(ReqResBean rrb,SessionLine sessionLine){
		try {
			String systemname = rrb.getRequestMap().get("systemname");
			String userid = rrb.getRequestMap().get("userid");
			String dataclass = rrb.getRequestMap().get("dataclass");
			String roleids = RoleDao.INSTANCE.getDataFromUserid(userid,systemname,rrb);
			ServerRequestBean srb = new ServerRequestBean();
			Map<String,String> map = new HashMap<String,String>();
			map.put("roleids", roleids);
			map.put("dataclass", dataclass);
			Object o = srb.send("/permi_getPermiFromIds", map, true, rrb);
			Map<String,String> maps = (Map<String,String>)o;
			rrb.setResponseMap(maps);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.setResponseData("error");
		}
	}
	
	@MethodMapping(path="/role_getPermiFromSession",interfacetype="sh")
	public void getPermiFromSession(ReqResBean rrb,SessionLine sessionLine){//从session中获取权限
		try {
			Object hasSession = sessionLine.hasSessionRun(null);
			if(hasSession!=null && hasSession.equals("success")){
				Object userid = sessionLine.getAttrRun("userid", null);
				if(userid!=null){
					String systemname = rrb.getRequestMap().get("systemname");
					String dataclass = rrb.getRequestMap().get("dataclass");
					String roleids = RoleDao.INSTANCE.getDataFromUserid(userid+"",systemname,rrb);
					ServerRequestBean srb = new ServerRequestBean();
					Map<String,String> map = new HashMap<String,String>();
					map.put("roleids", roleids);
					map.put("dataclass", dataclass);
					Object o = srb.send("/permi_getPermiFromIds", map, true, rrb);
					Map<String,String> maps = (Map<String,String>)o;
					map.clear();//用hashmap接口避免一些错误
					map.putAll(maps);
					map.put("type", "success");
					rrb.setResponseMap(map);
				}else{
					Map<String,String> map = new HashMap<String,String>();
					map.put("type", "nosession");
					rrb.setResponseMap(map);
				}
			}else{
				Map<String,String> map = new HashMap<String,String>();
				map.put("type", "nosession");
				rrb.setResponseMap(map);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Map<String,String> map = new HashMap<String,String>();
			map.put("type", "norole");
			rrb.setResponseMap(map);
		}
	}
	
}
