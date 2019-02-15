package com.wolf.server;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.wolf.ChannelWriter.LogWriter;
import com.wolf.cache.PathDataCache;
import com.wolf.cache.StaticFileCache;
import com.wolf.cache.StaticFileCacheLinux;
import com.wolf.center.CenterHandle;
import com.wolf.center.CenterServer;
import com.wolf.channel.LoadStrategyImpl;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.ServerBean;
import com.wolf.javabean.Session.SessionData;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBeans;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.SystemNet.Cookies;
import com.wolf.javabean.SystemNet.Files;
import com.wolf.javabean.SystemNet.HttpServletResponse;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.Locks;
import com.wolf.locks.DistributedLock;
import com.wolf.locks.DistributedLockMg;
import com.wolf.log.LogServer;
import com.wolf.security.SecurityClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

//将请求转发到业务服务器的控制器
//当前版本不再在入口控制session
public class ControllerProxy {

	//INSTANCE;
	private static ControllerProxy cp = new ControllerProxy();
	private ControllerProxy(){}
	public static ControllerProxy INSTANCE(){
		return cp;
	}
	
	public static int requestTimeOut = 5;//超时四秒
	public static String viewPath = "";
	public static String[] viewFileType;
	//key:responseId,value:HttpServerHandler
	public ConcurrentMap<String, HttpServerHandler> httpHandleMap = new ConcurrentHashMap<String, HttpServerHandler>();

	//请求转发
	public void sendConMethod(final SystemNet.Datas.Builder datas,String path,String sessionId,HttpServerHandler httpHandle){
		LogsBean logsBean = httpHandle.logsBean;
		ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
		if(!serverMap.containsKey(path)){
			Channel httpChannel = httpHandle.getCtxs().channel();
			sendData(null, "{'msg':'path not found'}", httpChannel,null);
			//---------日志---------------
			LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("path not found", "0",false);
			LogWriter.logWriter.write(logDatasBuilder);
			//---------日志---------------
			datas.clear();
			return;
		}
		ServerBean serverBean = serverMap.get(path);
		if(!sendType("h", path,serverBean) && !sendType("sh", path,serverBean)){
			sendData(null, "{'msg':'路径不是http类型不允许请求'}", httpHandle.getCtxs().channel(),null);
			//---------日志---------------
			final LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("路径不是http类型不允许请求", "0",false);
			LogWriter.logWriter.write(logDatasBuilder);
			//---------日志---------------
			datas.clear();
			return;//http请求的路径如果不是http请求类型，则不允许进入
		}
		if(sessionId == null || sessionId.trim().equals("") || sessionId.equals("null")){
			SessionLine sessionLine = new SessionLine();
			sessionLine.create(null);
			sessionId = sessionLine.getSessionId();
		}
		if(sessionId != null && !sessionId.equals("null") && !sessionId.trim().equals("")){//sessionId不能为空
			ByteBuf bytebuf = PathDataCache.getBytebufCache(path);
			if(bytebuf!=null){
				sendData(httpHandle.getCtxs().channel(), bytebuf);
				datas.clear();
				return;//如果有缓存，则不再访问业务端
			}
			boolean hasLock = false;//是否有锁
			DistributedLock dl = null;//锁对象
			String lockid = "";//锁Id
			if(serverBean.hasLock()){//判断路径是否包含锁
				dl = DistributedLockMg.INSTANCE.hasAndGet(path);
				if(dl == null){
					//没获得锁不允许进入方法，此处可加入其他处理逻辑;
					sendData(null, "get lock error", httpHandle.getCtxs().channel(),null);
					//---------日志---------------
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("get lock error", "0",false);
					LogWriter.logWriter.write(logDatasBuilder);
					//---------日志---------------
					datas.clear();
					return;
				}
				hasLock = true;
				lockid = UUID.randomUUID().toString().replace("-", "");
			}
			//将各种数据写入协议，发送到各个服务器
			datas.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(sessionId);
			Channel channel = LoadStrategyImpl.INSTANCE.getServerChannel(serverBean.getChList(),httpHandle,path,0);
			//System.out.println("15-------------------"+aa.addAndGet(1));
			if(channel == null){
				sendData(null, "503 服务过载", httpHandle.getCtxs().channel(),null);
				//---------日志---------------
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("503 服务过载", "0",false);
				LogWriter.logWriter.write(logDatasBuilder);
				//---------日志---------------
				datas.clear();
				return;
			}
			String responseId = UUID.randomUUID().toString().replace("-", "");
			httpHandleMap.put(responseId, httpHandle);
			datas.setHandletype("httpSend");
			datas.getHttpBeanBuilder().getResponseBuilder().setResponseId(responseId);
			datas.getHttpBeanBuilder().setUrl(path);
			Locks.Builder locksBuilder = Locks.newBuilder();
			locksBuilder.setId(lockid);
			datas.getHttpBeanBuilder().getRequestBuilder().setLocks(locksBuilder);
			System.out.println("发送锁id---"+lockid);
			//---------日志---------------
			LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("即将发送到业务服务器", "1",false);
			LogWriter.logWriter.write(logDatasBuilder);
			//---------日志---------------
			httpHandle.getMap().remove("sessionId");
			datas.getHttpBeanBuilder().getRequestBuilder().putAllParameter(httpHandle.getMap());
			if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
				channel.writeAndFlush(datas);
			}else{
				try {
					int i = 0;
					while(!channel.isWritable()){
						Thread.sleep(4);
						if(i == requestTimeOut*1000){
							break;
						}
						i++;
					}
					if(channel.isWritable()){
						channel.writeAndFlush(datas);
					}else{
						sendData(null, "504请求超时", httpHandle.getCtxs().channel(),null);
						return;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sendData(null, "504请求超时", httpHandle.getCtxs().channel(),null);
					return;
				}
				
			}
			//session.setIsusing(false);
			System.out.println("是否有锁-------------------"+hasLock);
			if(hasLock){//如果已经获取了锁，则进行解锁监听
				DistributedLock.distributedLockMap.put(lockid, dl);
				dl.releaseLock();//等待解锁
			}
		}else{
			System.out.println("session is null");
			sendData(null, "session is null",httpHandle.getCtxs().channel(),null);
			return;
		}
	}
	
	private static Gson gson = new Gson();
	//private static Gson gsondh = new GsonBuilder().disableHtmlEscaping().create();
	
	//获取http通道
	private Channel getResponseChannel(String responseId,String path){
		HttpServerHandler hsh = httpHandleMap.get(responseId);
		//负载还原
		CenterHandle ch = hsh.centerHandle;
		if(path != null && !path.trim().equals("") && ch.pathThreadPoolNumMax.containsKey(path)){
			int cur = ch.pathThreadPoolNumMax.get(path).incrementAndGet();//路径负载还原
			System.out.println("路径负载还原,当前值:"+cur);
		}else{
			int cur = hsh.centerHandle.serverRequestNum.incrementAndGet();//全局负载还原
			System.out.println("全局负载还原,当前值:"+cur);
		}
		Channel channel = hsh.getCtxs().channel();
		httpHandleMap.remove(responseId);
		return channel;
	}
	
	private void addCookie(FullHttpResponse response,HttpServletResponse responses){
		if(responses!=null && responses.getCookieList() != null){
			List<Cookies> list = responses.getCookieList();
			if(list!=null && list.size()>0){
				List<DefaultCookie> cookielist = new ArrayList<DefaultCookie>();
				for(Cookies cookies : list){
					DefaultCookie dc = new DefaultCookie(cookies.getKey(), cookies.getValue());
					cookielist.add(dc);
				}
				response.headers().add(HttpHeaderNames.SET_COOKIE, cookielist);
			}
		}
	}
	
	private void addCookie(FullHttpResponse response,Map<String,String> cookieMap){
		if(cookieMap == null){
			return;
		}
		if(cookieMap!=null && cookieMap.size()>0){
			List<DefaultCookie> cookielist = new ArrayList<DefaultCookie>();
			for(String key : cookieMap.keySet()){
				DefaultCookie dc = new DefaultCookie(key, cookieMap.get(key));
				cookielist.add(dc);
			}
			DefaultCookie dc = new DefaultCookie("WsessionId","123123");//临时写死
			cookielist.add(dc);
			response.headers().add(HttpHeaderNames.SET_COOKIE, cookielist);
		}
	}
	
	//发送数据或者文件到前端
	public void sendData(HttpServletResponse responses,String path){
		
		Channel channel = getResponseChannel(responses.getResponseId(),path);
		
	 	Map<String,String> map = responses.getAttrMap();
	 	FullHttpResponse response = null;
	 	if(map.containsKey("jsondata")){
	 		Map<String,Object> retMap = new HashMap<String,Object>();
	 		for(String key : map.keySet()){
	 			if(key.equals("jsondata")){continue;}
	 			retMap.put(key, map.get(key));
	 		}
	 		String value = map.get("jsondata");
	 		value = value.replace("\\", "");
	 		if(value.indexOf("\"") == 0){//可能是前面包含双引号的字符串
	 			value = value.substring(1, value.length()-1);
	 		}
	 		if(value.startsWith("{")){
		 		Map<String,Object> jsonmap = gson.fromJson(value, Map.class);
		 		retMap.put("jsondata", jsonmap);
		 		response = new DefaultFullHttpResponse(
		                HTTP_1_1, OK, Unpooled.wrappedBuffer(gson.toJson(retMap).getBytes(Charset.forName("utf-8"))));
		 		response.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
	 		}else if(value.startsWith("[")){
	 			List<Map<String,Object>> jsonmap = gson.fromJson(value, List.class);
	 			retMap.put("jsondata", jsonmap);
		 		response = new DefaultFullHttpResponse(
		                HTTP_1_1, OK, Unpooled.wrappedBuffer(gson.toJson(retMap).getBytes(Charset.forName("utf-8"))));
		 		response.headers().set(CONTENT_TYPE, "application/json;charset=UTF-8");
	 		}
	 	}else{
	 		String json = StaticBeans.jsonStringHandle(gson.toJson(map));
	 		if(json == null || json.trim().equals("") || json.equals("{}")){
	 			json = responses.getResponseData();
	 			if(json.indexOf("\"") == 0){//可能是前面包含双引号的字符串
	 				json = json.substring(1, json.length()-1);
		 		}
	 			json = json.replace("\\", "");
	 		}
	 		response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, Unpooled.wrappedBuffer(json.getBytes(Charset.forName("utf-8"))));
	 		response.headers().set(CONTENT_TYPE, "text/plain;charset=UTF-8");
	 	}
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set("Access-Control-Allow-Origin","*");
        response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=2,max=10"));
        addCookie(response, responses);
        writerResponse(channel, response);
        //channel.close();//可以选择关闭
    }
	
	public void sendData(Channel channel,ByteBuf bytebuf){
		
    	FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, bytebuf);
        response.headers().set(CONTENT_TYPE, "text/plain;charset=UTF-8");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set("Access-Control-Allow-Origin","*");
        response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=10,max=10"));
        writerResponse(channel, response);
        //ctx.close();//可以选择关闭
    }

	public void sendData(HttpServletResponse responses,final String message,Channel channel,String path){
		
		if(channel == null){
			channel = getResponseChannel(responses.getResponseId(),path);
		}
		
    	FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, Unpooled.wrappedBuffer(message.getBytes(Charset.forName("utf-8"))));
        response.headers().set(CONTENT_TYPE, "text/plain;charset=UTF-8");
        response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
        response.headers().set("Access-Control-Allow-Origin","*");
        response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=10,max=10"));
        addCookie(response, responses);
        writerResponse(channel, response);
        //ctx.close();
    }
    
	public void sendHtml(HttpServletResponse responses,String path){
    	try {
    		Channel channel = getResponseChannel(responses.getResponseId(),path);
    		String returnStr = responses.getResponseData();
    		//当前从静态资源缓存中获取，如果要直接从磁盘获取，可以再加个接口
    		ByteBuf bytebuf = null;
    		if(HttpServer.systemtype.equals("linux")){
    			bytebuf = StaticFileCacheLinux.getFile(returnStr);
    		}else{
    			bytebuf = StaticFileCache.getFile(returnStr);
    		}
			FullHttpResponse response = new DefaultFullHttpResponse(
			        HTTP_1_1, OK, bytebuf);
			response.headers().set(CONTENT_TYPE, "text/html");
			response.headers().set(CONTENT_LENGTH,
			        response.content().readableBytes());
			response.headers().set("Access-Control-Allow-Origin","*");
			response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=10,max=10"));
			addCookie(response, responses);
			writerResponse(channel, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
		}
    }
	
	public void sendHtml(String path,Channel channel,Map<String,String> cookieMap){
		ByteBuf bytebuf = null;
		if(HttpServer.systemtype.equals("linux")){
			bytebuf = StaticFileCacheLinux.getFile(path);
		}else{
			bytebuf = StaticFileCache.getFile(path);
		}
		FullHttpResponse response = new DefaultFullHttpResponse(
		        HTTP_1_1, OK, bytebuf);
		response.headers().set(CONTENT_TYPE, "text/html");
		response.headers().set(CONTENT_LENGTH,
		        response.content().readableBytes());
		response.headers().set("Access-Control-Allow-Origin","*");
		response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=10,max=10"));
		addCookie(response, cookieMap);
		writerResponse(channel, response);
	}
	
	public void sendHtmlFile(String htmlFilePath,Channel channel){
    	try {
    		//当前从静态资源缓存中获取，如果要直接从磁盘获取，可以再加个接口
    		ByteBuf bytebuf = StaticFileCache.getFile(htmlFilePath);
    		if(bytebuf == null){
    			sendData(channel, Unpooled.wrappedBuffer((htmlFilePath+" not found").getBytes(Charset.forName("utf-8"))));
    			return;
    		}
			FullHttpResponse response = new DefaultFullHttpResponse(
			        HTTP_1_1, OK, bytebuf);
			if(htmlFilePath.endsWith(".html")){
				response.headers().set(CONTENT_TYPE, "text/html");
			}else if(htmlFilePath.endsWith(".css")){
				response.headers().set(CONTENT_TYPE, "text/css");
			}else if(htmlFilePath.endsWith(".js")){
				response.headers().set(CONTENT_TYPE, "application/x-javascript");
			}else if(htmlFilePath.endsWith(".ico")){
				response.headers().set(CONTENT_TYPE, "application/x-ico");
			}
			response.headers().set(CONTENT_LENGTH,
			        response.content().readableBytes());
			response.headers().set("Access-Control-Allow-Origin","*");
			response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=10,max=10"));
			writerResponse(channel, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			//channel.close();
		}
    }
	
	public void sendFile(Files files,HttpServletResponse responses,String path){
		try {
			Channel channel = getResponseChannel(responses.getResponseId(),path);
			ByteString bs = files.getFiledata();
			byte[] bytes = bs.toByteArray();
			if(bytes.length == 0){
				sendData(responses, "no file", channel, path);
				return;
			}
			String hz = files.getHz();
			String fileName = files.getFileName()+hz;
			FullHttpResponse response = new DefaultFullHttpResponse(
			        HTTP_1_1, OK);
			if(hz.equals(".txt")){
				response.headers().set(CONTENT_TYPE, "application/octet-stream");
			}else if(hz.equals(".xlsx")){
				response.headers().set(CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			}
			response.headers().set(CONTENT_LENGTH,bytes.length);
			//response.headers().set("Content-Disposition","attachment;filename*=UTF-8''"+URLEncoder.encode(fileName, "UTF-8"));
			response.headers().set("Content-Disposition","attachment;filename="+URLEncoder.encode(fileName, "UTF-8"));
			response.headers().set("Access-Control-Allow-Origin","*");
			response.headers().set(CONNECTION, new AsciiString("keep-alive:timeout=10,max=10"));
			response.content().writeBytes(bytes);
			addCookie(response, responses);
			writerResponse(channel, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean writerResponse(Channel channel,FullHttpResponse response){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(response);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == ControllerProxy.requestTimeOut*1000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(response);
					return true;
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	
	
	//服务请求转发
	public void sendConMethod(SystemNet.Datas.Builder datas,String path,String sessionId,LineLog lineLog,Channel responseChannel){
		ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
		String mainId = lineLog.getMainId();
		String parentId = lineLog.getParentId();
		String logId = lineLog.getId();
		LogsBean logsBean = new LogsBean(mainId, parentId, logId, path, "ProxyHs", HttpServer.ip);
		LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("即将调用"+path+"接口", "1", false);
		LogWriter.logWriter.write(logDatasBuilder);
		
		if(serverMap.get(path) == null){
			datas.setHandletype("ServerResponse");
			datas.getServerResponseBuilder().setType("500");
			datas.getServerResponseBuilder().getServerlineDataBuilder().setSendDataStr("path not found");
			returnToServer(datas, responseChannel);
			LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("path not found", "0", false);
			LogWriter.logWriter.write(logDatasBuilder1);
			return;
		}
		ServerBean serverBean = serverMap.get(path);
		if(!sendType("s", path,serverBean) && !sendType("sh", path,serverBean)){
			datas.setHandletype("ServerResponse");
			datas.getServerResponseBuilder().setType("500");
			datas.getServerResponseBuilder().getServerlineDataBuilder().setSendDataStr("{'$code':'0','message':'路径不是server类型不允许请求'}");//最好改为英文标识
			returnToServer(datas, responseChannel);
			LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("路径不是server类型不允许请求", "0", false);
			LogWriter.logWriter.write(logDatasBuilder1);
			return;//server请求的路径如果不是server请求类型，则不允许进入
		}
		ByteBuf bytebuf = PathDataCache.getBytebufCache(path);
		if(bytebuf!=null){
			datas.setHandletype("ServerResponse");
			datas.getServerResponseBuilder().setType("200");
			byte[] bytes = new byte[bytebuf.readableBytes()];
			bytebuf.readBytes(bytes);
			datas.getServerResponseBuilder().getServerlineDataBuilder()
				.setSendDataStrBytes(ByteString.copyFrom(bytes));//内部接口缓存数据传输由byte数组进行
			
			LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("从缓存中获取数据", "1", false);
			LogWriter.logWriter.write(logDatasBuilder1);
			
			LineLog.Builder lineLogBuilder = datas.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			if(!writeServer(responseChannel, datas)){
				datas.setHandletype("ServerResponse");
				datas.getServerResponseBuilder().setType("500");
				datas.getServerResponseBuilder().getServerlineDataBuilder().setSendDataStr("{'$code':'0','message':'请求超时'}");//最好改为英文标识
				returnToServer(datas, responseChannel);
				return;//server请求的路径如果不是server请求类型，则不允许进入
			}
			
			return;//如果有缓存，则不再访问业务端
		}
		if(sessionId != null && !sessionId.equals("null") && !sessionId.trim().equals("")){//sessionId不能为空
			boolean hasLock = false;//是否有锁
			DistributedLock dl = null;//锁对象
			String lockid = "";//锁Id
			//if(CenterServer.serverLockMap.containsKey(path)){//判断路径是否包含锁
			if(serverBean.hasLock()){//判断路径是否包含锁
				dl = DistributedLockMg.INSTANCE.hasAndGet(path);
				if(dl == null){
					//没获得锁不允许进入方法，此处可加入其他处理逻辑;
					datas.setHandletype("ServerResponse");
					datas.getServerResponseBuilder().setType("500");
					datas.getServerResponseBuilder().getServerlineDataBuilder().setSendDataStr("{'$code':'0','message':'get lock error'}");
					returnToServer(datas, responseChannel);
					LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("get lock error", "0", false);
					LogWriter.logWriter.write(logDatasBuilder1);
					return;
				}
				hasLock = true;
				lockid = UUID.randomUUID().toString().replace("-", "");
			}
			//将各种数据写入协议，发送到各个服务器
			datas.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(sessionId);
			Channel channel = LoadStrategyImpl.INSTANCE.getServerChannel(serverMap.get(path).getChList(),path,datas.getLineLog(),0);
			if(channel == null){
				datas.getServerResponseBuilder().setType("500");
				datas.getServerResponseBuilder().getServerlineDataBuilder().setSendDataStr("{'$code':'0','message':'服务过载'}");//最好改为英文标识
				returnToServer(datas, responseChannel);
				LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("服务过载", "0", false);
				LogWriter.logWriter.write(logDatasBuilder1);
				return;
			}
			
			datas.getServerResponseBuilder().getLocksBuilder().setId(lockid);
			System.out.println("发送锁id---"+lockid);
			
			LineLog.Builder lineLogBuilder = datas.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			if(!writeServer(channel, datas)){
				datas.setHandletype("ServerResponse");
				datas.getServerResponseBuilder().setType("500");
				datas.getServerResponseBuilder().getServerlineDataBuilder().setSendDataStr("{'$code':'0','message':'请求超时'}");//最好改为英文标识
				returnToServer(datas, responseChannel);
				return;//server请求的路径如果不是server请求类型，则不允许进入
			}else{
				LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("接口已调用", "1", false);
				LogWriter.logWriter.write(logDatasBuilder1);
			}
			//session.setIsusing(false);
			if(hasLock){//如果已经获取了锁，则进行解锁监听
				DistributedLock.distributedLockMap.put(lockid, dl);
				dl.releaseLock();//等待解锁
			}
		}
	}
	
	private boolean writeServer(Channel channel,SystemNet.Datas.Builder datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == requestTimeOut*1000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(datas);
					return true;
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	
	private void returnToServer(SystemNet.Datas.Builder datasBuilder,Channel channel){
		//channel.writeAndFlush(datasBuilder);
		writeServer(channel, datasBuilder);
	}
	
	public void returnToServer(String responseChannelId,SystemNet.Datas.Builder datasBuilder){
		if(!CenterHandle.channelMap.containsKey(responseChannelId)){
			//如果找不到回家的路,则迷路
		}
		Channel channel = CenterHandle.channelMap.get(responseChannelId);//找到回家的路
		writeServer(channel, datasBuilder);
	}
	
	//判断路径是否支持某种请求，s为内部接口，h为外部接口,w为websocket接口，sh为内外接口，作为接口分离，默认为内部
	public static boolean sendType(String type,String path,ServerBean serverBean){
		if(type == null || type.trim().equals("")){
			System.out.println("没有类型不允许请求");
			return false;
		}
		if(serverBean.getInterfacetype().equals(type)){
			return true;
		}else{
			return false;
		}
	}
	
}
