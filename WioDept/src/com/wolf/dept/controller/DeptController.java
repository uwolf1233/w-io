package com.wolf.dept.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.wolf.dept.dao.DeptDao;
import com.wolf.dept.javaBean.DeptBean;
import com.wolf.dept.javaBean.DeptPropertyBean;
import com.wolf.dept.javaBean.DeptTypeBean;
import com.wolf.dept.javaBean.StaticBeans;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.server.MethodMapping;

public class DeptController {

	@MethodMapping(path="/systemShow",interfacetype="h",threadPoolNumMax=2)//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,SessionLine sessionLine){
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/dept_getAllDept",threadPoolNumMax=5,interfacetype="sh")
	public void getAllDept(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseKV("deptDatas", DeptDao.INSTANCE.getAllRole(rrb));
	}
	
	@MethodMapping(path="/dept_getDeptFromIdHttp",threadPoolNumMax=5,interfacetype="h")
	public void getRoleFromIdHttp(ReqResBean rrb,SessionLine sessionLine){
		String id = rrb.getRequestMap().get("id");
		//rrb.setResponseKV("jsondata", StaticBean.gson.toJson(RoleDao.INSTANCE.getAllRole(rrb)));//jsondata,jsonArraydata
	}
	
	@MethodMapping(path="/dept_addDept",threadPoolNumMax=5,interfacetype="h",lockNames="deptHandleLock"
			,geWait=8,trans=true)
	public void addDept(ReqResBean rrb,SessionLine sessionLine){
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(rrb.getRequestMap());
		String id = UUID.randomUUID().toString().replace("-", "");
		map.put("id", id);
		String[] ids = new String[]{id};
		int z = DeptDao.INSTANCE.addDept(map,rrb,ids);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			DeptDao.INSTANCE.refreshDepts(rrb);//不能在add里面写查询，因为有事务
			DeptBean.setOtherData();
			map.clear();
			map.put("retBean", DeptBean.getFromIds(ids, rrb));
			map.put("type", "success");
		}
		rrb.setResponseData(StaticBeans.gson.toJson(map));
	}
	
	@MethodMapping(path="/dept_getDeptOtherDatas",interfacetype="h")
	public void getDeptOtherDatas(ReqResBean rrb,SessionLine sessionLine){
		Set<DeptTypeBean> deptTypeSet = DeptTypeBean.deptTypeSet;
		Set<DeptPropertyBean> deptPropertySet = DeptPropertyBean.deptPropertySet;
		rrb.setResponseKV("deptTypeBean", StaticBeans.gson.toJson(deptTypeSet));
		rrb.setResponseKV("deptPropertyBean", StaticBeans.gson.toJson(deptPropertySet));
	}
	
	@MethodMapping(path="/dept_getOneDeptRole",interfacetype="h")
	public void getOneDeptRole(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseKV("data", DeptDao.INSTANCE.getDeptOneData(rrb.getRequestMap().get("id"), rrb));
	}
	
	@MethodMapping(path="/dept_updateDept",interfacetype="h",trans=true,lockNames="deptHandleLock")
	public void updateDept(ReqResBean rrb,SessionLine sessionLine){
		Map<String,Object> map = new HashMap<String,Object>();
		map.putAll(rrb.getRequestMap());
		map.remove("pid");
		int z = DeptDao.INSTANCE.updateDept(map,rrb);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			DeptDao.INSTANCE.refreshDepts(rrb);//不能在update里面刷新，因为有事务
			DeptBean.setOtherData();
		}
		String id = map.get("id")+"";
		map.clear();
		map.put("retBean", DeptBean.getFromIds(new String[]{id}, rrb));
		map.put("type", "success");
		rrb.setResponseData(StaticBeans.gson.toJson(map));//返回数据
	}
	
	@MethodMapping(path="/dept_deleteDatas",interfacetype="h",trans=true,lockNames="deptHandleLock")
	public void deleteDatas(ReqResBean rrb,SessionLine sessionLine){
		String ids = rrb.getRequestMap().get("id");
		String idArray[] = ids.split(",");
		int z = DeptDao.INSTANCE.delete(idArray, rrb);
		if(z > 0){
			rrb.thransThread(1);//注意死锁
			DeptDao.INSTANCE.refreshDepts(rrb);//不能在update里面刷新，因为有事务
			DeptBean.setOtherData();
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", "success");
		rrb.setResponseData(StaticBeans.gson.toJson(map));//返回数据
	}
	
	@MethodMapping(path="/dept_deptSetUser",interfacetype="h")
	public void deptSetUser(ReqResBean rrb,SessionLine sessionLine){
		String deptid = rrb.getRequestMap().get("deptid");
		String userids = rrb.getRequestMap().get("userids");
		String userid[] = userids.split(",");
		int i = DeptDao.INSTANCE.deptSetUser(deptid, userid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		if(i > 0){
			map.put("type", "1");
		}else{
			map.put("type", "0");
		}
		rrb.setResponseMap(map);
	}
	
	@MethodMapping(path="/dept_deptGetUsers",interfacetype="h")
	public void deptGetUsers(ReqResBean rrb,SessionLine sessionLine){
		String deptid = rrb.getRequestMap().get("deptid");
		String s = DeptDao.INSTANCE.deptGetUsers(deptid, rrb);
		Map<String,String> map = new HashMap<String,String>();
		map.put("data", s);
		rrb.setResponseData(s);
	}
	
	@MethodMapping(path="/dept_userGetDeptId",interfacetype="sh")
	public void userGetDeptId(ReqResBean rrb,SessionLine sessionLine){
		String userid = rrb.getRequestMap().get("userid");
		String s = DeptDao.INSTANCE.userGetDeptId(userid, rrb);
		rrb.setResponseData("dept:"+s);
	}
	
}

