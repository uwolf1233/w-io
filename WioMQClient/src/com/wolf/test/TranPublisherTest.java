package com.wolf.test;

import java.util.UUID;

import com.wolf.Service.InitServers;
import com.wolf.Service.TCPSendMessage;
import com.wolf.javabean.Producer;
import com.wolf.javabean.Publisher;

public class TranPublisherTest {

	public static void main(String[] args) {
		new InitServers().init();
		//new Thread(new TCPSendMessage()).start();
//		for(int i=0;i<1;i++){
//			Publisher publisher = new Publisher();
//			publisher.setName("def");
//			publisher.setMessageListener(new ConsumerListener());
//			//publisher.UDPSendMessage("2c3c4c");
//		}
		for(int i=0;i<1;i++){
			Publisher publisher = new Publisher();
			publisher.setId(UUID.randomUUID().toString().replace("-", ""));//事务消息必须手动设置id，避免断线
			publisher.setName("abc");
			publisher.setChannel(TCPSendMessage.channel);
			publisher.setMessageListener(new ConsumerListener());
			String transname = UUID.randomUUID().toString().replace("-", "");
			publisher.tcpSendMessage("abasb123", "queue5", "667788", "send", transname);
			publisher.tcpSendMessage("abasb789", "queue5", "667789", "send", transname);
			publisher.sendTran(transname, "commit");
		}
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for(int i=0;i<10;i++){
//			Publisher publisher = new Publisher();
//			publisher.setName("abc");
//			publisher.setChannel(TCPSendMessage.channel);
//			publisher.setMessageListener(new ConsumerListener());
//			publisher.tcpSendMessage("qqqqq111", "queue5", null, null, null);
//			publisher.tcpSendMessage("wwwww222", "queue5", null, null, null);
//		}
	}
	
}
