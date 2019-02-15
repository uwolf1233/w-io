package com.wolf.role.controller;

import java.util.HashMap;
import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.StaticBean;
import com.wolf.server.MethodMapping;
import com.wolf.server.OtherServer;

public class Server1Controller {
	
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
	
}
