package com.wolf.javabean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.center.CenterHandle;

public class ServerBean {

	private List<CenterHandle> chList;
	private String[] serverLock;
	private boolean hasFile;
	private boolean fileIn;
	private String interfacetype;
	private String path;
	private String lockNames;
	private int secondnum;//锁时长
	private long geWait;//获取锁等待时长
	private boolean trans;//是否启动事务
	private long tranCommitMinute;//超时提交时长(秒)默认10秒
	private long tranRollbackMinute;//超时回滚时长(秒)默认10秒
	private int threadPoolNumMax;//路径负载配置，没有配置则使用全局负载
	private boolean isWebSocket;//是否websocket
	
	public ReentrantReadWriteLock thisLock = new ReentrantReadWriteLock(false);
	
	public void removeChList(CenterHandle ch){//从注册中心组中删除
		thisLock.writeLock().lock();
		try{
			boolean b = chList.remove(ch);
			if(b){
				System.out.println("remove:Node success");
			}else{
				System.out.println("remove:Node error");
			}
			return;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			thisLock.writeLock().unlock();
		}
	}
	
	public Map<String,Object> getMap(String cinterfacetype){
		Map<String,Object> map = new HashMap<String,Object>();
		thisLock.readLock().lock();
		try {
			if(!cinterfacetype.equals(this.interfacetype)){return null;}
			map.put("serverLock", serverLock);
			map.put("fileIn", fileIn);
			map.put("interfacetype", interfacetype);
			map.put("path", path);
			map.put("lockNames", lockNames);
			map.put("secondnum", secondnum);
			map.put("geWait", geWait);
			map.put("trans", trans);
			map.put("tranCommitMinute", tranCommitMinute);
			map.put("tranRollbackMinute", tranRollbackMinute);
			map.put("threadPoolNumMax", threadPoolNumMax);
			map.put("isWebSocket", isWebSocket);
			return map;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return map;
		}finally{
			thisLock.readLock().unlock();
		}
	}
	
	public List<CenterHandle> getChList() {
		thisLock.readLock().lock();
		try {
			return chList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			thisLock.readLock().unlock();
		}
	}
	public void setChList(List<CenterHandle> chList) {
		thisLock.writeLock().lock();
		try{
			this.chList = chList;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			thisLock.writeLock().unlock();
		}
	}
	public String[] getServerLock() {
		return serverLock;
	}
	public void setServerLock(String[] serverLock) {
		this.serverLock = serverLock;
	}
	
	public boolean hasLock(){
		boolean b = false;
		if(serverLock!=null && serverLock.length > 0){
			for(String s : serverLock){
				if(s != null && !s.trim().equals("")){
					b = true;
					break;
				}
			}
		}
		return b;
	}
	public boolean isHasFile() {
		return hasFile;
	}
	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}
	public boolean isFileIn() {
		return fileIn;
	}
	public void setFileIn(boolean fileIn) {
		this.fileIn = fileIn;
	}
	public String getInterfacetype() {
		return interfacetype;
	}
	public void setInterfacetype(String interfacetype) {
		this.interfacetype = interfacetype;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLockNames() {
		return lockNames;
	}

	public void setLockNames(String lockNames) {
		this.lockNames = lockNames;
	}

	public int getSecondnum() {
		return secondnum;
	}

	public void setSecondnum(int secondnum) {
		this.secondnum = secondnum;
	}

	public long getGeWait() {
		return geWait;
	}

	public void setGeWait(long geWait) {
		this.geWait = geWait;
	}

	public boolean isTrans() {
		return trans;
	}

	public void setTrans(boolean trans) {
		this.trans = trans;
	}

	public long getTranCommitMinute() {
		return tranCommitMinute;
	}

	public void setTranCommitMinute(long tranCommitMinute) {
		this.tranCommitMinute = tranCommitMinute;
	}

	public long getTranRollbackMinute() {
		return tranRollbackMinute;
	}

	public void setTranRollbackMinute(long tranRollbackMinute) {
		this.tranRollbackMinute = tranRollbackMinute;
	}

	public int getThreadPoolNumMax() {
		return threadPoolNumMax;
	}

	public void setThreadPoolNumMax(int threadPoolNumMax) {
		this.threadPoolNumMax = threadPoolNumMax;
	}

	public boolean isWebSocket() {
		return isWebSocket;
	}

	public void setWebSocket(boolean isWebSocket) {
		this.isWebSocket = isWebSocket;
	}
	
}
