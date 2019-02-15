package com.wolf.channel;

import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.wolf.center.CenterHandle;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.server.HttpServerHandler;

//负载均衡策略接口
public interface LoadStrategy {

	public void setType(int type);
	
	public Channel getServerChannel(List<CenterHandle> list,HttpServerHandler httpHandle,String path,int z);//http
	
	public Channel getServerChannel(List<CenterHandle> list,String path,LineLog lineLog,int z);//server
	
	public Channel getServerChannel(List<CenterHandle> list,String path,AtomicInteger[] aiArray,LineLog.Builder lineLog,int z);//websocket
	
	
	
}
