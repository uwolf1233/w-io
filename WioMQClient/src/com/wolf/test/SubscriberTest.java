package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.Subscriber;

public class SubscriberTest {

	public static void main(String[] args) {
		try {
			new InitServers().init();
			
			Channel channel = TCPSendMessage.channel;
			
			MQData.Builder builder0 = MQData.newBuilder();
			builder0.setType("createTopic");
			builder0.setQueuename("queue5");
			channel.writeAndFlush(builder0);
			
			Subscriber subscriber = new Subscriber();
			subscriber.setId("565656");
			subscriber.setMessageListener(new ConsumerListener());
			subscriber.setQueueName("queue5");
			subscriber.setChannel(channel);
			subscriber.regSubscriber("abc");
			subscriber.putMg();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
