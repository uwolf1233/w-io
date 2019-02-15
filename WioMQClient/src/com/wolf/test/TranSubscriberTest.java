package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.Consumer;
import com.wolf.javabean.Subscriber;
import com.wolf.javabean.MQ.MQData;

public class TranSubscriberTest {

	public static void main(String[] args) {
		new InitServers().init();
		
		try {
			
			Channel channel = TCPSendMessage.channel;
			
			MQData.Builder builder0 = MQData.newBuilder();
			builder0.setType("createTopic");
			builder0.setQueuename("queue5");
			channel.writeAndFlush(builder0);
			
			Thread.sleep(2000);
			
			Subscriber subscriber = new Subscriber();
			subscriber.setId("123456");
			subscriber.setMessageListener(new ConsumerListener());
			subscriber.setQueueName("queue5");
			subscriber.setChannel(channel);
			subscriber.putMg();
			subscriber.regSubscriber("aaccbbdd");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
}
