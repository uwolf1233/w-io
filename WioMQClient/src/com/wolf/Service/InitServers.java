package com.wolf.Service;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.wolf.db.MQDBImpl;
import com.wolf.serconfig.Configs;

public class InitServers {

	public void init(){
		try {
			readConfig();
			new Thread(new ConsumerServer()).start();
			new Thread(new SubscriberServer()).start();
			new Thread(new TCPSendMessage()).start();
			new Thread(new UDPSendMessage()).start();
			Thread.sleep(2000);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readConfig() throws Throwable{
		InputStream in = InitServers.class.getResourceAsStream("/Config.xml");
		if(in!=null){
			SAXReader reader = new SAXReader();
			Document document = reader.read(in);
			Element configE = document.getRootElement();
			
			Element systemtypeE = configE.element("systemtype");
			String systemtype = systemtypeE.getText();
			
			Element consumeripE = configE.element("consumerip");
			ConsumerServer.ip = consumeripE.getText();
			Element consumerportE = configE.element("consumerport");
			ConsumerServer.port = Integer.parseInt(consumerportE.getText());
			Element consumergroupNumE = configE.element("consumergroupNum");
			ConsumerServer.groupNum = Integer.parseInt(consumergroupNumE.getText());
			ConsumerServer.systemtype = systemtype;
			
			Element subscriberipE = configE.element("subscriberip");
			SubscriberServer.ip = subscriberipE.getText();
			Element subscriberportE = configE.element("subscriberport");
			SubscriberServer.port = Integer.parseInt(subscriberportE.getText());
			Element subscribergroupNumE = configE.element("subscribergroupNum");
			SubscriberServer.groupNum = Integer.parseInt(subscribergroupNumE.getText());
			SubscriberServer.systemtype = systemtype;
			
			Element tcpidE = configE.element("tcpid");
			TCPSendMessage.ip = tcpidE.getText();
			Element tcpportE = configE.element("tcpport");
			TCPSendMessage.port = Integer.parseInt(tcpportE.getText());
			Element tcpgroupNumE = configE.element("tcpgroupNum");
			TCPSendMessage.groupNum = Integer.parseInt(tcpgroupNumE.getText());
			TCPSendMessage.systemtype = systemtype;
			
			Element udpidE = configE.element("udpid");
			UDPSendMessage.ip = udpidE.getText();
			Element udpportE = configE.element("udpport");
			UDPSendMessage.port = Integer.parseInt(udpportE.getText());
			Element udpgroupNumE = configE.element("udpgroupNum");
			UDPSendMessage.groupNum = Integer.parseInt(udpgroupNumE.getText());
			UDPSendMessage.systemtype = systemtype;
			
			String isDB = configE.element("isdb")+"";
			MQDBImpl.INSTANCE.setIsDB(Boolean.parseBoolean(isDB));
			if(MQDBImpl.INSTANCE.getIsDB()){
				Element dburlE = configE.element("dburl");
				Configs.url = dburlE.getText();
				Element dbusernameE = configE.element("dbusername");
				Configs.username = dbusernameE.getText();
				Element dbuserpassE = configE.element("dbuserpass");
				Configs.password = dbuserpassE.getText();
				Element cachePrepStmtsE = configE.element("cachePrepStmts");
				Configs.cachePrepStmts = cachePrepStmtsE.getText();
				Element prepStmtCacheSizeE = configE.element("prepStmtCacheSize");
				Configs.prepStmtCacheSize = prepStmtCacheSizeE.getText();
				Element prepStmtCacheSqlLimitE = configE.element("prepStmtCacheSqlLimit");
				Configs.prepStmtCacheSqlLimit = prepStmtCacheSqlLimitE.getText();
				Configs.dbconfig();
			}
		}
	}
	
}
