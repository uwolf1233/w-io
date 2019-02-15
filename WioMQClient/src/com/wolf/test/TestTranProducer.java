package com.wolf.test;

import java.util.UUID;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.Producer;

public class TestTranProducer {

	public static void main(String[] args) {
		new InitServers().init();
//		for(int i=0;i<4;i++){
//			Producer producer = new Producer();
//			producer.setName("abc");
//			producer.setMessageListener(new ConsumerListener());
//			producer.tcpSendMessage("abasb123", "queue1", "667788", "send", "t123123");
//			producer.sendTran("t123123", "commit");
//		}
		Producer producer = new Producer();
		producer.setName("abc");
		producer.setChannel(TCPSendMessage.channel);
		producer.setMessageListener(new ConsumerListener());
		String transname = UUID.randomUUID().toString().replace("-", "");
		producer.tcpSendMessage("abasb123", "queue1", "667788", "send", transname);
		producer.tcpSendMessage("abasb789", "queue1", "667789", "send", transname);
		producer.sendTran(transname, "commit");
		
//		for(int i=0;i<10;i++){
//			Producer producer = new Producer();
//			producer.setName("abc");
//			producer.setChannel(TCPSendMessage.channel);
//			producer.setMessageListener(new ConsumerListener());
//			producer.tcpSendMessage("abasb123", "queue1", null, null, null);
//			producer.tcpSendMessage("abasb789", "queue1", null, null, null);
//			producer.sendTran("t123123", "commit");
//		}
		
	}
	
}
