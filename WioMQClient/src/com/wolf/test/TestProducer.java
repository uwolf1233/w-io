package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.Service.UDPSendMessage;
import com.wolf.javabean.Producer;
import com.wolf.javabean.MQ.MQData;

public class TestProducer {

	public static void main(String[] args) {
		new InitServers().init();
		Channel channel = TCPSendMessage.channel;
		//Channel channel = UDPSendMessage.channel;
		
//		MQData.Builder builder0 = MQData.newBuilder();
//		builder0.setType("createQueue");
//		builder0.setQueuename("queue1");
//		channel.writeAndFlush(builder0);
		
		for(int i=0;i<2;i++){
			Producer producer = new Producer();
			producer.setName("abc");
			producer.setId("123123");
			producer.setChannel(channel);
			producer.setMessageListener(new ConsumerListener());
			producer.tcpSendMessage("1b2b3b","queue1",null,null,null);
			//producer.udpSendMessage("1b2b3b","queue1");
		}
	}
	
}
