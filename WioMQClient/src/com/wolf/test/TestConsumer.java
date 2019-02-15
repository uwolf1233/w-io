package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.Consumer;
import com.wolf.javabean.MQ.MQData;

public class TestConsumer {

	public static void main(String[] args) {
		
		new InitServers().init();
		
		try {
			
			Channel channel = TCPSendMessage.channel;
			
			MQData.Builder builder0 = MQData.newBuilder();
			builder0.setType("createQueue");
			builder0.setQueuename("queue1");
			channel.writeAndFlush(builder0);
			
			Thread.sleep(2000);
			
			Consumer consumer = new Consumer();
			consumer.setId("123456");
			consumer.setMessageListener(new ConsumerListener());
			consumer.setQueueName("queue1");
			consumer.setChannel(channel);
			consumer.putMg();
			consumer.regConsumer("123");
			//consumer.consumerUnchannel();
//			Thread.sleep(5000);
//			consumer.channelInConsumer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
