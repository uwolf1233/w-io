package com.wolf.im.controller;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.im.javabean.UsersBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.javabean.WebSocketBean;
import com.wolf.javabean.WebSocketBeanFilterI;
import com.wolf.javabean.WebSocketGroup;
import com.wolf.server.MethodMapping;

public class ImController {

	private static ConcurrentMap<String,WebSocketGroup> webSocketGroups = new ConcurrentHashMap<String,WebSocketGroup>();
	private static ConcurrentMap<WebSocketBean,String> webSockets = new ConcurrentHashMap<WebSocketBean,String>();
	
	private static ConcurrentMap<String,UsersBean> ubMap = new ConcurrentHashMap<String,UsersBean>();
	
	static{
		for(int i=0;i<10;i++){
			String uuid = UUID.randomUUID().toString().replace("-", "");
			UsersBean ub = new UsersBean();
			ub.setId(uuid);
			ubMap.put(uuid, ub);
		}
	}
	
	@MethodMapping(path="/im_getUsers",interfacetype="h")
	public void getUsers(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseData(StaticBean.gson.toJson(ubMap));
	}
	
	@MethodMapping(path="/wstest",interfacetype="w")
	public void wstest(ReqResBean rrb,SessionLine sessionLine){
		String s = rrb.getWebSocketBean().getInMessage();
		System.out.println(s);
		rrb.sendWebSocket(rrb.getWebSocketBean(),"返回数据:"+s);
		rrb.sendWebSocket(rrb.getWebSocketGroup(rrb.getWebSocketBean()),"返回数据:"+s);
	}
	
	@MethodMapping(path="/imShow",interfacetype="h")
	public String imShow(ReqResBean rrb,SessionLine sessionLine){
		return "/SystemShow/ImShows";
	}
	
	@MethodMapping(path="/say",interfacetype="w")
	public void say(ReqResBean rrb,SessionLine sessionLine){
		String p = webSockets.get(rrb.getWebSocketBean());
		WebSocketGroup wg = webSocketGroups.get(p);
		rrb.sendWebSocket(wg,rrb.getWebSocketBean().getInMessage());
	}
	
	@MethodMapping(path="/linein",interfacetype="w")
	public void linein(ReqResBean rrb,SessionLine sessionLine){
		final WebSocketBean wsb = rrb.getWebSocketBean();
		final String message = wsb.getInMessage();//第一次请求的message作为对话框的选择
		final WebSocketGroup wsGroup = rrb.getWebSocketGroup(rrb.getWebSocketBean());
		wsGroup.remove(wsb);
		if(webSocketGroups.containsKey(message)){
			WebSocketGroup cwsGroup = webSocketGroups.get(message);
			cwsGroup.addGroup(wsb);
		}else{
			WebSocketGroup cwsGroup = new WebSocketGroup();
			webSocketGroups.put(message, cwsGroup);
			cwsGroup.addGroup(wsb);
		}
		webSockets.put(wsb, message);
		wsb.setWebSocketBeanFilterI(new WebSocketBeanFilterI() {
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				WebSocketGroup cwsGroup = webSocketGroups.get(message);
				System.out.println(wsb.getId());
				cwsGroup.remove(wsb);
				if(cwsGroup.size() == 0){
					webSocketGroups.remove(message);
				}
				webSockets.remove(wsb);
			}
		});
		rrb.sendWebSocket(wsb, "lineinsuccess");//这个和openSuccess信息区别，这个主要作为连接到当前方法之后返回的
	}
	
}
