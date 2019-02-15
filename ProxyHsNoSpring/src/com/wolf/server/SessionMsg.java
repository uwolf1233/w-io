package com.wolf.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.wolf.locks.SystemLocks;

public class SessionMsg {
	
	//INSTANCE;
	
	public static SessionMsg INSTANCE = new SessionMsg();
	
	private SessionMsg(){}

	public void setSessionScanMinute(long sessionScanMinute) {
		this.sessionScanMinute = sessionScanMinute;
	}

//	private List<MySession> sessionArrya = new ArrayList<MySession>();
//	private List<MySession> sessionPool = new ArrayList<MySession>();
	private MySession[][] sessionPool = null;//session池
	private int poolsNum = 100;
	private int poolNum = 100;
	private long sessionScanMinute = 2;//session扫描（分钟）
	
	public void init(){
		sessionPool = new MySession[poolsNum][];
		for(int i=0;i<poolsNum;i++){
			MySession[] sessions = new MySession[poolNum];
			sessionPool[i] = sessions;
			for(int j=0;j<poolNum;j++){
				MySession session = new MySession();
				sessions[j] = session;
			}
		}
	}
	
	/**
	 * 获取session，根据sessionId
	 * @param sessionId
	 * @return
	 */
	public MySession getSession(String sessionId){
		MySession session = null;
		MySession sessionNullSession = null;
		if(sessionId == null){
			return createSession(session, sessionNullSession);
		}
		SystemLocks.sessionGetLock.readLock().lock();
		try {
			poolsNumFor:
			for(int i=0;i<poolsNum;i++){
				MySession[] sessions = sessionPool[i];
				for(int j=0;j<poolNum;j++){
					session = sessions[j];
					if(sessionId.equals(session.getSessionId())){
						break poolsNumFor;
					}else{
						session = null;
					}
					if(sessionNullSession == null && (session == null || session.isNull())){
						sessionNullSession = sessions[j];
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SystemLocks.sessionGetLock.readLock().unlock();
		}
		if(session!=null){
			session.refreshSession();
			return session;
		}
		return createSession(session, sessionNullSession);
	}
	
	private MySession createSession(MySession session,MySession sessionNullSession){
		SystemLocks.sessionGetLock.writeLock().lock();
		try {
			if(session == null && sessionNullSession != null){
				session = sessionNullSession;
			}else if(session == null && sessionNullSession == null){//加锁
				poolsNum += 1;
				MySession[][] sessionPool1 = new MySession[poolsNum][];
				System.arraycopy(sessionPool, 0, sessionPool1, 0, poolsNum-1);
				sessionPool = sessionPool1;
				MySession[] sessions = new MySession[poolNum];
				sessionPool[poolsNum-1] = sessions;
				for(int j=0;j<poolNum;j++){
					MySession csession = new MySession();
					csession.refreshSession();
					sessions[j] = csession;
				}
				session = sessions[0];
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SystemLocks.sessionGetLock.writeLock().unlock();
		}
		session.setUnUse(false);
		return session;
	}
	
	/**
	 * session扫描，判断session是否超时，超时后重置
	 */
	public void scanSessions(){
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
	      
	    service.scheduleAtFixedRate(new Runnable() {  
	          
	        @Override  
	        public void run() {  
	        	SystemLocks.sessionGetLock.writeLock().lock();
	        	try {
					for(int i=0;i<poolsNum;i++){
						MySession[] sessions = sessionPool[i];
						for(int j=0;j<poolNum;j++){
							MySession session = sessions[j];
							if(!session.isun() && !session.isIsusing()){
								if(!session.isun() && !session.isIsusing()){
									session.resetSession();
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					SystemLocks.sessionGetLock.writeLock().unlock();
				}
	              
	        }  
	    }, 1, sessionScanMinute,TimeUnit.MINUTES);  
	}
	
}
