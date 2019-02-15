package com.wolf.javabean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WioThreadPool {

	public static ExecutorService pool = Executors.newFixedThreadPool(10);
	
}
