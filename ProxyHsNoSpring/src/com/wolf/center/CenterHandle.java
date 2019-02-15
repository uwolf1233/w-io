package com.wolf.center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.wolf.javabean.ServerBean;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.Cpaths;
import com.wolf.javabean.SystemNet.RegBean;
import com.wolf.locks.DistributedLockMg;
import com.wolf.locks.SystemLocks;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

public class CenterHandle extends ChannelInboundHandlerAdapter{

	public static ConcurrentMap<String, Channel> channelMap = new ConcurrentHashMap<String,Channel>();//基本用于服务器之间调用
	private SystemNet.Datas.Builder idledatasBuilder = SystemNet.Datas.newBuilder();
	private String serverName = "";
	private Channel channel = null;
	private List<String> pathArray = null;//路径数组，所有注册的路径
	int pathArrayCount = 0;//路径数量
	public AtomicInteger serverRequestNum = null;
	public Map<String,AtomicInteger> pathThreadPoolNumMax = new HashMap<String,AtomicInteger>();//路径负载均衡
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if(msg instanceof SystemNet.Datas){
				final SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
				SystemNet.Datas datas = (SystemNet.Datas)msg;
				if(datas.getHandletype().equals("RegBean")){
					RegBean rb = datas.getRegBean();
					datasBuilder.setHandletype("RegBean");
					if(rb.getIp().trim().equals("") || rb.getPort().trim().equals("")){//没有ip或者port不允许注册
						datasBuilder.getSystemNetBeanBuilder().setMessage("ip or port is empty");
						ctx.writeAndFlush(datasBuilder).addListener(new ChannelFutureListener() {//监听是否成功
							
							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								// TODO Auto-generated method stub
								datasBuilder.getSystemNetBeanBuilder().clear();
								SystemLocks.regLock.writeLock().unlock();
							}
						});//发送注册后的信息
						return;
					}
					if(rb.getRegtype().equals("1")){//注册
						serverName = rb.getServerName();
						System.out.println(serverName+"正在进行注册");
						int serreqnum = rb.getRequestnum();
						if(serreqnum <= 0){
							serreqnum = 2;
						}
						
						Map<String,Cpaths> paths = rb.getPathsMap();
						
						serverRequestNum = new AtomicInteger(serreqnum);//请求数量，业务服务器配置(可作配置)
						//ConcurrentMap<String,List<CenterHandle>> serverMap = CenterServer.serverMap;
						ConcurrentMap<String,ServerBean> serverMap  = CenterServer.serverMap;
						pathArrayCount = paths.size();
						pathArray = new ArrayList<String>();
						for(String key : paths.keySet()){
							String path = key;
							Cpaths cpaths = paths.get(key);
							if(!serverMap.containsKey(path)){
								ServerBean serverBean = new ServerBean();
								List<CenterHandle> list = new ArrayList<CenterHandle>();
								serverBean.setChList(list);
								serverBean.setPath(path);
								serverBean.setLockNames(cpaths.getLocknames(0));//暂时只支持单锁
								serverBean.setSecondnum(cpaths.getSecondnum());
								serverBean.setGeWait(cpaths.getGeWait());
								serverBean.setTrans(cpaths.getTrans());
								serverBean.setTranCommitMinute(cpaths.getTranCommitMinute());
								serverBean.setTranRollbackMinute(cpaths.getTranRollbackMinute());
								serverBean.setThreadPoolNumMax(cpaths.getThreadPoolNumMax());
								serverBean.setHasFile(cpaths.getHasFile());
								serverMap.put(path, serverBean);
							}
							pathArray.add(path);
							System.out.println("注册的path:"+path);
							ServerBean serverBean = serverMap.get(path);
							List<CenterHandle> list = serverBean.getChList();
							list.add(this);
							
							boolean hasFile = cpaths.getHasFile();
							boolean fileIn = cpaths.getFileIn();//文件是否到方法
							String interfaceType = cpaths.getInterfacetype();
							interfaceType = interfaceType == null || interfaceType.trim().equals("") ? "s" : interfaceType;//如果没有标识，一律认为是内部接口
							serverBean.setHasFile(hasFile);
							serverBean.setFileIn(fileIn);
							serverBean.setInterfacetype(interfaceType);
							List<String> locknames = paths.get(key).getLocknamesList();
							DistributedLockMg.INSTANCE.addLock(locknames, path, cpaths.getSecondnum(), cpaths.getGeWait(),serverBean);
							int threadPoolNumMax = cpaths.getThreadPoolNumMax();
							if(threadPoolNumMax>0){
								pathThreadPoolNumMax.put(path, new AtomicInteger(threadPoolNumMax));
							}
						}
						channel = ctx.channel();
						datasBuilder.getSystemNetBeanBuilder().setMessage("Reg success");
						ctx.writeAndFlush(datasBuilder).addListener(new ChannelFutureListener() {//监听是否成功
							
							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								// TODO Auto-generated method stub
								datasBuilder.getSystemNetBeanBuilder().clear();
							}
						});
					}else if(rb.getRegtype().equals("0")){//脱离
						for(int i=0;i<pathArrayCount;i++){
							String path = pathArray.get(i)+"";
							ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
							if(serverMap.containsKey(path)){
								List<CenterHandle> list = serverMap.get(path).getChList();
								list.add(this);
								Iterator<CenterHandle> it = list.iterator();
								while(it.hasNext()){
									CenterHandle ch = it.next();
									if(ch.channel == this.channel){
										it.remove();
										break;
									}
								}
							}
						}
						datasBuilder.getSystemNetBeanBuilder().setMessage("unReg success");
						ctx.writeAndFlush(datasBuilder).addListener(new ChannelFutureListener() {//监听是否成功
							
							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								// TODO Auto-generated method stub
								datasBuilder.getSystemNetBeanBuilder().clear();
								pathThreadPoolNumMax.clear();
							}
						});
					}
				}else{
					ctx.fireChannelRead(datas);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		for(int i=0;i<pathArrayCount;i++){
			ConcurrentMap<String,ServerBean> serverMap = CenterServer.serverMap;
			serverMap.get(pathArray.get(i)).removeChList(this);
		}
		AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
		String id = ctx.channel().attr(channelIdAttr).get();
		channelMap.remove(id);
		ServerSendHandle.centerHandle.remove(id);
		super.channelInactive(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		if(evt instanceof IdleStateEvent){
			IdleStateEvent event = (IdleStateEvent)evt;
			if(event.state() == IdleState.READER_IDLE){
				idledatasBuilder.getSystemNetBeanBuilder().setMessage("is Running?");
				ctx.writeAndFlush(idledatasBuilder).addListener(new ChannelFutureListener() {//监听是否成功
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						// TODO Auto-generated method stub
						idledatasBuilder.getSystemNetBeanBuilder().clear();
					}
				});
			}
		}
		super.userEventTriggered(ctx, evt);
	}

	public SystemNet.Datas.Builder getIdledatasBuilder() {
		return idledatasBuilder;
	}

	public void setIdledatasBuilder(SystemNet.Datas.Builder idledatasBuilder) {
		this.idledatasBuilder = idledatasBuilder;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public List<String> getPathArray() {
		return pathArray;
	}

	public void setPathArray(List<String> pathArray) {
		this.pathArray = pathArray;
	}

	public int getPathArrayCount() {
		return pathArrayCount;
	}

	public void setPathArrayCount(int pathArrayCount) {
		this.pathArrayCount = pathArrayCount;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String uuid = UUID.randomUUID().toString().replace("-", "");
		Channel channel = ctx.channel();
		channelMap.put(uuid, channel);
		AttributeKey<String> channelIdAttr = AttributeKey.valueOf("channelId");
		channel.attr(channelIdAttr).set(uuid);
		super.channelActive(ctx);
	}
	
}
