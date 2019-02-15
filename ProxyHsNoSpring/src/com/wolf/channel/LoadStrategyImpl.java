package com.wolf.channel;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.wolf.ChannelWriter.LogWriter;
import com.wolf.center.CenterHandle;
import com.wolf.center.ServerSendHandle;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.LineLog.Builder;
import com.wolf.log.LogServer;
import com.wolf.server.HttpServer;
import com.wolf.server.HttpServerHandler;

public class LoadStrategyImpl implements LoadStrategy{

	private LoadStrategyImpl(){}
	
	public static LoadStrategy INSTANCE = new LoadStrategyImpl();
	
	private int type = 0;
	
	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub
		this.type = type;
	}

	@Override
	public Channel getServerChannel(List<CenterHandle> list,
			HttpServerHandler httpHandle, String path,int z) {//http
		if(type == 2){
			return LoadStrategyImpl1.INSTANCE.getServerChannel(list, httpHandle, path, z);
		}
		System.out.println("当前负载均衡策略1");
		LogsBean logsBean = httpHandle.logsBean;
		Iterator<CenterHandle> it = list.iterator();
		Channel channel = null;
		while(it.hasNext()){
			CenterHandle ch = it.next();
			int cur = 0;
			int a = 0;
			if(ch.pathThreadPoolNumMax.containsKey(path) && ch.pathThreadPoolNumMax.get(path).get() > 0){//路径负载均衡
				cur = ch.pathThreadPoolNumMax.get(path).getAndDecrement();
				a = 1;
				System.out.println("路径负载均衡，当前负载:"+cur);
				//日志
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("路径负载均衡，当前负载:"+cur, "1",false);
				LogWriter.logWriter.write(logDatasBuilder);
			}else{
				cur = ch.serverRequestNum.getAndDecrement();
				a = 2;
				System.out.println("全局负载均衡，当前负载:"+cur);
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("全局负载均衡，当前负载:"+cur, "1",false);
				LogWriter.logWriter.write(logDatasBuilder);
			}
			if(cur <= 0){
				if(a == 1){
					ch.pathThreadPoolNumMax.get(path).incrementAndGet();
				}else if(a == 2){
					ch.serverRequestNum.incrementAndGet();
				}
				continue;
			}else{
				channel = ch.getChannel();
				httpHandle.centerHandle = ch;
				break;
			}
		}
		if((channel == null || !channel.isActive() || !channel.isOpen()) && z <= 3){//尝试重新获取,10秒退出
			channel = getServerChannel(list,httpHandle,path,z++);
		}
		return channel;
	}

	@Override
	public Channel getServerChannel(List<CenterHandle> list, String path,
			LineLog lineLog,int z) {//server
		if(type == 2){
			return LoadStrategyImpl1.INSTANCE.getServerChannel(list, path, lineLog, z);
		}
		System.out.println("当前负载均衡策略1");
		LogsBean logsBean = null;
		if(lineLog.getParentId()!=null && !lineLog.getParentId().equals("")){
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId()+"-"+lineLog.getId(),
				UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}else{
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getMainId(),
					UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}
		Iterator<CenterHandle> it = list.iterator();
		Channel channel = null;
		while(it.hasNext()){
			CenterHandle ch = it.next();
			int cur = 0;
			int a = 0;
			if(ch.pathThreadPoolNumMax.containsKey(path) && ch.pathThreadPoolNumMax.get(path).get() > 0){//路径负载均衡
				cur = ch.pathThreadPoolNumMax.get(path).getAndDecrement();
				a = 1;
				System.out.println("server路径负载均衡，当前负载:"+cur);
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("全局负载均衡，当前负载:"+cur, "1",false);
				LogWriter.logWriter.write(logDatasBuilder);
			}else{
				cur = ch.serverRequestNum.getAndDecrement();
				a = 2;
				System.out.println("server全局负载均衡，当前负载:"+cur);
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("server全局负载均衡，当前负载:"+cur, "1",false);
				LogWriter.logWriter.write(logDatasBuilder);
			}
			if(cur <= 0){
				if(a == 1){
					ch.pathThreadPoolNumMax.get(path).incrementAndGet();
				}else{
					ch.serverRequestNum.incrementAndGet();
				}
				continue;
			}else{
				channel = ch.getChannel();
				if(channel != null){
					AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
					String id = channel.attr(channelIdAttr).get();
					ServerSendHandle.centerHandle.put(id, ch);
				}
			}//这里和http的方式不太一样
		}
		if((channel == null || !channel.isActive() || !channel.isOpen()) && z <= 3){//尝试重新获取
			channel = getServerChannel(list,path, lineLog,z++);//尝试重新获取
		}
		return channel;
	}

	@Override
	public Channel getServerChannel(List<CenterHandle> list, String path,
			AtomicInteger[] aiArray, Builder lineLog,int z) {//websocket
		if(type == 2){
			return LoadStrategyImpl1.INSTANCE.getServerChannel(list, path, aiArray, lineLog, z);
		}
		System.out.println("当前负载均衡策略1");
		LogsBean logsBean = null;
		if(lineLog.getParentId()!=null && !lineLog.getParentId().equals("")){
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId()+"-"+lineLog.getId(),
				UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}else{
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getMainId(),
					UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}
		Iterator<CenterHandle> it = list.iterator();
		Channel channel = null;
		while(it.hasNext()){
			CenterHandle ch = it.next();
			int cur = 0;
			int a = 0;
			if(ch.pathThreadPoolNumMax.containsKey(path) && ch.pathThreadPoolNumMax.get(path).get() > 0){//路径负载均衡
				cur = ch.pathThreadPoolNumMax.get(path).getAndDecrement();
				a = 1;
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("路径负载均衡，当前负载:"+cur, "1",false);
				LogWriter.logWriter.write(logDatasBuilder);
			}else{
				cur = ch.serverRequestNum.getAndDecrement();
				a = 2;
				System.out.println("全局负载均衡，当前负载:"+cur);
				String id = UUID.randomUUID().toString().replace("-", "");
				LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("全局负载均衡，当前负载:"+cur, "1",false);
				LogWriter.logWriter.write(logDatasBuilder);
			}
			if(cur <= 0){
				if(a == 1){
					ch.pathThreadPoolNumMax.get(path).incrementAndGet();
				}else if(a == 2){
					ch.serverRequestNum.incrementAndGet();
				}
				continue;
			}else{
				if(a == 1){//websocket只要发送成功就扣减负载
					aiArray[0] = ch.pathThreadPoolNumMax.get(path);
				}else if(a == 2){
					aiArray[0] = ch.serverRequestNum;
				}
				channel = ch.getChannel();
				break;
			}
		}
		if((channel == null || !channel.isActive() || !channel.isOpen()) && z <= 3){//尝试重新获取
			channel = getServerChannel(list, path, aiArray, lineLog, z++);//尝试重新获取
		}
		return channel;
	}

}
