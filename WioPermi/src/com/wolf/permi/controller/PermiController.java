package com.wolf.permi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.permi.dao.PermiDao;
import com.wolf.permi.javaBean.StaticBeans;
import com.wolf.server.MethodMapping;
import com.wolf.serverLine.ServerRequestBean;

public class PermiController {

	@MethodMapping(path="/systemShow",interfacetype="h",threadPoolNumMax=2)//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,Map<String,String> sessionMap){
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/permi_getJsonFromIds",threadPoolNumMax=5,interfacetype="s")//获取权限节点
	public void getJsonFromIds(ReqResBean rrb,Map<String,String> sessionMap){
//		String 
//		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(PermiDao.INSTANCE.getPermis()));//jsondata,jsonArraydata
	}
	
	@MethodMapping(path="/permi_getAllPermi",threadPoolNumMax=5,interfacetype="h")
	public void getAllPermi(ReqResBean rrb,Map<String,String> sessionMap){
		Map<String,Object> map = new HashMap<String,Object>();
		ServerRequestBean srb = new ServerRequestBean();
		String roleDatas = "";
		try {
			BlockingQueue queue = new ArrayBlockingQueue<Object>(2);
			srb.send("/role_getAllRole", new HashMap<String,String>(), rrb,queue);
			srb.send("/user_getAllUser", new HashMap<String,String>(), rrb,queue);
			srb.send("/dept_getAllDept", new HashMap<String,String>(), rrb,queue);
			Object roleObj = queue.poll(1500, TimeUnit.SECONDS);
			Object userObj = queue.poll(1500, TimeUnit.SECONDS);
			Object deptObj = queue.poll(1500, TimeUnit.SECONDS);
			rrb.setResponseMap((Map<String,String>)roleObj);
			rrb.setResponseMap((Map<String,String>)userObj);
			rrb.setResponseMap((Map<String,String>)deptObj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rrb.setResponseKV("permiDatas", PermiDao.INSTANCE.getAllPermi(rrb));
	}
	
	@MethodMapping(path="/permi_getPermiFromIdHttp",threadPoolNumMax=5,interfacetype="h")
	public void getPermiFromIdHttp(ReqResBean rrb,Map<String,String> sessionMap){
		String id = rrb.getRequestMap().get("id");
		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(PermiDao.INSTANCE.getPermisToJson(new String[]{id}, rrb)));//jsondata,jsonArraydata
	}
	
//	@MethodMapping(path="/permi_getPermiFromIdsServer",threadPoolNumMax=5,interfacetype="s")//提供给内部调用，根据roleid组进行获取
//	public void getPermiFromIdsServer(ReqResBean rrb,Map<String,String> sessionMap){
//		String ids = rrb.getRequestMap().get("roleids");
//		String[] idArray = ids.split(",");//逗号分隔
//		rrb.setResponseKV("jsondata", StaticBean.gson.toJson(PermiDao.INSTANCE.getPermisToJson(idArray, rrb)));//jsondata,jsonArraydata
//	}
	
	@MethodMapping(path="/permi_getPermiFromIds",threadPoolNumMax=500,interfacetype="s")//提供给内部调用，根据roleid组进行获取
	public void getPermiFromIds(ReqResBean rrb,SessionLine sessionLine){
		String ids = rrb.getRequestMap().get("roleids");
		String[] idArray = ids.split(",");//逗号分隔
		String dataClass = rrb.getRequestMap().get("dataclass");
		Map<String,String> map = PermiDao.INSTANCE.getPermiFromIds(idArray, dataClass, rrb);
		rrb.setResponseMap(map);
	}
	
	@MethodMapping(path="/permi_savePermi",threadPoolNumMax=5,interfacetype="h",trans = true)
	public void savePermi(ReqResBean rrb,SessionLine sessionLine){
		try {
			String jsonData = rrb.getRequestMap().get("jsonData");
			List<Map<String,Object>> dataLm = StaticBeans.gson.fromJson(jsonData, new TypeToken<List<Map<String,Object>>>(){}.getType());
			int dataLmSize = dataLm.size();
			boolean iscommit = true;
			for(int i=0;i<dataLmSize;i++){
				Map<String,Object> map = dataLm.get(i);
				String permiid = UUID.randomUUID().toString().replace("-", "");
				String name = map.get("name")+"";
				String systemname = map.get("systemname")+"";
				String pid = map.get("pid")+"";
				String isend = map.get("isend")+"";
				String datas = map.get("datas")+"";
				String datatype = map.get("datatype")+"";
				String dataclass = map.get("dataclass")+"";
				String roleid = map.get("roleid")+"";
				int a = PermiDao.INSTANCE.savePermi(permiid, name, systemname, pid, isend, datas, datatype, dataclass, roleid,rrb);
				if(a == 0){
					iscommit = false;
					break;
				}
			}
			if(iscommit){
				rrb.thransThread(1);
				rrb.setResponseKV("type", "success");
			}else{
				rrb.thransThread(0);
				rrb.setResponseKV("type", "error");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.thransThread(0);
			rrb.setResponseKV("type", "error");
		}
	}
	
}













