package com.wolf.listener;

import java.util.ArrayList;
import java.util.List;

import com.wolf.javabean.UsersBean;

public class UserListener implements MessageListener {

	public List<Object> oList = new ArrayList<Object>();
	public UsersBean usersBean = null;
	
	@Override
	public void channelIn(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readMessage(Object[] os, String id) throws RuntimeException {
		// TODO Auto-generated method stub
		System.out.println(id+"---"+os[0].toString());
		oList.add(os);
		if(usersBean.getStatus().equals("1")){
			usersBean.returnShow(oList);
		}
	}

	@Override
	public void reg(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tcpSend(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void udpSend(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unReg(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unchannel(String arg0) {
		// TODO Auto-generated method stub

	}

}
