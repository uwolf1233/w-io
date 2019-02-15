package com.wolf.serverLine;

import io.netty.channel.Channel;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.javabean.FileBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.Files;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.ServerRequest;
import com.wolf.javabean.SystemNet.ServerResponse;
import com.wolf.javabean.SystemNet.ServerlineData;
import com.wolf.server.OtherServer;

public class ServerRequestBean {

	public static ConcurrentMap<String, ServerRequestBean> serRMap = new ConcurrentHashMap<String, ServerRequestBean>();
	private Date startTime;
	private Date endTime;
	private int second;
	private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(2);
	
	/**
	 * 数据发送
	 * @param path 另一端服务器的path
	 * @param sendData 发送的数据(暂时为String)
	 * @param isReturn 是否返回
	 * @return 返回Object
	 * @throws Exception
	 */
	public Object send(String path,Map<String,String> sendDataMap,boolean isReturn,ReqResBean rrb) throws Exception{
		String sessionId = rrb.getSessionId();
		SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();//先实例化proto
		datasBuilder.setHandletype("ServerRequest");//定义发送的类型，由注册中心判断
		datasBuilder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(sessionId);
		datasBuilder.getServerRequestBuilder().setPath(path);
		ServerResponse.Builder response = datasBuilder.getServerResponseBuilder();
		response.setIsReturn(isReturn);
		
		String message = "";
		String uuid = UUID.randomUUID().toString().replace("-", "");
		if(isReturn){//如果有返回值
			serRMap.put(uuid, this);
			response.setResponseId(uuid);
			message = "服务调用，即将请求的路径"+path+",有返回值";
			if(rrb.isTran()){//说明已经开启了事务
				datasBuilder.getTransBuilder().setMainId(rrb.getMainId());
				message+=",有事务";
			}
		}else{
			message = "服务调用，即将请求的路径"+path+",无返回值";
		}
		
		rrb.log(message, "1");
		LineLog.Builder lineLog = datasBuilder.getLineLogBuilder();//此处
		lineLog.setMainId(rrb.getLogsBean().getMainId());
		lineLog.setParentId(rrb.getLogsBean().getParentId());
		lineLog.setId(rrb.getLogsBean().getId());
		ServerRequest.Builder request = datasBuilder.getServerRequestBuilder();
		
		request.getServerlineDataBuilder().putAllAttr(sendDataMap);
		//判断是否有文件传输，如果有，则将文件放入协议中
		if(rrb.getServerOutFileBean() != null){
			List<Files.Builder> fileBeans = rrb.getServerOutFileBean();
			for(Files.Builder f : fileBeans){
				request.addFiles(f);
			}
		}
		Channel channel = getChannel();
		channel.writeAndFlush(datasBuilder);//获取通道并发送数据
		if(isReturn){//如果有返回值
			Object o = queue.take();
			serRMap.remove(uuid);
			return o;
		}else{
			return null;
		}
	}
	
	/**
	 * 设置返回值
	 * @param o
	 */
	public void setResponse(Object o) throws Exception{
		queue.put(o);
	}
	
	private Channel getChannel(){
		return OtherServer.regChannel;
	}
	
}
