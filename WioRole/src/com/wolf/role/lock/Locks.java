package com.wolf.role.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locks {

	public static ReentrantReadWriteLock rolewrLock = new ReentrantReadWriteLock(false);
	
}
