package com.wolf.javabean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MQThreadFactory {

	public static ExecutorService es = Executors.newFixedThreadPool(10);
	
}
