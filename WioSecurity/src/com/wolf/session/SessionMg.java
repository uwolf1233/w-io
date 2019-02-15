package com.wolf.session;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.protobuf.ByteString;
import com.wolf.locks.AllLock;

public class SessionMg {

	public static SessionMg INSTANCE = new SessionMg();
	
	private SessionMg(){}
	
	private MySession[][] sessionPool = null;
	public int poolsNum = 20;
	public int poolNum = 5000;
	public long sessionScanMinute = 2;
	private BlockingQueue<MySession> sessionQueue = new ArrayBlockingQueue<MySession>(1000);
	
	public void init(int jvmlsh){//初始化，jvm流水号
		System.out.println("正在准备初始化session池");
		sessionPool = new MySession[poolsNum][];
		for(int i=0;i<poolsNum;i++){
			MySession[] sessions = new MySession[poolNum];
			sessionPool[i] = sessions;
			for(int j=0;j<poolNum;j++){
				MySession session = new MySession(String.format("%03d", i+1),String.format("%03d", jvmlsh));
				sessions[j] = session;
				if(sessionQueue.size()<800){
					sessionQueue.add(session);
				}
			}
		}
		System.out.println("session池初始化完成");
	}
	
	public MySession createSession(){//创建session
		MySession session = null;
		try {
			session = sessionQueue.poll(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}
	
	public MySession getSession(String sessionId){//获取session
		try {
			String mglsh = sessionId.substring(32, 35);
			int mglshint = Integer.parseInt(mglsh);
			MySession[] mss = sessionPool[mglshint-1];
			MySession csession = null;
			for(MySession session : mss){
				if(session.readLock()){//先持有读锁再操作
					try {
						if(session.getSessionId().equals(sessionId)){
							csession = session;
							break;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						session.unReadLock();
					}
				}
			}
			return csession;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean vaildPath(String sessionId,String path){//验证路径权限
		MySession session = getSession(sessionId);
		boolean b = false;
		if(session != null){
			session.refreshSession();
			String[] paths = session.path;
			for(String s : paths){
				if(s.equals(path)){
					b = true;
					break;
				}
			}
		}
		return b;
	}
	
	public boolean setAttr(String sessionId,String key,ByteString byteString){//添加属性
		MySession session = getSession(sessionId);
		boolean b = false;
		if(session != null){
			session.refreshSession();
			session.setAttr(key, byteString);
		}
		return b;
	}
	
	public Object getAttr(String sessionId,String key){//获取属性
		MySession session = getSession(sessionId);
		if(session != null){
			session.refreshSession();
			return session.getAttr(key);
		}else{
			return -1;
		}
	}
	
	public void resetSession(String sessionId){//重置
		MySession session = getSession(sessionId);
		if(session != null){
			session.resetSession();
		}
	}
	
	public void removeAttr(String key,String sessionId){
		MySession session = getSession(sessionId);
		if(session != null){
			session.refreshSession();
			session.removeAttr(key);
		}
	}
	
	public void clearAttr(String sessionId){
		MySession session = getSession(sessionId);
		if(session != null){
			session.refreshSession();
			session.clearAttr();
		}
	}
	
	public void initScan(){//扫描
		for(int i=0;i<poolsNum;i++){
			final int a = i;
			ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
		      
		    service.scheduleAtFixedRate(new Runnable() {  
		          
		        @Override  
		        public void run() {  
		        	try {
						MySession[] mss = sessionPool[a];
						for(MySession session : mss){//循环当前组
							if(session.writeLock()){//先持有写锁再操作
								try {
									if((!session.isun() || session.isNull()) && session.isIsusing()){//判断状态
										session.resetSession();
										if(!sessionQueue.contains(session) && sessionQueue.size()<900){
											sessionQueue.offer(session, 1, TimeUnit.SECONDS);
										}
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}finally{
									session.unWriteLock();
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		              
		        }  
		    }, sessionScanMinute, sessionScanMinute,TimeUnit.SECONDS);  
		}
	}
	
//	public static void main(String[] args) {
//		SessionMg.INSTANCE.init(1);
//		long start = System.currentTimeMillis();
//		ReentrantReadWriteLock rwlock = AllLock.sessionmgLock[0];
//    	rwlock.writeLock().lock();
//    	try {
//			MySession[] mss = SessionMg.INSTANCE.sessionPool[0];
//			for(MySession session : mss){
//				if((!session.isun() || session.isNull()) && !SessionMg.INSTANCE.sessionQueue.contains(session) ){
//					session.resetSession();
//					if(SessionMg.INSTANCE.sessionQueue.size()<900){
//						SessionMg.INSTANCE.sessionQueue.offer(session, 1, TimeUnit.SECONDS);
//					}
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//			rwlock.writeLock().unlock();
//		}
//    	long end = System.currentTimeMillis();
//		System.out.println(end+"---"+start);
//	}
	
}
