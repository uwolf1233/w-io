package com.wolf.cache;

import io.netty.util.internal.PlatformDependent;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CacheSee implements Runnable{

	private static final int _1K = 1024;
	private static final String BUSINESS_KEY = "netty_direct_memory";
	
	private AtomicLong directMemory;
	
	public void init(){
		//PlatformDependent
		try {
			Field field = PlatformDependent.class.getDeclaredField("DIRECT_MEMORY_COUNTER");
			field.setAccessible(true);
			directMemory = (AtomicLong)field.get(PlatformDependent.class);
			int memoryInKb = (int)(directMemory.get());
			System.out.println(memoryInKb+" bit");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public static void starts() {
		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(new CacheSee(), 1000, 3000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		init();
	}
	
}
