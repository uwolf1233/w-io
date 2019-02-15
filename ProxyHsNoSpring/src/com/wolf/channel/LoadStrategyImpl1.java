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

public class LoadStrategyImpl1 implements LoadStrategy {

	private LoadStrategyImpl1(){}
	
	public static LoadStrategy INSTANCE = new LoadStrategyImpl1();
	
	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub

	}

	//AtomicInteger minhttp = new AtomicInteger(0);
	
	@Override
	public Channel getServerChannel(List<CenterHandle> list,
			HttpServerHandler httpHandle, String path,int z) {//http
		System.out.println("当前负载均衡策略2");
		LogsBean logsBean = httpHandle.logsBean;
		if(list.size() == 0){
			return null;
		}
		Iterator<CenterHandle> it = list.iterator();
		int a = 0;
		Channel channel = null;
		CenterHandle ch = null;
		AtomicInteger pathnum = null;
		int num = 0;//全局负载配置
		while(it.hasNext()){//当前逻辑必须在所有集群接口配置等同的情况下
			CenterHandle cch = it.next();
			if(cch == null){
				continue;
			}
			if(cch.pathThreadPoolNumMax.containsKey(path)){
				if(!(cch.getChannel().isOpen() && cch.getChannel().isActive())){
					cch = null;
					continue;
				}
				AtomicInteger cpathnum = cch.pathThreadPoolNumMax.get(path);
				int curtomin = cpathnum.get();//对比
				if(num == 0){
					if(curtomin>0){
						ch = cch;
						num = curtomin;
						pathnum = cpathnum;
					}
				}else if(curtomin>num){//判断是否最大，优先获取最大
					ch = cch;
					num = curtomin;
					pathnum = cpathnum;
				}
				a = 1;
			}else{
				if(!(cch.getChannel().isOpen() && cch.getChannel().isActive())){
					cch = null;
					continue;
				}
				AtomicInteger cpathnum = cch.serverRequestNum;
				int curtomin = cpathnum.get();//对比
				if(num == 0){
					if(curtomin>0){
						ch = cch;
						num = curtomin;
						pathnum = cpathnum;
					}
				}else if(curtomin>num){//判断是否最大，优先获取最大
					ch = cch;
					num = curtomin;
					pathnum = cpathnum;
				}
				a = 2;
			}
			if(num <= 0){
				continue;
			}
		}
		if(ch == null  && z <= 100){
			channel = getServerChannel(list,httpHandle,path,++z);
		}else{
			if(ch != null){
				httpHandle.centerHandle = ch;
				if(a == 1){
					System.out.println("路径负载均衡，当前负载:"+pathnum.decrementAndGet());
				}else if(a == 2){
					System.out.println("路径负载均衡，当前负载:"+pathnum.decrementAndGet());
				}
				channel = ch.getChannel();
				if(a == 1){
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("路径负载均衡，当前负载:"+num, "1",false);
					LogWriter.logWriter.write(logDatasBuilder);
				}else if(a==2){
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("全局负载均衡，当前负载:"+num, "1",false);
					LogWriter.logWriter.write(logDatasBuilder);
				}
			}
		}
		return channel;
	}
	
	@Override
	public Channel getServerChannel(List<CenterHandle> list, String path,
			LineLog lineLog,int z) {//server
		System.out.println("当前负载均衡策略2");
		LogsBean logsBean = null;
		if(lineLog.getParentId()!=null && !lineLog.getParentId().equals("")){
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId()+"-"+lineLog.getId(),
				UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}else{
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getMainId(),
					UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}
		Iterator<CenterHandle> it = list.iterator();
		int a = 0;
		Channel channel = null;
		CenterHandle ch = null;
		AtomicInteger pathnum = null;
		int num = 0;//全局负载配置
		while(it.hasNext()){//当前逻辑必须在所有集群接口配置等同的情况下
			CenterHandle cch = it.next();
			if(cch == null){
				continue;
			}
			if(cch.pathThreadPoolNumMax.containsKey(path)){//路径负载均衡
				if(!(cch.getChannel().isOpen() && cch.getChannel().isActive())){
					cch = null;
					continue;
				}
				AtomicInteger cpathnum = cch.pathThreadPoolNumMax.get(path);
				int curtomin = cpathnum.get();//对比
				if(num == 0){
					if(curtomin>0){
						ch = cch;
						num = curtomin;
						pathnum = cpathnum;
					}
				}else if(curtomin>num){//判断是否最大，优先获取最大
					ch = cch;
					num = curtomin;
					pathnum = cpathnum;
				}
				a = 1;
			}else{
				if(!(cch.getChannel().isOpen() && cch.getChannel().isActive())){
					cch = null;
					continue;
				}
				AtomicInteger cpathnum = cch.serverRequestNum;
				int curtomin = cpathnum.get();//对比
				if(num == 0){
					if(curtomin>0){
						ch = cch;
						num = curtomin;
						pathnum = cpathnum;
					}
				}else if(curtomin>num){//判断是否最大，优先获取最大
					ch = cch;
					num = curtomin;
					pathnum = cpathnum;
				}
				a = 2;
			}
		}
		if(ch == null  && z <= 100){//尝试重新获取
			channel = getServerChannel(list,path, lineLog,z++);//尝试重新获取
		}else{
			if(ch != null){//这边后续测试，目前先做http
				channel = ch.getChannel();
				if(channel != null){
					AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
					String id = channel.attr(channelIdAttr).get();
					ServerSendHandle.centerHandle.put(id, ch);
				}//这里和http的方式不太一样
				if(a == 1){
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("路径负载均衡，当前负载:"+num, "1",false);
					LogWriter.logWriter.write(logDatasBuilder);
				}else if(a==2){
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("全局负载均衡，当前负载:"+num, "1",false);
					LogWriter.logWriter.write(logDatasBuilder);
				}
			}
		}
		return channel;
	}

	AtomicInteger minwebsocket = new AtomicInteger(0);
	
	@Override
	public Channel getServerChannel(List<CenterHandle> list, String path,
			AtomicInteger[] aiArray, Builder lineLog,int z) {//websocket
		System.out.println("当前负载均衡策略2");
		LogsBean logsBean = null;
		if(lineLog.getParentId()!=null && !lineLog.getParentId().equals("")){
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId()+"-"+lineLog.getId(),
				UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}else{
			logsBean = new LogsBean(lineLog.getMainId(), lineLog.getMainId(),
					UUID.randomUUID().toString().replace("-", ""), path, "proxyHs", HttpServer.ip);
		}
		Iterator<CenterHandle> it = list.iterator();
		int a = 0;
		Channel channel = null;
		CenterHandle ch = null;
		AtomicInteger pathnum = null;
		int num = 0;//全局负载配置
		while(it.hasNext()){//当前逻辑必须在所有集群接口配置等同的情况下
			CenterHandle cch = it.next();
			if(cch == null){
				continue;
			}
			if(cch.pathThreadPoolNumMax.containsKey(path)){//路径负载均衡
				if(!(cch.getChannel().isOpen() && cch.getChannel().isActive())){
					cch = null;
					continue;
				}
				AtomicInteger cpathnum = cch.pathThreadPoolNumMax.get(path);
				int curtomin = cpathnum.get();//对比
				if(num == 0){
					if(curtomin>0){
						ch = cch;
						num = curtomin;
						pathnum = cpathnum;
					}
				}else if(curtomin>num){//判断是否最大，优先获取最大
					ch = cch;
					num = curtomin;
					pathnum = cpathnum;
				}
				a = 1;
			}else{
				if(!(cch.getChannel().isOpen() && cch.getChannel().isActive())){
					cch = null;
					continue;
				}
				AtomicInteger cpathnum = cch.serverRequestNum;
				int curtomin = cpathnum.get();//对比
				if(num == 0){
					if(curtomin>0){
						ch = cch;
						num = curtomin;
						pathnum = cpathnum;
					}
				}else if(curtomin>num){//判断是否最大，优先获取最大
					ch = cch;
					num = curtomin;
					pathnum = cpathnum;
				}
				a = 2;
			}
			if(num <= 0){
				continue;
			}
		}
		if(ch == null  && z <= 100){//尝试重新获取
			channel = getServerChannel(list, path, aiArray, lineLog, z++);//尝试重新获取
		}else{
			if(ch != null){
				if(a == 1){
					System.out.println("路径负载均衡，当前负载:"+pathnum.get());//这里后面改，websocket应该发完就确认，不需要扣减负载数
				}else if(a == 2){
					System.out.println("路径负载均衡，当前负载:"+pathnum.get());
				}
				channel = ch.getChannel();
				if(a == 1){
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("路径负载均衡，当前负载:"+num, "1",false);
					LogWriter.logWriter.write(logDatasBuilder);
				}else if(a==2){
					LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("全局负载均衡，当前负载:"+num, "1",false);
					LogWriter.logWriter.write(logDatasBuilder);
				}
			}
		}
		return channel;
	}

}
