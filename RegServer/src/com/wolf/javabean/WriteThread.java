package com.wolf.javabean;

import com.wolf.server.OtherServer;

import io.netty.channel.Channel;

public class WriteThread implements Runnable{

	public Object sendObj;
	public Channel channel;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		write(channel, sendObj);
	}
	
	private boolean write(Channel channel,Object sendObj){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(sendObj);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == OtherServer.timeOut*1000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(sendObj);
					return true;
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}

}
