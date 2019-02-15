package com.wolf.test;

import com.wolf.Service.InitServers;
import com.wolf.javabean.Subscriber;

public class SubscriberTest1 {

	public static void main(String[] args) {
		try {
			new InitServers().init();
			Subscriber subscriber = new Subscriber();
			subscriber.setMessageListener(new ConsumerListener());
			subscriber.regSubscriber("abc");
			subscriber.putMg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
