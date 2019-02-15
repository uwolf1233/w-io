package com.wolf.cache;

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
		OtherServer.regChannel.writeAndFlush(datasBuilder);
	}
	
}
