package com.wolf.dept.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locks {

	public static ReentrantReadWriteLock deptwrLock = new ReentrantReadWriteLock(false);
	
}
