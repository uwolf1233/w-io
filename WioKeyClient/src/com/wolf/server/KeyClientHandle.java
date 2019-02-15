package com.wolf.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.javabean.Key.Datas;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class KeyClientHandle extends ChannelInboundHandlerAdapter{

	public static ConcurrentMap<String,BlockingQueue> retQueueMap = new ConcurrentHashMap<String,BlockingQueue>();
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if(msg instanceof Datas){
				Datas datas = (Datas)msg;
				String type = datas.getType();
				String id = datas.getId();
				if(!retQueueMap.containsKey(id)){
					System.out.println("加密解密失败");
					return;
				}
				if(type.equals("-1")){
					retQueueMap.remove(id);
					return;
				}else if(type.equals("2")){//字符串加密
					if(retQueueMap.containsKey(id)){
						BlockingQueue queue = retQueueMap.get(id);
						if(queue!=null){
							queue.put(datas.getEmcrypt().getRdata().toByteArray());//byte数组
							System.out.println("加密返回");
						}
						retQueueMap.remove(id);
					}
				}else if(type.equals("3")){//字符串解密
					if(retQueueMap.containsKey(id)){
						BlockingQueue queue = retQueueMap.get(id);
						if(queue!=null){
							queue.put(datas.getDmcrypt().getRdata());//解密后的字符串
							System.out.println("解密返回");
						}
						retQueueMap.remove(id);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	
	
}
