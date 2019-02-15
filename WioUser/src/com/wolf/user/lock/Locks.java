package com.wolf.user.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locks {

	public static ReentrantReadWriteLock userwrLock = new ReentrantReadWriteLock(false);
	
}
