package com.wolf.controller;

import java.util.HashMap;
import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.server.MethodMapping;
import com.wolf.server.OtherServer;

public class Server1Controller {
	
	@MethodMapping(path="/wstest",interfacetype="w")
	public void wstest(ReqResBean rrb,SessionLine sessionLine){//ws示例
		String s = rrb.getWebSocketBean().getInMessage();
		System.out.println(s);
		rrb.sendWebSocket(rrb.getWebSocketBean(),"返回数据:"+s);
		rrb.sendWebSocket(rrb.getWebSocketGroup(rrb.getWebSocketBean()),"返回数据:"+s);
	}
	
	@MethodMapping(path="/getConfig",interfacetype="w",isWebSocket=true)
	public void getConfig(ReqResBean rrb,Map<String,String> sessionMap){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("serverName", OtherServer.serverName);
		map.put("threadPoolNum", OtherServer.threadPoolNum);//业务池大小
		map.put("threadPoolNumMax", OtherServer.threadPoolNumMax);//业务池最大
		map.put("ip", OtherServer.regIp);
		map.put("port", OtherServer.regPort);
		map.put("groupNum", OtherServer.groupNum);
		//rrb.setResponseKV(OtherServer.serverName, StaticBean.gson.toJson(map));
		rrb.sendWebSocket(rrb.getWebSocketGroup(rrb.getWebSocketBean()),StaticBean.gson.toJson(map));//按组发
	}
	
	@MethodMapping(path="/configHtml",interfacetype="h")
	public String configHtml(ReqResBean rrb,Map<String,String> sessionMap){
		return "/SystemShow/SystemShow";
	}
	
//	@MethodMapping(path="/testSession",interfacetype="h")
//	public void testSession(ReqResBean rrb,SessionLine sessionLine){
//		Map<String,Object> map = new HashMap<String,Object>();
//		map.put("username", "abc");
//		sessionLine.create(map);
//		String sessionId = sessionLine.getSessionId();
//		System.out.println("得到的sessionId=="+sessionId);
//		System.out.println("session数据=="+sessionLine.getAttr("username"));
//		sessionLine.setAttr("pwd", "123456");
//		System.out.println("session pwd数据=="+sessionLine.getAttr("pwd"));
//		sessionLine.removeAttr("username");
//		System.out.println("session username数据=="+sessionLine.getAttr("username"));
//		sessionLine.clearAttr();
//		System.out.println("session pwd数据=="+sessionLine.getAttr("pwd"));
//		sessionLine.setAttr("pwd", "123456");
//		System.out.println("session pwd数据=="+sessionLine.getAttr("pwd"));
//	}
	
}
