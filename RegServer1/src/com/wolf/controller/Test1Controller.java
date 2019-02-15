package com.wolf.controller;

import java.util.HashMap;
import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.server.MethodMapping;
import com.wolf.serverLine.ServerRequestBean;

public class Test1Controller {

	@MethodMapping(path="/httpinterface",interfacetype="h")//基本测试,全局负载均衡
	public void i1(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("1111server1111");
			ServerRequestBean srb = new ServerRequestBean();
			Map<String,String> map = new HashMap<String,String>();
			map.put("server1message", "server1 say test2");
			Object o = srb.send("/ninterface", map, true, rrb);
			rrb.setResponseKV("aa", "aa1_testa1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/ninterface",interfacetype="s")//基本测试,全局负载均衡
	public void i2(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("1111server11112");
		rrb.setResponseKV("aa", "aa1_testa1");
	}
	
	@MethodMapping(path="/winterface",interfacetype="w",isWebSocket=true)//定义接口类型和是否websocket，目前两个都需要定义
	public void i3(ReqResBean rrb,SessionLine sessionLine){
		System.out.println("1111server11113");
		rrb.sendWebSocket(rrb.getWebSocketBean(), "websocket2 bean test");//单个发
	}
	
	@MethodMapping(path="/httpinterface1",interfacetype="h")//基本测试,全局负载均衡
	public void i4(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("1111server11112");
		rrb.setResponseKV("aa", "aa1_testa1");
	}
	
}
