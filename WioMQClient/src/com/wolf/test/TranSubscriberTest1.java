package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.Consumer;
import com.wolf.javabean.Subscriber;
import com.wolf.javabean.MQ.MQData;

public class TranSubscriberTest1 {

	public static void main(String[] args) {
		new InitServers().init();
		
		try {
			
			Channel channel = TCPSendMessage.channel;
			
			Subscriber subscriber = new Subscriber();
			subscriber.setId("32323232");
			subscriber.setMessageListener(new ConsumerListener());
			subscriber.setQueueName("queue5");
			subscriber.setChannel(channel);
			subscriber.putMg();
			subscriber.regSubscriber("eeffeeff");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
}
