package com.wolf.controller;

import java.util.HashMap;
import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.server.MethodMapping;
import com.wolf.serverLine.ServerRequestBean;

public class LogTestController {

	@MethodMapping(path="/logtest1",threadPoolNumMax=30,interfacetype="h",trans=true)//基本测试,路径负载均衡,调用本地方法
	public void logtest1(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			rrb.setResponseKV("aa", "aa1_testa3");
			Map<String,String> map = new HashMap<String,String>();
			map.put("server1message", "server1 say test1");
			ServerRequestBean srb = new ServerRequestBean();
			Object o = srb.send("/logtest2", map, true,rrb);
			System.out.println("testa5==="+o);
			rrb.thransThread(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/logtest2",threadPoolNumMax=40,trans=true)//基本测试,路径负载均衡,调用本地方法，被调用，异步
	public void logtest2(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			System.out.println("requestData===="+rrb.getRequestMap());
			ServerRequestBean srb = new ServerRequestBean();
			Map<String,String> map = new HashMap<String,String>();
			map.put("server1message", "server1 say test2");
			Object o = srb.send("/logtest3", map, true,rrb);
			System.out.println("logtest3==="+o);
			rrb.setResponseKV("aa", "aa1_testa4");
			rrb.thransThread(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/logtest3",threadPoolNumMax=50,trans=true)//基本测试,路径负载均衡,调用本地方法，被调用，同步
	public void testa5(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		System.out.println("requestData===="+rrb.getRequestMap());
		rrb.setResponseKV("aa", "aa1_testa5");
		rrb.thransThread(1);
	}
	
}
