package com.wolf.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import com.google.gson.reflect.TypeToken;
import com.wolf.dao.UserDao;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.server.MethodMapping;
import com.wolf.serverLine.ServerRequestBean;

public class LoginController {

	@MethodMapping(path="/login_login",interfacetype="h",trans=true)
	public void login(ReqResBean rrb,SessionLine sessionLine){//登录,1是登录成功，2是已经登录过，-2是登录异常，0是登录失败
		try {
			String username = rrb.getRequestMap().get("username");
			ServerRequestBean srb = new ServerRequestBean();
			Map<String,String> map = new HashMap<String,String>();
			map.put("systemname", "system");
			map.put("name", username);
			Object o = srb.send("/user_getOneUserFromName", map, true, rrb);
			Map<String,Object> retmap = StaticBean.gson.fromJson(o.toString(), new TypeToken<Map<String,Object>>(){}.getType());
			Map<String,Object> userDatas = (Map<String,Object>)retmap.get("userDatas");
			String userid = userDatas.get("id")+"";
			if(retmap.get("type").equals("error")){
				rrb.setResponseKV("type", "-2");//-2为登录异常
				rrb.thransThread(0);
			}
			String pwd = rrb.getRequestMap().get("pwd");
			//保存
			int i = UserDao.INSTANCE.boolUser(pwd, username,sessionLine,rrb);
			if(i == -1 || i == 2){
				rrb.thransThread(0);
				rrb.setResponseKV("type", i+"");//-2为登录异常,2为已经登录过
				if(i == 2){
					rrb.addCookie("wsessionId", sessionLine.getSessionId());
				}
				return;
			}else if(i == 1){
				rrb.setResponseKV("type", i+"");
				rrb.thransThread(1);//外面做事务
				rrb.addCookie("wsessionId", sessionLine.getSessionId());
			}
			map.clear();
			map.put("userid", userid);
			BlockingQueue queue = new ArrayBlockingQueue<Object>(1);
			srb.send("/dept_userGetDeptId", map, rrb,queue);
			//srb.send("/role_getDataFromUserid", map, rrb,queue);
			for(int a=0;a<1;a++){
				Object o1 = queue.poll(2, TimeUnit.SECONDS);
				String o1Str = o1+"";
				if(o1Str.startsWith("dept:")){
					String deptid = o1Str.replace("dept:", "");
					if(!deptid.equals("error")){
						sessionLine.setAttr("deptid", deptid,null);
					}
				}
//				else if(o1Str.startsWith("roleid:")){
//					String roleid = o1Str.replace("roleid:", "");
//					if(!roleid.equals("error")){
//						sessionLine.setAttr("roleid", roleid,null);
//					}
//				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.thransThread(0);
			rrb.setResponseKV("type", "-2");//-2为登录异常
		}finally{
//			long endtime = System.currentTimeMillis();
//			System.out.println("16执行时间---"+(endtime-starttime));
		}
	}
	
	@MethodMapping(path="/login_regUser",interfacetype="h",trans=true)
	public void regUser(ReqResBean rrb,SessionLine sessionLine){//注册
		String username = rrb.getRequestMap().get("username");
		String pwd = rrb.getRequestMap().get("pwd");
		String systemname = rrb.getRequestMap().get("systemname");
		try {
			ServerRequestBean srb = new ServerRequestBean();
			Map<String,String> map = new HashMap<String,String>();
			map.put("name", username);
			map.put("systemname", "system");
			map.put("pid", "1");
			map.put("isend", "1");
			Object o = srb.send("/user_addUser", map, true, rrb);
			if(o != null){
				Map<String,Object> retmap = StaticBean.gson.fromJson(o.toString(), new TypeToken<Map<String,Object>>(){}.getType());
				if(retmap.get("type").equals("success")){
					UserDao.INSTANCE.saveUser(retmap.get("id")+"",username, pwd, systemname, rrb);
					rrb.thransThread(1);
					rrb.setResponseKV("type", "success");
				}else{
					rrb.thransThread(0);
					rrb.setResponseKV("type", "error");
					if(retmap.containsKey("data")){
						rrb.setResponseKV("data", retmap.get("data")+"");
					}
				}
			}
			else{
				rrb.thransThread(0);
				rrb.setResponseKV("type", "error");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.thransThread(0);
			rrb.setResponseKV("type", e.toString());
		}
	}
	
	@MethodMapping(path="/login_logout",interfacetype="h")
	public void logout(ReqResBean rrb,SessionLine sessionLine){
		String username = sessionLine.getAttrRun("username",null)+"";
		if(username != null && !username.equals("sessionNull")){
			if(UserDao.INSTANCE.outUser(username, rrb) > 0){
				sessionLine.reset();
			}
		}
		rrb.setResponseKV("type", "success");
	}
	
//	@MethodMapping(path="/login_getSessionDataTest",interfacetype="h")
//	public void getSessionDataTest(ReqResBean rrb,SessionLine sessionLine){//测试获取用户数据
//		String userid = sessionLine.getAttr("userid",null)+"";
//		String username = sessionLine.getAttr("username",null)+"";
//		String deptid = sessionLine.getAttr("deptid",)+"";
//		rrb.setResponseKV("data", userid+"----"+username+"-----"+deptid);
//	}
	
}




