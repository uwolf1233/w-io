package com.wolf.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AllLock {

	//public static ReentrantReadWriteLock sessionGetLock = new ReentrantReadWriteLock(false);//session获取锁
	
	//public static ReentrantReadWriteLock[] sessionmgLock = null;//session组锁
	
	public static ReentrantReadWriteLock sessionPathLock = new ReentrantReadWriteLock(false);
	
}
