package com.wolf.center;

import com.wolf.cache.PathDataCache;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.PathDataCachepro;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PathDataCacheHandle extends ChannelInboundHandlerAdapter{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;//接收到的proto
		if(datas.getHandletype().equals("PathDataCache")){
			PathDataCachepro pathDataCachepro = datas.getPathDataCachepro();
			String message = pathDataCachepro.getMessage();
			String path = pathDataCachepro.getPath();
			if(message == null || message.trim().equals("") || path == null || path.trim().equals("")){//没有相关数据不允许缓存
				System.out.println("cache error");
				return;
			}
			PathDataCache.putCache(message, path);
		}else{
			ctx.fireChannelRead(datas);
		}
	}

	
	
}
