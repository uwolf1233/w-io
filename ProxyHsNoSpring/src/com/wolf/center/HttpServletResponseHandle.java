package com.wolf.center;

import java.util.Map;

import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.HttpServletResponse;
import com.wolf.javabean.SystemNet.HttpSession;
import com.wolf.locks.DistributedLock;
import com.wolf.server.ControllerProxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HttpServletResponseHandle extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;
		if(datas.getHandletype().equals("HttpServletResponse")){
			//CenterServer.pool.execute(new Thread(new MyThread(datas)));
			HttpSession session = datas.getHttpBean().getRequest().getSession();
			String sessionId = session.getSessionId();
			//Map<String,String> sessionMap = session.getSessionMap();
			if(sessionId == null || sessionId.equals("null")){
				return;
			}
			//MySession mySession = SessionMsg.INSTANCE.getSession(sessionId);
			//mySession.setServerName(session.getServerName());
			//mySession.getAttr().putAll(sessionMap);
			
			HttpServletResponse responses = datas.getHttpBean().getResponse();
			String type = responses.getType();
			
			String lockid = responses.getLocks().getId();
			if(!lockid.trim().equals("")){
				System.out.println("即将解锁id---"+lockid);
				DistributedLock dl = DistributedLock.distributedLockMap.get(lockid);
				if(dl == null){
					System.out.println("锁已经不在，应该是被锁扫描解锁");
				}else{
					dl.setReleaseQueue();//解锁
					DistributedLock.distributedLockMap.remove(lockid);
				}
			}
			
			if(responses.getFiles()!=null && !responses.getFiles().getFileName().trim().equals("")){
				ControllerProxy.INSTANCE().sendFile(responses.getFiles(), responses, datas.getHttpBean().getUrl());
			}else if(type.equals("String")){//如果是字符串，返回数据到前端
				ControllerProxy.INSTANCE().sendData(responses,datas.getHttpBean().getUrl());
			}else if(type.equals("file")){//如果是文件，读取文件
				ControllerProxy.INSTANCE().sendHtml(responses,datas.getHttpBean().getUrl());
			}
		}else{
			ctx.fireChannelRead(datas);
		}
	}
	
//	class MyThread implements Runnable{
//
//		private SystemNet.Datas datas;
//		public MyThread(SystemNet.Datas datas){
//			this.datas = datas;
//		}
//		
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			boolean flag = true;
//			while(flag){
//				try {
//					HttpSession session = datas.getHttpBean().getRequest().getSession();
//					String sessionId = session.getSessionId();
//					Map<String,String> sessionMap = session.getSessionMap();
//					if(sessionId == null || sessionId.equals("null")){
//						return;
//					}
//					MySession mySession = SessionMsg.INSTANCE.getSession(sessionId);
//					mySession.setServerName(session.getServerName());
//					mySession.getAttr().putAll(sessionMap);
//					
//					HttpServletResponse responses = datas.getHttpBean().getResponse();
//					String type = responses.getType();
//					if(type.equals("String")){
//						ControllerProxy.INSTANCE.sendData(responses);
//					}else if(type.equals("file")){
//						ControllerProxy.INSTANCE.sendHtml(responses);
//					}
//					flag = false;
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					flag = false;
//				}finally{
//					flag = false;
//				}
//			}
//		}	
//	}
	
}
