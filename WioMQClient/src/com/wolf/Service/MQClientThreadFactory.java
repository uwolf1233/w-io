package com.wolf.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MQClientThreadFactory {

	public static ExecutorService es = Executors.newFixedThreadPool(10);
	
}
