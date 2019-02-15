package com.wolf.session;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.protobuf.ByteString;

public class MySession {

	private String sessionId;//32到35位是session组流水，后面的才是jvm流水
	private boolean isNull = true;//是否是空
	private ConcurrentMap<String,ByteString> attr = new ConcurrentHashMap<String,ByteString>();//内部各个属性
	private Date starttime;//开始时间
	private Date endtime;//结束时间
	private boolean isusing;//是否正在使用
	public static int sessionTime = 20;//超时时间，分钟
	public String[] path;//path授权
	private ReentrantReadWriteLock sessionLock = new ReentrantReadWriteLock(false);
	
	private MySession(){}
	
	public String[] getPath() {
		return path;
	}

	public MySession setPath(String[] path) {
		this.path = path;return this;
	}

	public MySession(String mglsh,String jvmlsh){
		sessionId = UUID.randomUUID().toString().replace("-", "")+mglsh+jvmlsh;
		isusing = false;
		isNull = false;
		//refreshSession();
	}
	
	public boolean isIsusing() {
		return isusing;
	}

	public void setIsusing(boolean isusing) {
		this.isusing = isusing;
	}
	
	/**
	 * 重置session
	 */
	public void resetSession(){
		String oldSessionId = sessionId;
		String ls = oldSessionId.substring(32);
		sessionId = UUID.randomUUID().toString().replace("-", "") + ls;//加上当前流水
		attr.clear();
		path = null;
		starttime = null;
		endtime = null;
		isusing = false;
		isNull = true;
		System.out.println("resetSession:"+oldSessionId);
	}
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	
	/**
	 * 刷新session
	 */
	public void refreshSession(){
		this.starttime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, sessionTime);
		endtime = calendar.getTime();
		isusing = true;
		isNull = false;
	}
	
	/**
	 * 是否运行,只能用于扫描判断
	 * @return
	 */
	public boolean isun(){
		if(endtime == null){
			return false;
		}
		Date now = new Date();
		if(now.getTime() >= endtime.getTime()){
			return false;
		}else{
			return true;
		}
		
	}

	public boolean isNull() {
		return isNull;
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}
	
	public ByteString getAttr(String key){
		return attr.get(key);
	}
	
	public void setAttr(String key,ByteString byteString){
		attr.put(key, byteString);
	}
	
	public boolean writeLock(){
		boolean b = false;
		try {
			b = sessionLock.writeLock().tryLock(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}
	
	public void unWriteLock(){
		sessionLock.writeLock().unlock();
	}
	
	public boolean readLock(){
		boolean b = false;
		try {
			b = sessionLock.readLock().tryLock(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}
	
	public void unReadLock(){
		sessionLock.readLock().unlock();
	}
	
	public void removeAttr(String key){
		this.attr.remove(key);
	}
	
	public void clearAttr(){
		this.attr.clear();
	}
	
}
