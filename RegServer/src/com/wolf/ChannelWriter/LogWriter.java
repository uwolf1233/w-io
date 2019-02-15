package com.wolf.ChannelWriter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.wolf.javabean.LogBean.LogDatas;

import io.netty.channel.Channel;

public class LogWriter implements Runnable{

	private Channel channel;
	public static LogWriter logWriter = new LogWriter();
	private LinkedBlockingQueue<LogDatas.Builder> queue = new LinkedBlockingQueue<LogDatas.Builder>(300);
	
	public LogWriter setLogWriter(Channel channel){
		this.channel = channel;
		return this;
	}
	
	public boolean write(LogDatas.Builder logDatasBuilder){
		if(channel!=null && channel.isActive() && channel.isOpen()){
			return queue.offer(logDatasBuilder);
		}
		return false;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			if(!channel.isActive() || !channel.isOpen()){
				continue;
			}
			if(channel.isWritable()){
				LogDatas.Builder logDatasBuilder = null;
				try {
					logDatasBuilder = queue.poll(30,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(logDatasBuilder == null){
					continue;
				}
				channel.writeAndFlush(logDatasBuilder);
			}
		}
	}

	public Channel getChannel() {
		return channel;
	}
	
}
