package com.wolf.spring;

import com.wolf.server.HttpServer;

public class StartInit {

	public static void main(String[] args) {
		try {
			HttpServer.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
