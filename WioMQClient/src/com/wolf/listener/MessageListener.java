package com.wolf.listener;

public interface MessageListener {

	public void readMessage(Object[] os,String id) throws RuntimeException;
	
	public void reg(String type);
	
	public void channelIn(String type);
	
	public void unReg(String type);
	
	public void unchannel(String type);
	
	public void tcpSend(String type);
	
	public void udpSend(String type);
	
	public void readTranMessage(String id,String type,String transtatus,String transname) throws RuntimeException;
}
