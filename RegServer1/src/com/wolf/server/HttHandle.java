package com.wolf.server;

import java.util.List;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SystemNet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HttHandle extends ChannelInboundHandlerAdapter {

	//private SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		long starttime = System.currentTimeMillis();
		SystemNet.Datas datas = (SystemNet.Datas)msg;
		if(datas.getHandletype().equals("httpSend")){
			OtherServer.pool.execute(new MyThread(datas,ctx));
		}else{
			ctx.fireChannelRead(msg);
		}
		long endtime = System.currentTimeMillis();
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
				try {
					if(!datas.getHttpBean().getUrl().trim().equals("")){
						SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
						datasBuilder.getHttpBeanBuilder().setId(datas.getHttpBean().getId());//获取发送过来的数据
						ReqResBean rrb = RefController.INSTANCE.invokeMethod(datas.getHttpBean(),datasBuilder.getHttpBeanBuilder(),datas.getLineLog());//执行对应方法
						try {
							datasBuilder.setHandletype("HttpServletResponse");
							datasBuilder.getHttpBeanBuilder().setUrl(datas.getHttpBean().getUrl());
							ctx.channel().writeAndFlush(datasBuilder);//发送返回的数据到前端,方法return基本都是html
							if(rrb != null && rrb.isTran()){
								boolean b = rrb.transEnd();//事务等待结束(补偿程序设计未完成)
								if(!b){
									System.out.println("进入补偿程序");
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							rrb.setResponseData("error");
						}finally{
							if(rrb != null && rrb.isTran() && rrb.getCon() != null && !rrb.getCon().isClosed()){
								rrb.getCon().close();
							}
						}
					}
					flag = false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					flag = false;
				}finally{
					flag = false;
				}
			}
		}
		
	}
	
}
