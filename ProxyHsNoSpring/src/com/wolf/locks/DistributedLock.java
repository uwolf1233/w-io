package com.wolf.locks;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//分布式锁，从入口处抢占锁，最好设置开始时间和时间长度，避免出现释放不了锁的问题
public class DistributedLock {

	public static ConcurrentMap<String, DistributedLock> distributedLockMap = new ConcurrentHashMap<String, DistributedLock>();
	private Date startTime;
	private Date endTime;
	private int secondnum;
	private ReentrantLock lock = new ReentrantLock(false);
	private long geWait = 0;
	private BlockingQueue<String> lockQueue = new ArrayBlockingQueue<String>(2);//锁队列
	private int isGet = 0;
	private DistributedLock[] allDls = null;//放入所有锁
	
	public DistributedLock(int secondnum,long geWait){
		if(secondnum == 0 && geWait == 0){
			return;
		}
		this.secondnum = secondnum;
		this.geWait = geWait;
		refreshLock();
	}
	
	//获取锁
	public DistributedLock getLock(){
		try {
			if(geWait > 0){
				boolean b = lock.tryLock(geWait, TimeUnit.SECONDS);
				if(b){
					isGet = 1;
					refreshLock();
				}else{
					isGet = 0;
				}
			}else{
				lock.lock();
				isGet = 1;
				refreshLock();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isGet = 0;
		}finally{
			System.out.println("获取锁----"+isGet);
			return this;
		}
	}

	/**
	 * 将运行解锁队列（哪个线程执行的获取锁，必须由那个线程释放，其他线程不可释放本线程的锁）
	 */
	public void setReleaseQueue(){
		try {
			lockQueue.put("s");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 释放锁
	 */
	public void releaseLock(){
		try {
			lockQueue.take();//当解锁队列有数据，则进行解锁
			for(DistributedLock dls : allDls){
				if(dls.lock.isLocked()){
					System.out.println("解锁----");
					lock.unlock();
				}else{
					System.out.println("已被解锁---");
				}
			}
			allDls = null;
			startTime = null;
			endTime = null;
			isGet = 0;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getSecondnum() {
		return secondnum;
	}
	
	public DistributedLock refreshLock(){
		this.startTime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, secondnum);
		endTime = calendar.getTime();
		return this;
	}
	
	public void canclean(DistributedLock lock,String lockid){
		System.out.println("扫描进入解锁程序");
		if(isGet == 1 && allDls == null){//如果锁已经被获取但没有锁组，说明当前锁在其他锁的数组里面，这种情况由拥有数组的锁进行判断是否解锁
			System.out.println("没有锁组");
			return;
		}
		Date now = new Date();
		//System.out.println(distributedLockMap.toString());
		if(lock.getStartTime() != null && now.getTime() >= lock.getEndTime().getTime()){
			System.out.println("可以解锁");
			lock.setReleaseQueue();
			distributedLockMap.remove(lockid);
		}
	}

	public int getIsGet() {
		return isGet;
	}

	public DistributedLock[] getAllDls() {
		return allDls;
	}

	public void setAllDls(DistributedLock[] allDls) {
		this.allDls = allDls;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}
	
}
