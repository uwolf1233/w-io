package com.wolf.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.center.CenterServer;
import com.wolf.javabean.GsonBean;
import com.wolf.javabean.ServerBean;
import com.wolf.websocket.WebSocketHandle;
import com.wolf.websocket.WebSocketServer;

public class GetAndSetConfig {

	private GetAndSetConfig(){};
	
	public static GetAndSetConfig INSTANCE = new GetAndSetConfig();
	
	public static Object gets(String cinterfacetype){//获取配置数据
		Map<String, Object> dataMap = new HashMap<String,Object>();
		dataMap.put("msg", "getConfigs");
		dataMap.put("ip", HttpServer.getIp());
		dataMap.put("cpath", HttpServer.getCpath());
		if(cinterfacetype.equals("h")){
			dataMap.put("httpPort", HttpServer.getPort());
			dataMap.put("httpBossNum", HttpServer.getHttpBossNum());
			dataMap.put("httpWorkerNum", HttpServer.getHttpWorkerNum());
			dataMap.put("httpMaxReadCont", HttpServerHandler.getMaxReadCont());
			dataMap.put("httpreqnum", HttpServerHandler.getReqnum().get());
		}
		dataMap.put("clientPort", CenterServer.getClientPort());
		dataMap.put("clientBossNum", CenterServer.getClientBossNum());
		dataMap.put("clientWorkNum", CenterServer.getClientWorkNum());
		if(cinterfacetype.equals("w")){
			dataMap.put("websocketPort", WebSocketServer.port);
			dataMap.put("websocketBossNum", WebSocketServer.bossGroupNum);
			dataMap.put("websocketWorkerNum", WebSocketServer.workerGroupNum);
			dataMap.put("websocketClientNum", WebSocketHandle.channelMap.size());//websocket当前连接数
		}
		//dataMap.put("serverNum", value)
		ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
		int serverNum = 0;
		Map<String,Integer> serverNumMap = new HashMap<String,Integer>();
		//List<ServerBean> serverBeanList = new ArrayList<ServerBean>();
		List<Map<String,Object>> serverBeanList = new ArrayList<Map<String,Object>>();
		for(String key : serverMap.keySet()){
			ServerBean cserverBean = serverMap.get(key);
			Map<String,Object> map = cserverBean.getMap(cinterfacetype);
			if(map==null){
				continue;
			}
			serverNumMap.put(key, cserverBean.getChList().size());//获取路径集群数
			if(serverBeanList.contains(cserverBean)){
				continue;
			}else{
				serverBeanList.add(map);
				serverNum++;
			}
		}
		dataMap.put("serverPathServerNum", serverNumMap);//路径集群数
		dataMap.put("serverNum", serverNum);//分布式服务数，不计算集群
		dataMap.put("serverBeanList", serverBeanList);
		return dataMap;
	}
	
}











