package com.wolf.Service;

import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Consumer;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.MQ.TranMessages;
import com.wolf.javabean.Producer;
import com.wolf.javabean.Publisher;
import com.wolf.javabean.Subscriber;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ReturnMessageHandle extends ChannelInboundHandlerAdapter{

	private static MessageI consumerI = new MessageImpl();
	public String id = "";
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		id = UUID.randomUUID().toString().replace("-", "");
		System.out.println(id+"连接");
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(id+"断开连接");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if(msg instanceof MQData){
				MQData mqdata = (MQData)msg;
				if(mqdata.getType().equals("returnQueueMessage") || mqdata.getType().equals("returnTopicMessage") 
						|| mqdata.getType().equals("tranQueueMessage") || mqdata.getType().equals("tranTopicMessage")
						|| mqdata.getType().equals("returnProducer") || mqdata.getType().equals("returnPublisher")){
					int size = mqdata.getDataCount();
					Object[] os = new Object[size];
					for(int i=0;i<size;i++){
						ByteString bs = mqdata.getData(i);
						byte[] bytes = bs.toByteArray();
						os[i] = consumerI.getDatas(bytes);
					}
					String id = "";
					if(mqdata.getType().equals("returnQueueMessage")){
						id = mqdata.getConsumer().getId();
						if(Consumer.consumerMap.containsKey(id)){
							Consumer.consumerMap.get(id).returnQueueMessage(os);
						}
					}else if(mqdata.getType().equals("returnTopicMessage")){
						id = mqdata.getSubscriber().getId();
						if(Subscriber.subscriberMap.containsKey(id)){
							Subscriber.subscriberMap.get(id).returnTopicMessage(os);
						}
					}else if(mqdata.getType().equals("tranQueueMessage")){
//						System.out.println("模拟掉线开始");
//						ctx.channel().close();//呀，掉线了
//						System.out.println("模拟掉线结束");
						TranMessages tm = mqdata.getTranMessages(0);
						String tname = tm.getTransname();
						Producer.putTranstatusMap(tname, tm.getTranstatus());
					}else if(mqdata.getType().equals("tranTopicMessage")){
//						System.out.println("模拟掉线开始");
//						ctx.channel().close();//呀，掉线了
//						System.out.println("模拟掉线结束");
						TranMessages tm = mqdata.getTranMessages(0);
						String tname = tm.getTransname();
						Publisher.putTranstatusMap(tname, tm.getTranstatus());
					}else if(mqdata.getType().equals("returnPublisher")){
						TranMessages tm = mqdata.getTranMessages(0);
						String transtatus = tm.getTranstatus();
						if(transtatus.equals("waitstatus")){
							Publisher.getTransStatus(tm.getTransname());
						}
					}else if(mqdata.getType().equals("returnProducer")){
						TranMessages tm = mqdata.getTranMessages(0);
						String transtatus = tm.getTranstatus();
						if(transtatus.equals("waitstatus")){
							Producer.getTransStatus(tm.getTransname());
						}
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
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}

	
	
}
