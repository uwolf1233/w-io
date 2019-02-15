package com.wolf.cache;

import io.netty.channel.Channel;

import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.Datas;
import com.wolf.javabean.SystemNet.PathDataCachepro;
import com.wolf.server.OtherServer;

public class SendToProxyCache {
	
	//缓存主推
	public static void send(String message,String path){
		Datas.Builder datasBuilder = Datas.newBuilder();
		datasBuilder.setHandletype("PathDataCache");
		PathDataCachepro.Builder pathDataCachepro = datasBuilder.getPathDataCacheproBuilder();
		pathDataCachepro.setMessage(message);
		pathDataCachepro.setPath(path);
		writeAndFlush(OtherServer.regChannel, datasBuilder);
	}
	
	private static boolean writeAndFlush(Channel channel,SystemNet.Datas.Builder datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
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
					channel.writeAndFlush(datas);
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
