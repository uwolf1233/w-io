package com.wolf.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.wolf.controller.Server1Controller;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.SystemNet.Cpaths;
import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.Files;
import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.HttpServletResponse;
import com.wolf.javabean.SystemNet.HttpSession;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.RegBean;
import com.wolf.javabean.SystemNet.ServerRequest;
import com.wolf.javabean.SystemNet.ServerResponse;
import com.wolf.javabean.WebSocketBean;
import com.wolf.javabean.WebSocketGroup;

public class RefController {

	public static RefController INSTANCE = new RefController();
	
	private RefController(){}
	
	//控制器获取方法数据
	public void regControllers(List<Object> oList,RegBean.Builder regBean){
		Map<String, Cpaths> pathsBuilder = new HashMap<String,Cpaths>();
		
		for(Object o : oList){
			Class clazz = o.getClass();
			Method[] methods = clazz.getDeclaredMethods();
			
			for(Method method : methods){
				if(method.isAnnotationPresent(MethodMapping.class)){
					String path = method.getAnnotation(MethodMapping.class).path();
					String lockNames = method.getAnnotation(MethodMapping.class).lockNames();
					int secondnum = method.getAnnotation(MethodMapping.class).secondnum();
					long geWait = method.getAnnotation(MethodMapping.class).geWait();
					int threadPoolNumMax = method.getAnnotation(MethodMapping.class).threadPoolNumMax();
					boolean hasFile = method.getAnnotation(MethodMapping.class).hasFile();
					boolean fileIn = method.getAnnotation(MethodMapping.class).fileIn();
					String interfaceType = method.getAnnotation(MethodMapping.class).interfacetype();
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("object", o);
					map.put("method", method);
					OtherServer.controllerClass.put(path, map);
					Cpaths.Builder cpath = Cpaths.newBuilder();
					cpath.addAllLocknames(Arrays.asList(lockNames.split(",")));
					cpath.setSecondnum(secondnum);
					cpath.setGeWait(geWait);
					cpath.setThreadPoolNumMax(threadPoolNumMax);
					cpath.setHasFile(hasFile);
					cpath.setFileIn(fileIn);
					cpath.setInterfacetype(interfaceType);
					cpath.setLocknames(0, method.getAnnotation(MethodMapping.class).lockNames().split(",")[0]);
					cpath.setSecondnum(method.getAnnotation(MethodMapping.class).secondnum());
					cpath.setGeWait(method.getAnnotation(MethodMapping.class).geWait());
					cpath.setTrans(method.getAnnotation(MethodMapping.class).trans());
					cpath.setTranCommitMinute(method.getAnnotation(MethodMapping.class).tranCommitMinute());
					cpath.setTranRollbackMinute(method.getAnnotation(MethodMapping.class).tranRollbackMinute());
					cpath.setThreadPoolNumMax(method.getAnnotation(MethodMapping.class).threadPoolNumMax());
					
					pathsBuilder.put(path, cpath.build());
					//regBean.addPaths(path);
				}else{
					continue;
				}
			}
		}
		regBean.putAllPaths(pathsBuilder);
	}
	
	//控制器注册
	private List<Object> oList = new ArrayList<Object>();
	
	public void putoList(Object o){
		oList.add(o);
	}
	
	public void controllerSetting(RegBean.Builder regBean){
		regControllers(oList,regBean);
	}
	
	private DBCon dbcon = null;
	
	public void setDbcon(DBCon dbcon){
		this.dbcon = dbcon;
	}
	
	/**
	 * 由http调用
	 * @param httpBean
	 * @param builder
	 * @param type
	 * @throws Exception
	 */
	public ReqResBean invokeMethod(HttpBean httpBean,HttpBean.Builder builder,LineLog lineLog) throws Exception{//,List<Files> files
		String url = httpBean.getUrl();
		Connection con = null;//这里写获取jdbc连接的方式(为了得到更高性能，后续将一直采用原生jdbc)
		try{
			//反射相关知识，根据url和注解配置的路径
			if(OtherServer.controllerClass.containsKey(url)){
				Map<String,Object> map = OtherServer.controllerClass.get(url);
				Object o = map.get("object");
				Method method = (Method)map.get("method");
				method.setAccessible(true);
				String returnTypeName = method.getReturnType().getName();
				HttpServletResponse.Builder response = builder.getResponseBuilder();
				
				SessionLine sessionLine = new SessionLine();
				sessionHandle(httpBean, builder,sessionLine);
				ReqResBean rrb = new ReqResBean("http",response,httpBean.getRequest(),httpBean.getRequest().getSession().getSessionId());//由ReqResBean统一封装request和response
				//http调用的一定是mainId，内部不再考虑用http接口调用
				String id = UUID.randomUUID().toString().replace("-", "");
				//由当前方法组装上一个方法的parentId
				LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId()+"-"+lineLog.getId(), id, url, OtherServer.serverName, OtherServer.ip);
				rrb.setLogsBean(logsBean);
				rrb.setLogChannel(LogServer.logChannel);
				
				boolean trans = method.getAnnotation(MethodMapping.class).trans();//是否开启事务
		
				if(trans){//http直接请求进来的，默认为事务开始
					con = dbcon != null ? dbcon.getCon() : null;
					if(con!=null){
						con.setAutoCommit(false);
					}
					boolean isMain = false;
					String serverId = UUID.randomUUID().toString().replace("-", "");
					isMain = true;
					//String mainId = UUID.randomUUID().toString().replace("-", "");
					String mainId = serverId;
					String curid = UUID.randomUUID().toString().replace("-", "");
					logsBean.setParentId(logsBean.getParentId()+"-"+logsBean.getId());
					logsBean.setId(curid);
					rrb.log("事务预提交，主控ID:"+mainId, "1");
					rrb.trans(con, LineTransServer.transChannel, mainId, serverId, isMain);
					boolean b = rrb.thransThreadInit();
					if(!b){//事务进不了调度器则其他全部回滚
						System.out.println("事务预提交失败");
						rrb.log("事务预提交失败，主控ID:"+mainId, "0");
						return null;
					}else{
						rrb.log("事务预提交成功，主控ID:"+mainId, "1");
					}
				}
				
				if(returnTypeName.equals("void")){//判断返回的类型
					method.invoke(o, rrb,sessionLine);
					response.setType("String");//返回给前端的类型
					response.setResponseId(httpBean.getResponse().getResponseId());//把发送的responseId返回，方便前端服务器辨别response
					rrb.log("数据返回", "1");
				}else{
					Object reto = method.invoke(o, rrb,sessionLine);
					if(reto instanceof String){
						response.setResponseId(httpBean.getResponse().getResponseId());
						response.setResponseData(reto+"");
						response.setType("file");
						rrb.log("返回html文件", "1");
					}//其他返回类型可以自己做
				}
				response.setLocks(httpBean.getRequest().getLocks());
				//builder.getRequestBuilder().getSessionBuilder().putAllSession(sessionMap);
				return rrb;
			}else{
				throw new RuntimeException("执行失败");
			}
		}catch(Throwable e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
//		finally{
//			if(con != null && !con.isClosed()){
//				con.close();
//				con = null;
//			}
//		}
	}
	
	/**
	 * 由websocket调用，open和close状态不进入，只有message状态进入
	 * @param httpBean
	 * @param builder
	 * @param type
	 * @throws Exception
	 */
	public ReqResBean webSocketInvokeMethod(HttpBean httpBean,HttpBean.Builder builder,
			WebSocketBean webSocketBean) throws Exception{//,List<Files> files
		String url = webSocketBean.getPath() == null || webSocketBean.getPath().trim().equals("") 
				? httpBean.getWebsocketms().getPath() : webSocketBean.getPath();
		webSocketBean.setId(httpBean.getWebsocketms().getId());
		webSocketBean.setInMessage(httpBean.getWebsocketms().getSendMessage());//其他类型后期再加
		//反射相关知识，根据url和注解配置的路径
		if(OtherServer.controllerClass.containsKey(url)){
			Map<String,Object> map = OtherServer.controllerClass.get(url);
			Object o = map.get("object");
			Method method = (Method)map.get("method");
			method.setAccessible(true);
			String returnTypeName = method.getReturnType().getName();
			HttpServletResponse.Builder response = builder.getResponseBuilder();
			
			SessionLine sessionLine = new SessionLine();
			sessionHandle(httpBean, builder,sessionLine);
			//ReqResBean rrb = new ReqResBean("http",response,httpBean.getRequest(),httpBean.getRequest().getSession().getSessionId());//由ReqResBean统一封装request和response
			ReqResBean rrb = new ReqResBean(webSocketBean, sessionLine.getSessionId());
			String mainId = UUID.randomUUID().toString().replace("-", "");//websocket接口调用的一定是mainid
			LogsBean logsBean = new LogsBean(mainId, "", "", url, OtherServer.serverName, OtherServer.ip);
			rrb.setLogsBean(logsBean);
			
			boolean trans = method.getAnnotation(MethodMapping.class).trans();//是否开启事务
			Connection con = null;//这里写获取jdbc连接的方式(为了得到更高性能，后续将一直采用原生jdbc)
			if(trans){//http直接请求进来的，默认为事务开始
				con = dbcon != null ? dbcon.getCon() : null;
				if(con!=null){
					con.setAutoCommit(false);
				}
				boolean isMain = false;
				//String serverId = UUID.randomUUID().toString().replace("-", "");
				String serverId = mainId;
				isMain = true;
				rrb.trans(con, LineTransServer.transChannel, mainId, serverId, isMain);
				boolean b = rrb.thransThreadInit();
				if(!b){//事务进不了调度器则其他全部回滚
					System.out.println("事务预提交失败");
					return null;
				}
			}
			
			if(returnTypeName.equals("void")){//判断返回的类型
				method.invoke(o, rrb,sessionLine);
				response.setType("String");//返回给前端的类型
				response.setResponseId(httpBean.getResponse().getResponseId());//把发送的responseId返回，方便前端服务器辨别response
			}else{
				Object reto = method.invoke(o, rrb,sessionLine);
				if(reto instanceof String){
					response.setResponseId(httpBean.getResponse().getResponseId());
					response.setResponseData(reto+"");
					response.setType("file");
				}//其他返回类型可以自己做
			}
			response.setLocks(httpBean.getRequest().getLocks());
			//builder.getRequestBuilder().getSessionBuilder().putAllSession(sessionMap);
			return rrb;
		}
		return null;
	}
	
	/**
	 * 由服务器之间调用
	 * @param httpBean
	 * @param builder
	 * @param type
	 * @throws Exception
	 */
	public ReqResBean invokeMethod(Datas datas,Datas.Builder builder) throws Exception{
		ServerRequest request = datas.getServerRequest();
		String url = request.getPath();
		//反射相关知识，根据url和注解配置的路径
		if(OtherServer.controllerClass.containsKey(url)){
			Map<String,Object> map = OtherServer.controllerClass.get(url);
			Object o = map.get("object");
			Method method = (Method)map.get("method");
			method.setAccessible(true);
			String returnTypeName = method.getReturnType().getName();
			ServerResponse.Builder response = builder.getServerResponseBuilder();
			
			SessionLine sessionLine = new SessionLine();
			sessionHandle(datas.getHttpBean(), builder.getHttpBeanBuilder(),sessionLine);
			ReqResBean rrb = new ReqResBean("server",response,request,sessionLine.getSessionId());//由ReqResBean统一封装request和response
			
			LineLog lineLog = datas.getLineLog();
			LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(),
					url, OtherServer.serverName, OtherServer.ip);
			rrb.setLogsBean(logsBean);
			
			boolean trans = method.getAnnotation(MethodMapping.class).trans();//是否开启事务
			Connection con = null;//这里写获取jdbc连接的方式(为了得到更高性能，后续将一直采用原生jdbc)
			String mainId = datas.getTrans().getMainId();
			if(trans){
				con = dbcon != null ? dbcon.getCon() : null;
				if(con!=null){
					con.setAutoCommit(false);
				}
				boolean isMain = false;
				String serverId = UUID.randomUUID().toString().replace("-", "");
				if(mainId == null || mainId.trim().equals("")){
					mainId = serverId;
					isMain = true;
				}
				rrb.trans(con, LineTransServer.transChannel, mainId, serverId, isMain);//方法内不允许设置con，但可以获取
				boolean b = rrb.thransThreadInit();
				if(!b){//事务进不了调度器则其他全部回滚
					rrb.thransThread(0);
					System.out.println("事务预提交失败，同一个mainId的事务全部回滚");
					return null;
				}
			}else if(mainId!=null && !mainId.trim().equals("")){
				//rrb.trans(con, LineTransServer.transChannel, mainId, serverId, false);//方法内不允许设置con，但可以获取
				rrb.setMainId(mainId);
			}
			
			if(returnTypeName.equals("void")){//判断返回的类型
				method.invoke(o, rrb,sessionLine);
				response.setType("ServerResponse");//由注册中心处理
				response.setResponseId(datas.getServerResponse().getResponseId());//responseId是由另一端定义的业务ID
				response.setChannelId(datas.getServerResponse().getChannelId());//channelId是另一端对应的通道ID
			}
			response.setLocks(datas.getServerResponse().getLocks());
			//builder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().putAllSession(sessionMap);
			return rrb;
		}
		return null;
	}
	
	/**
	 * 将session数据加入(后期将改为根据业务服务器传session数据)
	 * @param httpBean
	 * @param builder
	 * @param map
	 */
//	private void sessionHandle(HttpBean httpBean,HttpBean.Builder builder,Map<String,String> map){
//		HttpSession session = httpBean.getRequest().getSession();
//		Map<String,String> sessionMap = session.getSessionMap();
//		map.putAll(sessionMap);
//		map.put("sessionId", session.getSessionId());
//		HttpSession.Builder sessionBuilder = builder.getRequestBuilder().getSessionBuilder();
//		sessionBuilder.setServerName(OtherServer.serverName);
//		sessionBuilder.setSessionId(session.getSessionId());
//	}
	
	/**
	 * 将session数据加入(后期将改为根据业务服务器传session数据)
	 * @param httpBean
	 * @param builder
	 * @param map
	 */
	private void sessionHandle(HttpBean httpBean,HttpBean.Builder builder,SessionLine sessionLine){
		HttpSession session = httpBean.getRequest().getSession();
		HttpSession.Builder sessionBuilder = builder.getRequestBuilder().getSessionBuilder();
		sessionBuilder.setSessionId(session.getSessionId());
		sessionLine.setSessionId(session.getSessionId());
	}
	
}
