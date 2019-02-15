package com.wolf.server;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MySession {

	private String sessionId;
	private boolean isNull;//是否是空
	private ConcurrentMap<String,String> attr = new ConcurrentHashMap<String,String>();//内部各个属性
	private Date starttime;//开始时间
	private Date endtime;//结束时间
	private boolean unUse;//是否不能使用
	private boolean isusing;//是否正在使用
	private String serverName;//服务器名称
	public static int sessionTime = 20;//扫描时间，分钟
	
	public MySession(){
		attr.put("(_init)", "true");//先给个初始属性
		sessionId = UUID.randomUUID().toString().replace("-", "");
		unUse = false;
		refreshSession();
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
		sessionId = UUID.randomUUID().toString().replace("-", "");
		attr.clear();
		refreshSession();
		unUse = false;
		isNull = true;
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
	public boolean isUnUse() {
		return unUse;
	}
	public void setUnUse(boolean unUse) {
		this.unUse = unUse;
	}
	
	/**
	 * 刷新session
	 */
	public void refreshSession(){
		this.starttime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, sessionTime);
		endtime = calendar.getTime();
	}
	
	/**
	 * 是否运行
	 * @return
	 */
	public boolean isun(){
		Date now = new Date();
		if(now.getTime() >= endtime.getTime()){
			unUse = true;
			return false;
		}else{
			return true;
		}
		
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public ConcurrentMap<String, String> getAttr() {
		return attr;
	}

	public void setAttr(ConcurrentMap<String, String> attr) {
		this.attr = attr;
	}

	public boolean isNull() {
		return isNull;
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}
	
}
