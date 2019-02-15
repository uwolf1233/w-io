package com.wolf.permi.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locks {

	public static ReentrantReadWriteLock permiwrLock = new ReentrantReadWriteLock(false);
	
}
