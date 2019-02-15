package com.wolf.cache;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//路径缓存
public class PathDataCache {

	private static ConcurrentMap<String,ByteBuf> bytebufCache = new ConcurrentHashMap<String, ByteBuf>();
	
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);
	
	public static ByteBuf getBytebufCache(String path){
		boolean islock;
		try {
			islock = lock.readLock().tryLock(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			if(!bytebufCache.containsKey(path)){
				return null;
			}
			ByteBuf bf = bytebufCache.get(path).retain();
			return bf.duplicate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(islock){
				lock.readLock().unlock();
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public static void putCache(String message,String path){
		boolean islock;
		try {
			islock = lock.writeLock().tryLock(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			if(bytebufCache.containsKey(path)){
				ByteBuf bf = bytebufCache.get(path);
				int refCnt = bf.refCnt();
				if(refCnt > 0){
					bf.release(refCnt);
				}
			}
			byte[] bytes = message.getBytes(Charset.forName("utf-8"));
			bytebufCache.put(path, Unpooled.directBuffer().writeBytes(bytes));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(islock){
				lock.writeLock().unlock();
			}
		}
	}
//	
//	public static void putCache(ByteBuf bytebuf,String path){
//		bytebufCache.put(path, bytebuf);
//	}
	
}
