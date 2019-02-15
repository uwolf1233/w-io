package com.wolf.role.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.StaticBean;
import com.wolf.role.javaBean.Datas;
import com.wolf.role.javaBean.Permi;
import com.wolf.role.javaBean.Roles;
import com.wolf.role.javaBean.Users;
import com.wolf.server.MethodMapping;

public class RoleMgController {
	
	@MethodMapping(path="/systemShow",interfacetype="h")//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/role_getRoles",threadPoolNumMax=5,interfacetype="h")//获取权限节点
	public void getRoles(ReqResBean rrb,Map<String,String> sessionMap){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("users", Users.usersList);
		map.put("roles", Roles.rolesSet);
		map.put("permis", Permi.permiSet);
		map.put("datas", Datas.datasList);
		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(map));//jsondata,jsonArraydata
	}
	
	@MethodMapping(path="/role_getUserRole",interfacetype="h")
	public void getUserRole(ReqResBean rrb,Map<String,String> sessionMap){
		Map<String,Object> map = Users.getRolsMg(rrb.getRequestMap().get("id"), rrb);
		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(map));
	}
	
	@MethodMapping(path="/role_getRolePermiDatas",interfacetype="h")
	public void getRolePermiDatas(ReqResBean rrb,Map<String,String> sessionMap){
		Map<String,Object> map = Roles.getPermisDatas(rrb.getRequestMap().get("roleid"));
		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(map));
	}
	
	@MethodMapping(path="/role_getPermiDatas",interfacetype="h")
	public void getPermiDatas(ReqResBean rrb,Map<String,String> sessionMap){
		Map<String,Object> map = Permi.getDatas(rrb.getRequestMap().get("permiid"));
		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(map));
	}
	
	@MethodMapping(path="/role_userAddRole",interfacetype="h")
	public void userAddRole(ReqResBean rrb,Map<String,String> sessionMap){
		String userids = rrb.getRequestMap().get("userids");
		String roleids = rrb.getRequestMap().get("roleids");
		boolean flag = Users.userAddRole(userids, roleids);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
	
	@MethodMapping(path="/role_roleAddPermi",interfacetype="h")
	public void roleAddPermi(ReqResBean rrb,Map<String,String> sessionMap){
		String roleids = rrb.getRequestMap().get("roleids");
		String permiids = rrb.getRequestMap().get("permiids");
		boolean flag = Roles.roleAddPermi(roleids, permiids);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
	
	@MethodMapping(path="/role_permiAddDatas",interfacetype="h")
	public void permiAddDatas(ReqResBean rrb,Map<String,String> sessionMap){
		String permiid = rrb.getRequestMap().get("permiid");
		String dataids = rrb.getRequestMap().get("dataids");
		String[] dataidArray = dataids.split(",");
		List<String> dataidsList = Arrays.asList(dataidArray);
		boolean flag = Permi.add(Datas.getFromIds(dataidsList), permiid);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
	
	@MethodMapping(path="/role_removeUserFromName",interfacetype="h")
	public void removeUserFromName(ReqResBean rrb,Map<String,String> sessionMap){
		String name = rrb.getRequestMap().get("name");
		boolean flag = Users.removeUserFromName(name);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
	
	@MethodMapping(path="/role_removeRoleFromName",interfacetype="h")
	public void removeRoleFromName(ReqResBean rrb,Map<String,String> sessionMap){
		String name = rrb.getRequestMap().get("name");
		boolean flag = Roles.removeRoleFromName(name);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
	
	@MethodMapping(path="/role_removePermiFromName",interfacetype="h")
	public void removePermiFromName(ReqResBean rrb,Map<String,String> sessionMap){
		String name = rrb.getRequestMap().get("name");
		boolean flag = Permi.removePermiFromName(name);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
	
	@MethodMapping(path="/role_removeDatasFromIds",interfacetype="h")
	public void removeDatasFromIds(ReqResBean rrb,Map<String,String> sessionMap){
		String ids = rrb.getRequestMap().get("ids");
		String[] idsArray = ids.split(",");
		List<String> idsList = Arrays.asList(idsArray);
		boolean flag = Datas.removeDatasFromId(idsList);
		rrb.setResponseKV("jsondata", flag ? "success" : "error");
	}
}
