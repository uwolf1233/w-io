package com.wolf.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.WebSocketBean;
import com.wolf.javabean.WebSocketGroup;
import com.wolf.javabean.WebSocketMessage;
import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.HttpBean;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class WebSocketHandle extends ChannelInboundHandlerAdapter{

	public static ConcurrentMap<String, WebSocketGroup> groupMap = new ConcurrentHashMap<String, WebSocketGroup>();
	public static ConcurrentMap<String, WebSocketBean> webSocketBeanId = new ConcurrentHashMap<String, WebSocketBean>();
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;
		if(datas.getHandletype().equals("websocket")){
			OtherServer.pool.execute(new Thread(new MyThread(datas,ctx)));
		}else{
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	class MyThread implements Runnable{

		private SystemNet.Datas datas;
		private ChannelHandlerContext ctx;
		public MyThread(SystemNet.Datas datas,ChannelHandlerContext ctx){
			this.datas = datas;
			this.ctx = ctx;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				String path = "";
				String type = datas.getHttpBean().getWebsocketms().getType();
				if(type == null){
					//返回无类型异常
				}
				String id = datas.getHttpBean().getWebsocketms().getId();
				if(type.equals("open")){//打开连接之后第一传入数据必须是带有open类型字符串
					path = datas.getHttpBean().getWebsocketms().getPath();
					WebSocketGroup group = null;
					if(!groupMap.containsKey(path)){
						group = new WebSocketGroup();
						groupMap.put(path, group);
					}else{
						group = groupMap.get(path);
					}
					WebSocketBean webSocketBean = new WebSocketBean(path,id);
					webSocketBean.setChannel(ctx.channel());
					webSocketBeanId.put(id, webSocketBean);
					group.addGroup(webSocketBean);//添加进分组
					System.out.println("websocket已连接");
					WebSocketMessage wsMessage = new WebSocketMessage();
					wsMessage.setType("String");
					wsMessage.setMessage("openSuccess");//返回openSuccess表示连接成功
					webSocketBean.send(wsMessage);//发送message对象
					//开启连接不进入方法，如果有必要，可添加
				}else if(type.equals("message")){//如果是消息类型，则进入方法，和普通http请求一样
					WebSocketBean webSocketBean = webSocketBeanId.get(id);
					if(webSocketBean!=null){
						Datas.Builder datasBuilder = Datas.newBuilder();
						HttpBean.Builder httpBeanBuilder = datasBuilder.getHttpBeanBuilder();
						RefController.INSTANCE.webSocketInvokeMethod(datas.getHttpBean(), httpBeanBuilder, webSocketBean);
					}
				}else if(type.equals("close")){//关闭之后从组中删除
					WebSocketBean webSocketBean = webSocketBeanId.get(id);
					webSocketBeanId.remove(id);
					//groupMap.remove(path);
					path = datas.getHttpBean().getWebsocketms().getPath();
					WebSocketGroup webSocketGroup = groupMap.get(path);
					if(webSocketBean!=null){
						webSocketGroup.remove(webSocketBean);
						System.out.println("server端id为"+id+"路径为"+path+"的websocket连接关闭");
						webSocketBean.close();
					}
				}else if(type.equals("change")){//切换目录
					WebSocketBean webSocketBean = webSocketBeanId.get(id);
					String oldPath = webSocketBean.getPath();
					String newPath = datas.getHttpBean().getWebsocketms().getPath();
					WebSocketGroup webSocketGroup = groupMap.get(oldPath);//先从原有的组中删除
					webSocketGroup.remove(webSocketBean);
					webSocketBean.setPath(newPath);
					WebSocketGroup group = null;
					if(!groupMap.containsKey(newPath)){
						group = new WebSocketGroup();
						groupMap.put(newPath, group);
					}else{
						group = groupMap.get(newPath);
					}
					group.addGroup(webSocketBean);
					Datas.Builder datasBuilder = Datas.newBuilder();
					HttpBean.Builder httpBeanBuilder = datasBuilder.getHttpBeanBuilder();
					RefController.INSTANCE.webSocketInvokeMethod(datas.getHttpBean(), httpBeanBuilder, webSocketBean);
				}
			}catch(Exception e){
				e.printStackTrace();
				//进入方法异常
			}finally{
				ReferenceCountUtil.release(datas);
			}
		}
		
	}
	
}
