package com.wolf.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SystemLocks {

	public static ReentrantReadWriteLock regLock = new ReentrantReadWriteLock(false);//服务注册锁
	
	public static ReentrantReadWriteLock sessionGetLock = new ReentrantReadWriteLock(false);//session获取锁
	
	public static ReentrantReadWriteLock distributedLockLock = new ReentrantReadWriteLock(false);//锁获取锁（暂时不要）
	
}
