package com.wolf.server;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Session.CreateSession;
import com.wolf.javabean.Session.GetAttr;
import com.wolf.javabean.Session.RemoveAttr;
import com.wolf.javabean.Session.SessionData;
import com.wolf.javabean.Session.SetAttr;
import com.wolf.javabean.Session.SetPath;
import com.wolf.locks.AllLock;
import com.wolf.session.MySession;
import com.wolf.session.SessionMg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

public class SessionHandle extends ChannelInboundHandlerAdapter{

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
		try{
			if(msg instanceof SessionData){
				SessionData sessionData = (SessionData)msg;
				String sessionId = sessionData.getSessionId();
				SessionData.Builder sessionBuilder = SessionData.newBuilder();
				MySession session = null;
				sessionBuilder.setId(sessionData.getId());
				boolean sessionNull = false;
				if(!sessionData.getType().equals("create")){//非创建需要判断session是否存在
					session = SessionMg.INSTANCE.getSession(sessionId);
					if(session == null){
						sessionNull = true;
					}else{
						session.refreshSession();//做一次session刷新
					}
				}
				if(sessionData.getType().equals("vaildpath")){//路径验证
					sessionBuilder.setType("vaildpath");
					if(sessionNull){
						sessionBuilder.getVaildPathBuilder().setEnd("sessionNull");//这里不对
					}else{
						sessionBuilder.getVaildPathBuilder().setEnd("faild");
						String cp = sessionData.getVaildPath().getPath();//需要验证的路径
						String path[] = session.getPath();
						for(String p : path){
							if(p.equals(cp)){//如果验证成功
								sessionBuilder.getVaildPathBuilder().setEnd("success");//验证成功
							}
						}
					}
				}else if(sessionData.getType().equals("create")){//创建session
					session = SessionMg.INSTANCE.createSession();
					sessionBuilder.setType("create");
					CreateSession cs = sessionData.getCreateSession();
					if(session == null){
						sessionBuilder.getCreateSessionBuilder().setEnd("sessionNull");
						writerSession(ctx.channel(), sessionBuilder);
						return;
					}else{
						sessionBuilder.getCreateSessionBuilder().setEnd("success");
						sessionBuilder.setSessionId(session.getSessionId());
						int attrCount = cs.getSetAttrsCount();
						if(attrCount > 0){//如果有初始化属性,则将初始化属性填入当前已创建好的session
							for(int i=0;i<attrCount;i++){
								SetAttr setAttr = cs.getSetAttrs(i);
								session.setAttr(setAttr.getKey(), setAttr.getDatas());
							}
						}
					}
				}else if(sessionData.getType().equals("setAttr")){//给session设置属性
					sessionBuilder.setType("setAttr");
					if(sessionNull){
						sessionBuilder.getSetAttrBuilder().setEnd("sessionNull");//这里不对
					}else{
						sessionBuilder.getSetAttrBuilder().setEnd("success");
						SetAttr setAttr = sessionData.getSetAttr();
						session.setAttr(setAttr.getKey(), setAttr.getDatas());
					}
				}else if(sessionData.getType().equals("getAttr")){//获取session属性
					sessionBuilder.setType("getAttr");
					if(sessionNull){
						sessionBuilder.getGetAttrBuilder().setEnd("sessionNull");//这里不对
					}else{
						GetAttr getAttr = sessionData.getGetAttr();
						String key = getAttr.getKey();
						if(session.getAttr(key) == null){//如果没有这个数据
							sessionBuilder.getGetAttrBuilder().setEnd("No Data");
						}else{
							sessionBuilder.getGetAttrBuilder().setDatas(session.getAttr(key));//从session中取数
							sessionBuilder.getGetAttrBuilder().setEnd("success");
						}
					}
				}else if(sessionData.getType().equals("removeAttr")){//删除一个属性
					sessionBuilder.setType("removeAttr");
					if(sessionNull){
						sessionBuilder.getRemoveAttrBuilder().setEnd("sessionNull");//这里不对
					}else{
						RemoveAttr removeAttr = sessionData.getRemoveAttr();
						session.removeAttr(removeAttr.getKey());
						sessionBuilder.getRemoveAttrBuilder().setEnd("success");
					}
				}else if(sessionData.getType().equals("Reset")){//重置session
					sessionBuilder.setType("Reset");
					if(sessionNull){
						sessionBuilder.getResetBuilder().setEnd("sessionNull");//这里不对
					}else{
						session.resetSession();
						sessionBuilder.getResetBuilder().setEnd("success");
					}
				}else if(sessionData.getType().equals("clearAttr")){
					sessionBuilder.setType("clearAttr");
					if(sessionNull){
						sessionBuilder.getCleanAttrBuilder().setEnd("sessionNull");//这里不对
					}else{
						session.clearAttr();
						sessionBuilder.getCleanAttrBuilder().setEnd("success");
					}
				}else if(sessionData.getType().equals("setPath")){//设置路径权限，说明当前session有哪些路径的访问权限
					sessionBuilder.setType("setPath");
					if(sessionNull){
						sessionBuilder.getSetPathBuilder().setEnd("sessionNull");//这里不对
					}else{
						SetPath setPath = sessionData.getSetPath();
						try {
							AllLock.sessionPathLock.writeLock().tryLock(10, TimeUnit.SECONDS);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							sessionBuilder.getSetPathBuilder().setEnd("error");
							writerSession(ctx.channel(), sessionBuilder);
							return;
						}
						try{
							List<String> paths = setPath.getPathsList();
							String[] cpath = session.getPath();
							String[] rPath = paths.toArray(new String[paths.size()]);
							String[] newPaths = new String[cpath.length + rPath.length];
							System.arraycopy(cpath, 0, newPaths, 0, cpath.length);//先复制原有的到新数组
							System.arraycopy(rPath, 0, newPaths, cpath.length, rPath.length);//复制传递过来的到新数组
							sessionBuilder.getSetPathBuilder().setEnd("success");
						}catch(Exception e){
							e.printStackTrace();
						}finally{
							AllLock.sessionPathLock.writeLock().unlock();
						}
					}
				}else if(sessionData.getType().equals("hasSession")){//是否存在session
					sessionBuilder.setType("hasSession");
					if(sessionNull){
						sessionBuilder.getHasSessionBuilder().setEnd("sessionNull");//这里不对
					}else{
						sessionBuilder.getHasSessionBuilder().setEnd("success");
					}
				}
				writerSession(ctx.channel(), sessionBuilder);
				if(session != null){
					session.refreshSession();//做一次session刷新,前后刷新一次避免运行过程出现session被回收
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}
	
	private boolean writerSession(Channel channel,SessionData.Builder sessionBuilder){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(sessionBuilder);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == 4000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(sessionBuilder);
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

	public static void main(String[] args) {
		String s1[] = new String[]{"1","2","3"};
		String s2[] = new String[]{"4","5","6","7"};
		String s3[] = new String[]{"8","9"};
		String news[] = new String[s1.length+s2.length+s3.length];
		System.arraycopy(s1, 0, news, 0, s1.length);
		System.arraycopy(s2, 0, news, s1.length, s2.length);
		System.arraycopy(s3, 0, news, s1.length+s2.length, s3.length);
		for(String s : news){
			System.out.println(s);
		}
	}
	
}
