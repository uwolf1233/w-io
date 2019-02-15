package com.wolf.javabean;

import io.netty.channel.Channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Session.SessionData;
import com.wolf.javabean.Session.SetAttr;
import com.wolf.security.SecurityClient;
import com.wolf.server.ControllerProxy;

public class SessionLine {

	public static ConcurrentMap<String, SessionLine> SessionLineMap = new ConcurrentHashMap<String, SessionLine>();
	
	private String sessionId;
	private String path;
	private ByteString datas;
	private String end;
	private boolean overtime = true;//是否超时
	public BlockingQueue<SessionData> sessionDataQueue = new ArrayBlockingQueue<SessionData>(5);
	//private SessionData.Builder sessionBuilder = SessionData.newBuilder();
	
	public void clear_noSessionId(){
		path = null;
		datas = null;
		end = null;
		overtime = true;
		sessionDataQueue.clear();
		//sessionBuilder.clear();
	}
	
	public boolean isOvertime() {
		return overtime;
	}
	public void setOvertime(boolean overtime) {
		this.overtime = overtime;
	}
	
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public ByteString getDatas() {
		return datas;
	}
	public void setDatas(ByteString datas) {
		this.datas = datas;
	}
	
	public void create(Map<String,Object> map){//创建,凡是超时，都将到下一个session服务中
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setType("create");
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		SessionLineMap.put(id, this);
		try{
			List<SetAttr.Builder> setAttrBuilderList = new ArrayList<SetAttr.Builder>();
			if(map!=null && map.size() > 0){
				for(String key : map.keySet()){
					Object co = map.get(key);
					ByteArrayOutputStream baos = null;
					ObjectOutputStream oos = null;
					try{
						baos = new ByteArrayOutputStream();
						oos = new ObjectOutputStream(baos);
						oos.writeObject(co);
						oos.flush();
						byte[] bytes = baos.toByteArray();
						ByteString bs = ByteString.copyFrom(bytes);
						SetAttr.Builder setAttrBuilder = SetAttr.newBuilder();
						setAttrBuilder.setKey(key);
						setAttrBuilder.setDatas(bs);
						setAttrBuilderList.add(setAttrBuilder);//把初始化属性放入list
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						try {
							if(baos!=null){
								baos.close();
								baos = null;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							if(oos!=null){
								oos.close();
								oos = null;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				sessionBuilder.getCreateSessionBuilder().getSetAttrsBuilderList().addAll(setAttrBuilderList);//属性设置进去
			}
			sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
			SessionData sessionData = this.sessionDataQueue.poll(5, TimeUnit.SECONDS);//返回
			if(sessionData == null){
				this.overtime = true;//超时了
			}else{
				if(!sessionData.getType().equals("create")){
					this.overtime = true;//类型不同也被认为超时了
				}else if(sessionData.getCreateSession().getEnd().equals("sessionNull")){
					this.overtime = true;//session创建失败也被认为超时
				}else{
					this.overtime = false;
					this.sessionId = sessionData.getSessionId();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SessionLineMap.remove(id);
		}
	}
	
	public Object hasSession(){//判断session是否存在
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("hasSession");
		SessionLineMap.put(id, this);
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
		try {
			SessionData sessionData = this.sessionDataQueue.poll(2, TimeUnit.SECONDS);
			if(sessionData == null || !sessionData.getType().equals("hasSession")){
				return null;
			}else if(sessionData.getHasSession().getEnd().equals("sessionNull")){
				return "sessionNull";
			}else{
				return "success";
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean vaildPath(String path){//路径验证
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("vaildpath");
		sessionBuilder.getVaildPathBuilder().setPath(path);
		SessionLineMap.put(id, this);
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
		try {
			SessionData sessionData = this.sessionDataQueue.poll(2, TimeUnit.SECONDS);
			if(sessionData == null){
				return false;
			}else if(!sessionData.getType().equals("vaildpath") 
					|| sessionData.getVaildPath().getEnd().equals("sessionNull")){
				return false;
			}else{
				return true;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void setPath(List<String> paths){//路径设置
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("setPath");
		sessionBuilder.getSetPathBuilder().getPathsList().addAll(paths);
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
	}
	
	public boolean setAttr(String key,Object o){
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("setAttr");
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			ByteString bs = ByteString.copyFrom(bytes);
			sessionBuilder.getSetAttrBuilder().setKey(key);
			sessionBuilder.getSetAttrBuilder().setDatas(bs);
			sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
			try {
				SessionData sessionData = this.sessionDataQueue.poll(2, TimeUnit.SECONDS);
				if(sessionData == null){
					return false;
				}else if(!sessionData.getType().equals("setAttr") 
						|| sessionData.getSetAttr().getEnd().equals("sessionNull")){
					return false;
				}else {
					return true;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				if(baos != null){
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(oos != null){
					oos.close();
					oos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Object getAttr(String key){//读取属性
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("getAttr");
		sessionBuilder.getGetAttrBuilder().setKey(key);
		SessionLineMap.put(id, this);
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
		try {
			SessionData sessionData = this.sessionDataQueue.poll(2, TimeUnit.SECONDS);
			if(sessionData == null || !sessionData.getType().equals("getAttr")){
				return null;//null表示请求失败或者连接错误，不代表session不存在
			}else if(sessionData.getGetAttr().getEnd().equals("sessionNull")){
				return "sessionNull";
			}else if(sessionData.getGetAttr().getEnd().equals("No Data")){
				return "No Data";
			}
			ByteString bs = sessionData.getGetAttr().getDatas();
			byte[] bytes = bs.toByteArray();
			ByteArrayInputStream bais = null;
			ObjectInputStream bis = null;
			try {
				bais = new ByteArrayInputStream(bytes);
				bis = new ObjectInputStream(bais);
				Object o = bis.readObject();
				return o;
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}finally{
				try {
					if(bais != null){
						bais.close();
						bais = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					if(bis != null){
						bis.close();
						bis = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public Object removeAttr(String key){//删除一个属性
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("removeAttr");
		sessionBuilder.getRemoveAttrBuilder().setKey(key);
		SessionLineMap.put(id, this);
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
		try {
			SessionData sessionData = this.sessionDataQueue.poll(2, TimeUnit.SECONDS);
			if(sessionData == null || !sessionData.getType().equals("removeAttr")){
				return null;
			}else if(sessionData.getRemoveAttr().getEnd().equals("sessionNull")){
				return "sessionNull";
			}
			return "success";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Deprecated
	public void reset(){
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("Reset");
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
	}
	
	public Object clearAttr(){
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("clearAttr");
		SessionLineMap.put(id, this);
		sessionWriter(SecurityClient.sessionChannel,sessionBuilder);
		try {
			SessionData sessionData = this.sessionDataQueue.poll(2, TimeUnit.SECONDS);
			if(sessionData == null || !sessionData.getType().equals("clearAttr")){
				return null;
			}else if(sessionData.getRemoveAttr().getEnd().equals("sessionNull")){
				return "sessionNull";
			}
			return "success";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean sessionWriter(Channel channel,SessionData.Builder sessionBuilder){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(sessionBuilder);
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
	
}
