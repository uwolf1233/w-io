package com.wolf.test;

import io.netty.channel.Channel;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.Service.UDPSendMessage;
import com.wolf.javabean.Publisher;
import com.wolf.javabean.MQ.MQData;

public class PublisherTest {

	public static void main(String[] args) {
		new InitServers().init();
		
		Channel channel = TCPSendMessage.channel;
//		
//		MQData.Builder builder0 = MQData.newBuilder();
//		builder0.setType("createTopic");
//		builder0.setQueuename("queue5");
//		channel.writeAndFlush(builder0);
		
		for(int i=0;i<1;i++){
			Publisher publisher = new Publisher();
			publisher.setName("def");
			publisher.setMessageListener(new ConsumerListener());
			publisher.setChannel(TCPSendMessage.channel);
			//publisher.setChannel(UDPSendMessage.channel);
			//publisher.udpSendMessage("2c3c4c","queue5");
			publisher.tcpSendMessage("2c3c4c", "queue5", null, null, null);
		}
	}
	
}
