package com.wolf.javabean;

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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Session.SessionData;
import com.wolf.javabean.Session.SetAttr;
import com.wolf.result.GetResults;
import com.wolf.result.GetResult;
import com.wolf.server.SecurityClient;

public class SessionLine{

	//public static ConcurrentMap<String, SessionLine> SessionLineMap = new ConcurrentHashMap<String, SessionLine>();
	public static ConcurrentMap<String, BlockingQueue> SessionLineMap = new ConcurrentHashMap<String, BlockingQueue>();
	
	private String sessionId;
	private String path;
	private ByteString datas;
	private String end;
	private boolean overtime = true;//是否超时
	//public BlockingQueue<SessionData> sessionDataQueue = new ArrayBlockingQueue<SessionData>(5);
	//private SessionData.Builder sessionBuilder = SessionData.newBuilder();
	
	public void clear_noSessionId(){
		path = null;
		datas = null;
		end = null;
		overtime = true;
		//sessionDataQueue.clear();
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
	
//	public void create(Map<String,Object> map){
//		WioThreadResult wioThreadResult = new WioThreadResult(this.sessionDataQueue).getData();
//		WioThreadPool.pool.execute(new RunSessionLine("create",new Object[]{map},wioThreadResult));
//	}
	
	public void create(Map<String,Object> map){//创建,凡是超时，都将到下一个session服务中
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setType("create");
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
		SessionLineMap.put(id, sessionDataQueue);
		try{
			List<SetAttr> setAttrBuilderList = new ArrayList<SetAttr>();
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
						setAttrBuilderList.add(setAttrBuilder.build());//把初始化属性放入list
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
				//sessionBuilder.getCreateSessionBuilder().getSetAttrsBuilderList().addAll(setAttrBuilderList);//属性设置进去
				sessionBuilder.getCreateSessionBuilder().addAllSetAttrs(setAttrBuilderList);
			}
			writeSessionBuilder(sessionBuilder);
			SessionData sessionData = sessionDataQueue.poll(5, TimeUnit.SECONDS);//返回
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
	
	private void writeSessionBuilder(SessionData.Builder sessionBuilder){
		SecurityClient.sessionChannel.writeAndFlush(sessionBuilder);
	}
	
	public void hasSession(GetResult getResult){
		WioThreadResult wioThreadResult = new WioThreadResult(new SynchronousQueue<SessionData>());
		RunSessionLine rsl = new RunSessionLine("hasSession", null, wioThreadResult,getResult);
		WioThreadPool.pool.execute(rsl);
	}
	
	public Object hasSessionRun(WioThreadResult wioThreadResult){//判断session是否存在
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("hasSession");
		if(wioThreadResult != null){
			SessionLineMap.put(id, wioThreadResult.getQueue());
		}else{
			BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
			SessionLineMap.put(id, sessionDataQueue);
		}
		writeSessionBuilder(sessionBuilder);
		try {
			Object ro = null;
			if(wioThreadResult != null){
				ro = wioThreadResult.getData().getO();
			}else{
				ro = SessionLineMap.get(id).poll(5, TimeUnit.SECONDS);
			}
			if(ro == null){
				return null;
			}else{
				SessionData sessionData = (SessionData)ro;
				if(!sessionData.getType().equals("hasSession") || sessionData.getHasSession().getEnd().equals("sessionNull")){
					return "sessionNull";
				}else{
					return "success";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			SessionLineMap.remove(id);
		}
	}
	
	public void vaildPath(String path,GetResult getResult){
		WioThreadResult wioThreadResult = new WioThreadResult(new SynchronousQueue<SessionData>());
		RunSessionLine rsl = new RunSessionLine("vaildPath", new Object[]{path}, wioThreadResult,getResult);
		WioThreadPool.pool.execute(rsl);
	}
	
	public boolean vaildPathRun(String path,WioThreadResult wioThreadResult){//路径验证
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("vaildpath");
		sessionBuilder.getVaildPathBuilder().setPath(path);
		if(wioThreadResult != null){
			SessionLineMap.put(id, wioThreadResult.getQueue());
		}else{
			BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
			SessionLineMap.put(id, sessionDataQueue);
		}
		writeSessionBuilder(sessionBuilder);
		try {
			Object ro = null;
			if(wioThreadResult != null){
				ro = wioThreadResult.getData().getO();
			}else{
				ro = SessionLineMap.get(id).poll(5, TimeUnit.SECONDS);
			}
			if(ro == null){
				return false;
			}else{
				SessionData sessionData = (SessionData)ro;
				if(!sessionData.getType().equals("vaildpath") 
						|| sessionData.getVaildPath().getEnd().equals("sessionNull")){
					return false;
				}else{
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}finally{
			SessionLineMap.remove(id);
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
		SecurityClient.sessionChannel.writeAndFlush(sessionBuilder);
	}
	
	public void setAttr(String key,Object o,GetResult getResult){
		WioThreadResult wioThreadResult = new WioThreadResult(new SynchronousQueue<SessionData>());
		RunSessionLine rsl = new RunSessionLine("setAttr", new Object[]{key,o}, wioThreadResult,getResult);
		WioThreadPool.pool.execute(rsl);
	}
	
	public boolean setAttrRun(String key,Object o,WioThreadResult wioThreadResult){
//		long starttime = System.currentTimeMillis();
		clear_noSessionId();//做一次清空
		
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("setAttr");
		if(wioThreadResult != null){
			SessionLineMap.put(id, wioThreadResult.getQueue());
		}else{
			BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
			SessionLineMap.put(id, sessionDataQueue);
		}
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
			writeSessionBuilder(sessionBuilder);
			
			try {
				Object ro = null;
				if(wioThreadResult != null){
					ro = wioThreadResult.getData().getO();
				}else{
					ro = SessionLineMap.get(id).poll(5, TimeUnit.SECONDS);
				}
				if(ro == null){
					return false;
				}else{
					SessionData sessionData = (SessionData)ro;
					if(!sessionData.getType().equals("setAttr") 
							|| sessionData.getSetAttr().getEnd().equals("sessionNull")){
						return false;
					}else {
						return true;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			SessionLineMap.remove(id);
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
	
	public void getAttr(String key,GetResult getResult){
		WioThreadResult wioThreadResult = new WioThreadResult(new SynchronousQueue<SessionData>());
		RunSessionLine rsl = new RunSessionLine("getAttr", new Object[]{key}, wioThreadResult,getResult);
		WioThreadPool.pool.execute(rsl);
	}
	
	public Object getAttrRun(String key,WioThreadResult wioThreadResult){//读取属性
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("getAttr");
		sessionBuilder.getGetAttrBuilder().setKey(key);
		if(wioThreadResult != null){
			SessionLineMap.put(id, wioThreadResult.getQueue());
		}else{
			BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
			SessionLineMap.put(id, sessionDataQueue);
		}
		writeSessionBuilder(sessionBuilder);
		try {
			Object ro = null;
			if(wioThreadResult != null){
				ro = wioThreadResult.getData().getO();
			}else{
				ro = SessionLineMap.get(id).poll(5, TimeUnit.SECONDS);
			}
			if(ro == null){
				return false;
			}else{
				SessionData sessionData = (SessionData)ro;
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
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			SessionLineMap.remove(id);
		}
	}
	
	public void removeAttr(String key,GetResult getResult){
		WioThreadResult wioThreadResult = new WioThreadResult(new SynchronousQueue<SessionData>());
		RunSessionLine rsl = new RunSessionLine("removeAttr", new Object[]{key}, wioThreadResult,getResult);
		WioThreadPool.pool.execute(rsl);
	}
	
	public Object removeAttrRun(String key,WioThreadResult wioThreadResult){//删除一个属性
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("removeAttr");
		sessionBuilder.getRemoveAttrBuilder().setKey(key);
		if(wioThreadResult != null){
			SessionLineMap.put(id, wioThreadResult.getQueue());
		}else{
			BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
			SessionLineMap.put(id, sessionDataQueue);
		}
		writeSessionBuilder(sessionBuilder);
		try {
			Object ro = null;
			if(wioThreadResult != null){
				ro = wioThreadResult.getData().getO();
			}else{
				ro = SessionLineMap.get(id).poll(5, TimeUnit.SECONDS);
			}
			if(ro == null){
				return false;
			}else{
				SessionData sessionData = (SessionData)ro;
				if(sessionData == null || !sessionData.getType().equals("removeAttr")){
					return null;
				}else if(sessionData.getRemoveAttr().getEnd().equals("sessionNull")){
					return "sessionNull";
				}
			}
			return "success";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			SessionLineMap.remove(id);
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
		SecurityClient.sessionChannel.writeAndFlush(sessionBuilder);
	}
	
	public void clearAttr(GetResult getResult){
		WioThreadResult wioThreadResult = new WioThreadResult(new SynchronousQueue<SessionData>());
		RunSessionLine rsl = new RunSessionLine("clearAttr", null, wioThreadResult,getResult);
		WioThreadPool.pool.execute(rsl);
	}
	
	public Object clearAttrRun(WioThreadResult wioThreadResult){
		clear_noSessionId();//做一次清空
		SessionData.Builder sessionBuilder = SessionData.newBuilder();
		sessionBuilder.setSessionId(sessionId);
		String id = UUID.randomUUID().toString().replace("-", "");
		sessionBuilder.setId(id);
		sessionBuilder.setType("clearAttr");
		if(wioThreadResult != null){
			SessionLineMap.put(id, wioThreadResult.getQueue());
		}else{
			BlockingQueue<SessionData> sessionDataQueue = new SynchronousQueue<SessionData>();
			SessionLineMap.put(id, sessionDataQueue);
		}
		writeSessionBuilder(sessionBuilder);
		try {
			Object ro = null;
			if(wioThreadResult != null){
				ro = wioThreadResult.getData().getO();
			}else{
				ro = SessionLineMap.get(id).poll(5, TimeUnit.SECONDS);
			}
			if(ro == null){
				return false;
			}else{
				SessionData sessionData = (SessionData)ro;
				if(sessionData == null || !sessionData.getType().equals("clearAttr")){
					return null;
				}else if(sessionData.getRemoveAttr().getEnd().equals("sessionNull")){
					return "sessionNull";
				}
			}
			return "success";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			SessionLineMap.remove(id);
		}
	}

	class RunSessionLine implements Runnable{
		
		public String runfun = "";
		public Object[] param = null; 
		public WioThreadResult wioThreadResult = null;
		public GetResult getResult = null;
		public RunSessionLine(String runfun,Object[] param,WioThreadResult wioThreadResult,GetResult getResult){
			this.runfun = runfun;
			this.param = param;
			this.wioThreadResult = wioThreadResult;
			this.getResult = getResult;
		}
	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(runfun.equals("setAttr")){
				boolean b = setAttrRun((String)param[0], param[1], wioThreadResult);
				if(getResult!=null){
					getResult.isBooleanData(b);
				}
			}else if(runfun.equals("hasSession")){
				Object co = hasSessionRun(wioThreadResult);
				if(getResult!=null){
					getResult.getObjectData(co);
				}
			}else if(runfun.equals("vaildPath")){
				boolean b = vaildPathRun((String)param[0], wioThreadResult);
				if(getResult!=null){
					getResult.isBooleanData(b);
				}
			}else if(runfun.equals("getAttr")){
				Object co = getAttrRun((String)param[0], wioThreadResult);
				if(getResult!=null){
					getResult.getObjectData(co);
				}
			}else if(runfun.equals("removeAttr")){
				Object co = removeAttrRun((String)param[0], wioThreadResult);
				if(getResult!=null){
					getResult.getObjectData(co);
				}
			}else if(runfun.equals("clearAttr")){
				Object co = clearAttrRun(wioThreadResult);
				if(getResult!=null){
					getResult.getObjectData(co);
				}
			}
		}
	}
	
}
