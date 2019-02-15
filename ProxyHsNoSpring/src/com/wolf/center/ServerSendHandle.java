package com.wolf.center;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.javabean.LogsBean;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.HttpSession;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.ServerRequest;
import com.wolf.javabean.SystemNet.ServerResponse;
import com.wolf.locks.DistributedLock;
import com.wolf.server.ControllerProxy;
import com.wolf.server.HttpServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

//服务之间相互调用
public class ServerSendHandle extends ChannelInboundHandlerAdapter{
	
	//用于负载还原，http由http的handle持有，因为一个http请求对应的是一个连接，而服务器端互相调用采用的是多路复用，只能由id辨别
	public static ConcurrentMap<String,CenterHandle> centerHandle = new ConcurrentHashMap<String,CenterHandle>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;//接收到的proto
		LineLog lineLog = datas.getLineLog();
		if(datas.getHandletype().equals("ServerRequest")){//背井离乡去到远方，留下一个channelId辨别回家的路
			Channel channel = ctx.channel();
			AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
			String channelId = channel.attr(channelIdAttr).get();
			SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();//先实例化proto
			
			datasBuilder.setHandletype("ServerRequest");//下面的代码都是请求的数据转发
			datasBuilder.setServerResponse(datas.getServerResponse());
			datasBuilder.setServerRequest(datas.getServerRequest());
			
			datasBuilder.setTrans(datas.getTrans());//事务传递
			
			datasBuilder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(
			datas.getHttpBean().getRequest().getSession().getSessionId());
			ServerResponse.Builder responseBuilder = datasBuilder.getServerResponseBuilder();
			responseBuilder.setChannelId(channelId);
			//------日志------
			LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(), 
					datas.getServerRequest().getPath(), "ProxyHs", HttpServer.ip);
			logsBean.getLogDatasBuilder("服务调用:路径"+datas.getServerRequest().getPath(), "1", false);
			//------日志------
			LineLog.Builder lineLogBuilder = datasBuilder.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			ControllerProxy.INSTANCE().sendConMethod(datasBuilder, datas.getServerRequest().getPath(), 
					datas.getHttpBean().getRequest().getSession().getSessionId(), datas.getLineLog(),channel);
			
		}else if(datas.getHandletype().equals("ServerResponse")){
			
			SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();//先实例化proto
			datasBuilder.setHandletype("ServerResponse");
			datasBuilder.setServerResponse(datas.getServerResponse());
			datasBuilder.getHttpBeanBuilder().getRequestBuilder().getSessionBuilder().setSessionId(
					datas.getHttpBean().getRequest().getSession().getSessionId());
			String lockId = datas.getServerResponse().getLocks().getId();
			AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
			String channelId = ctx.channel().attr(channelIdAttr).get();
			reset(lockId, channelId,datas.getServerRequest().getPath());//这个是还原负载的channel，不要和下面的搞混了
			//------日志------
			LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(), 
					datas.getServerRequest().getPath(), "ProxyHs", HttpServer.ip);
			logsBean.getLogDatasBuilder("服务调用返回,有返回值", "1", false);
			//------日志------
			
			LineLog.Builder lineLogBuilder = datasBuilder.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			ControllerProxy.INSTANCE().returnToServer(datas.getServerResponse().getChannelId(), datasBuilder);//根据channelId找到回家的路
		}else if(datas.getHandletype().equals("ServerResponseNoReturn")){
			String lockId = datas.getServerResponse().getLocks().getId();
			AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
			String channelId = ctx.channel().attr(channelIdAttr).get();
			reset(lockId, channelId,datas.getServerRequest().getPath());
			//------日志------
			LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(), 
					datas.getServerRequest().getPath(), "ProxyHs", HttpServer.ip);
			logsBean.getLogDatasBuilder("服务调用返回,没返回值", "1", false);
			//------日志------
		}
		else{
			ctx.fireChannelRead(datas);
		}
	}

	/**
	 * 重置一些东西,解锁，负载还原
	 */
	private void reset(String lockid,String channelId,String path){
		if(!lockid.trim().equals("")){
			System.out.println("server之间传输即将解锁id---"+lockid);
			DistributedLock dl = DistributedLock.distributedLockMap.get(lockid);
			if(dl == null){
				System.out.println("锁已经不在，应该是被锁扫描解锁");
				return;
			}
			dl.setReleaseQueue();//解锁
			DistributedLock.distributedLockMap.remove(lockid);
		}
		CenterHandle ch = null;
		if((ch = centerHandle.get(channelId)) != null){
			if(path != null && !path.trim().equals("") && ch.pathThreadPoolNumMax.containsKey(path)){
				int cur = ch.pathThreadPoolNumMax.get(path).incrementAndGet();//路径负载还原
				System.out.println("server之间传输的路径负载还原:"+cur);
			}else{
				int cur = ch.serverRequestNum.incrementAndGet();
				System.out.println("server之间传输的全局负载还原:"+cur);
			}
		}
	}
	
}
