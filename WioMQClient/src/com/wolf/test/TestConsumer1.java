package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.Consumer;

public class TestConsumer1 {

	public static void main(String[] args) {
		
		new InitServers().init();
		Channel channel = TCPSendMessage.channel;
		
		try {
			Consumer consumer = new Consumer();
			consumer.setId("123457");
			consumer.setMessageListener(new ConsumerListener());
			consumer.setQueueName("queue1");
			consumer.setChannel(channel);
			consumer.putMg();
			consumer.regConsumer("456");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
