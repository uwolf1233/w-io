package com.wolf.role.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AllLocks {

	public static ReentrantReadWriteLock usersRwLock = new ReentrantReadWriteLock(false);
	public static ReentrantReadWriteLock rolesRwLock = new ReentrantReadWriteLock(false);
	public static ReentrantReadWriteLock permisRwLock = new ReentrantReadWriteLock(false);
	public static ReentrantReadWriteLock datasRwLock = new ReentrantReadWriteLock(false);
	
}
