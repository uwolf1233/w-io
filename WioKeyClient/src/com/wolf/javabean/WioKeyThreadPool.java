package com.wolf.javabean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WioKeyThreadPool {

	public static ExecutorService pool = Executors.newFixedThreadPool(100);
	
}
