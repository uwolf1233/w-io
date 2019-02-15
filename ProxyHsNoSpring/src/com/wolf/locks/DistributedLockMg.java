package com.wolf.locks;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.wolf.center.CenterServer;
import com.wolf.javabean.ServerBean;

//锁管理器
public class DistributedLockMg {

	public static DistributedLockMg INSTANCE = new DistributedLockMg();
	
	private long scanDistributedSECONDS = 15;
	
	//注册后添加锁对象，虽然支持锁组，但目前最好不要用
	public void addLock(List<String> lockNames,String path,int secondnum,long geWait,ServerBean serverBean){
		if(lockNames == null || lockNames.size() == 0 || 
				(lockNames.size() == 1 && lockNames.get(0).trim().equals(""))){
			return;
		}
		serverBean.setServerLock(lockNames.toArray(new String[lockNames.size()]));
		//CenterServer.serverLockMap.put(path, lockNames.toArray(new String[lockNames.size()]));
		for(String s : lockNames){
			DistributedLock lock = new DistributedLock(secondnum,geWait);
			CenterServer.locObjkMap.put(s, lock);
		}
	}
	
	/**返回锁组
	 * 
	 * @param path 请求路径
	 * @return 锁组
	 */
	public DistributedLock hasAndGet(String path){
		//String[] serverLockArray = CenterServer.serverLockMap.get(path);//先获取当前路径包含的所有锁
		String[] serverLockArray = CenterServer.serverMap.get(path).getServerLock();
		DistributedLock[] dls = new DistributedLock[serverLockArray.length];
		DistributedLock curDls = null;//当前需要返回的锁对象
		int i = 0;
		boolean canget = true; //是否可以获取
		for(String s : serverLockArray){
			DistributedLock dl = CenterServer.locObjkMap.get(s);
			dl.getLock();
			if(i == 0){
				dls[0] = dl;//第一个锁放入锁组
				dl.setAllDls(dls);//将锁组放入第一个锁对象
				curDls = dl;//需要用第一个锁对象作为返回值
			}else{
				dls[i] = dl;//将其他锁放入锁组
			}
			if(dl.getIsGet() == 0){
				canget = false;
				break;
			}
			i++;
		}
		return canget ? curDls.refreshLock() : null;//如果所有锁能获取，则返回锁对象，否则返回null
	}
	
	//锁扫描
	public void scanDistributedLock(){
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);  
	      
	    service.scheduleAtFixedRate(new Runnable() {  
	          
	        @Override  
	        public void run() {  
	        	//System.out.println("锁扫描");
	        	//SystemLocks.distributedLockLock.writeLock().lock();
	        	try {
					for(String key : DistributedLock.distributedLockMap.keySet()){
						DistributedLock lock = DistributedLock.distributedLockMap.get(key);
						lock.canclean(lock,key);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					//SystemLocks.distributedLockLock.writeLock().unlock();
				}
	              
	        }  
	    }, 1, scanDistributedSECONDS,TimeUnit.SECONDS);  
	}
	
}
