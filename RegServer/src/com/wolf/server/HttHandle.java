package com.wolf.server;

import java.sql.SQLException;
import java.util.List;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SystemNet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class HttHandle extends ChannelInboundHandlerAdapter {

	//private SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;
		if(datas.getHandletype().equals("httpSend")){
			OtherServer.pool.execute(new MyThread(datas,ctx));
		}else{
			ctx.fireChannelRead(msg);
		}
	}

	class MyThread implements Runnable{

		private SystemNet.Datas datas;
		private ChannelHandlerContext ctx;
		public MyThread(SystemNet.Datas datas,ChannelHandlerContext ctx){
			this.datas = datas;
			this.ctx = ctx;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean flag = true;
			while(flag){
				ReqResBean rrb = null;
				try {
					if(!datas.getHttpBean().getUrl().trim().equals("")){
						SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
						datasBuilder.getHttpBeanBuilder().setId(datas.getHttpBean().getId());//获取发送过来的数据
						rrb = RefController.INSTANCE.invokeMethod(datas.getHttpBean(),datasBuilder.getHttpBeanBuilder(),datas.getLineLog());//执行对应方法
						datasBuilder.setHandletype("HttpServletResponse");
						datasBuilder.getHttpBeanBuilder().setUrl(datas.getHttpBean().getUrl());
						writeHttp(ctx.channel(), datasBuilder);//发送返回的数据到前端,方法return基本都是html
						if(rrb != null && rrb.isTran()){
							boolean b = rrb.transEnd();//事务等待结束(补偿程序设计未完成)
							if(!b){
								System.out.println("进入补偿程序");
							}
						}
					}
					flag = false;
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(rrb!=null){
						rrb.setResponseData("error");
					}
					flag = false;
				}finally{
					flag = false;
					ReferenceCountUtil.release(datas);
					try {
						if(rrb != null && rrb.isTran() && rrb.getCon() != null && !rrb.getCon().isClosed()){
							rrb.getCon().close();
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		private boolean writeHttp(Channel channel,SystemNet.Datas.Builder datasBuilder){
			if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
				channel.writeAndFlush(datasBuilder);
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
						channel.writeAndFlush(datasBuilder);
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
	
}
