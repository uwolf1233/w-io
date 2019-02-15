package com.wolf.javabean;

import com.wolf.server.LineTransServer;

import io.netty.channel.Channel;

public class WriteThread implements Runnable{

	public Object sendObj;
	public Channel channel;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		channel.writeAndFlush(sendObj);
	}

}
