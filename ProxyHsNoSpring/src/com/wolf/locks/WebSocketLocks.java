package com.wolf.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WebSocketLocks {

	private static ReentrantReadWriteLock sendToClientLock = new ReentrantReadWriteLock(false);
	public static ReentrantReadWriteLock webSocketBeanLock = new ReentrantReadWriteLock(false);
	
}
