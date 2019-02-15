package com.wolf.server;

import com.wolf.javabean.SystemNet;
import com.wolf.javabean.SystemNet.SystemNetBean;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class OtherHandle extends ChannelInboundHandlerAdapter {
	
	private SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// TODO Auto-generated method stub
		try {
			if(msg instanceof SystemNet.Datas){
				SystemNet.Datas datas = (SystemNet.Datas)msg;
				if(datas.getHandletype().equals("RegBean")){
					if(!datas.getSystemNetBean().getMessage().trim().equals("")){
						String message = datas.getSystemNetBean().getMessage();
						if(message.equals("unReg success")){
							ctx.channel().close();
						}else{
							System.out.println(message);
						}
					}
				}else{
					ctx.fireChannelRead(msg);
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
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
	
}
