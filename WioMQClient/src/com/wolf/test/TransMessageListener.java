package com.wolf.test;

import com.wolf.listener.MessageListener;

public class TransMessageListener implements MessageListener {

	@Override
	public void readMessage(Object[] os, String id) throws RuntimeException {
		// TODO Auto-generated method stub
		System.out.println("id:"+id+"返回"+os[0].toString());
	}

	@Override
	public void reg(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelIn(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unReg(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unchannel(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tcpSend(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void udpSend(String type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readTranMessage(String id, String type, String transtatus,
			String transname) throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

}
