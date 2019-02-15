package com.wolf.server;

import com.wolf.javabean.TranNet;
import com.wolf.javabean.TranNet.Trans;
import com.wolf.javabean.TranNet.TransDatas;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

//事务handle，根据主控ID判断异步事务和同步事务,同步事务的前提是同步方法调用
//异步事务将不受调用者事务控制
//同步事务受调用者控制，由第一个有事务的调用者提供主控事务ID
//可以说，异步事务的事务也是异步的，各自提交自己的事务，但异步事务中包含同步事务，则由同步事务中的第一个事务生成主控ID
//目前采用两阶段提交技术，当然，可以自己去实现或者采用第三方事务控制(以下是目前控制流程)
//1.如果需要事务控制，则生成事务id到调度器，本身业务线程将进入阻塞状态
//2.调度器返回已收到信息，确认事务已由调度器控制
//3.各个服务器进行第一次事务确认，本身线程阻塞等待确认
//4.调度器收到消息确认事务，并返回确认信息
//5.各个服务器进行第二次事务确认，第二次事务确认需要所有同步事务必须全部确认才返回已确认，当有一个事务超时回滚或者被调用者回滚，则全部事务回滚
//6.当全部确认后，告知各个服务器提交各自的事务，各个服务器提交自己的事务后，发送信息到调度器做一次确认，完全确认后删除本次事务控制，如果未完全确认或者超过回滚时长未完全确认需要自己实现补偿程序
//7.最后未完全确认但遇到消息传输失败或者服务器端接收不到信息无法执行补偿程序,或者存在不可靠消息，这个时候最好做好日志(这是个变态拜占庭将军的问题，毕竟需要情况比较极端，后续版本再提供解决方案)
public class TransHandle extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		TransDatas tn = (TransDatas)msg;
		TransBeanMg.INSTANCE.getAllTransBean(tn,ctx);
		//super.channelRead(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	
	
}
