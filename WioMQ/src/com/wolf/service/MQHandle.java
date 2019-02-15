package com.wolf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.protobuf.ByteString;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.MQ.TranMessages;
import com.wolf.javabean.TranMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MQHandle extends ChannelInboundHandlerAdapter{

//	private String id;
//	private String type;
//	private String queueName;
//	private List<String[]> datas = new ArrayList<String[]>();
	private ConcurrentMap<String,String[]> datas = new ConcurrentHashMap<String, String[]>();
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if(datas.size()>0){
			for(String key : datas.keySet()){
				String s[] = datas.get(key);
				String type = s[0];
				String queueName = s[1];
				if(type == null){
					continue;
				}
				if(type.equals("Consumer")){
					AllInit.INSTANCE.channelUnConsumer(key,queueName);
				}else if(type.equals("Subscriber")){
					AllInit.INSTANCE.channelUnSubscriber(key,queueName);
				}
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if(msg instanceof MQData){
				MQData mqdata = (MQData)msg;
				String type = mqdata.getType();
				Channel channel = ctx.channel();
				if(type.equals("createQueue")){
					if(mqdata.getQueuename() == null || mqdata.getQueuename().equals("")){
						return;
					}
					AllInit.INSTANCE.createQueue(mqdata.getQueuename());
					return;
				}
				if(type.equals("createTopic")){
					if(mqdata.getQueuename() == null || mqdata.getQueuename().equals("")){
						return;
					}
					AllInit.INSTANCE.createTopic(mqdata.getQueuename());
					return;
				}
				if(type.equals("regConsumer")){
					String s[] = new String[2];
					String id = mqdata.getConsumer().getId();
					String ctype = "Consumer";
					String queueName = mqdata.getQueuename();
					s[0] = ctype;
					s[1] = queueName;
					int i = AllInit.INSTANCE.regConsumer(channel, id, mqdata.getConsumer().getName(),queueName);
					if(i == 0){
						datas.put(id, s);
					}
					return;
				}else if(type.equals("regSubscriber")){
					String id = mqdata.getSubscriber().getId();
					String queueName = mqdata.getQueuename();
					String ctype = "Subscriber";
					String s[] = new String[2];
					s[0] = ctype;
					s[1] = queueName;
					int i = AllInit.INSTANCE.regSubscriber(channel, id, mqdata.getSubscriber().getName(),queueName);
					datas.put(id, s);
					return;
				}else if(type.equals("ChannelInConsumer")){
					String id = mqdata.getConsumer().getId();
					String queueName = datas.get(id)[1];
					AllInit.INSTANCE.ChannelInConsumer(channel, id,queueName);
					return;
				}else if(type.equals("ChannelInSubscriber")){
					String id = mqdata.getSubscriber().getId();
					String queueName = datas.get(id)[1];
					AllInit.INSTANCE.ChannelInSubscriber(channel, id, queueName);
					return;
				}else if(type.equals("unRegConsumer")){
					String[] s = datas.get(mqdata.getConsumer().getId());
					String queueName = s[1];
					System.out.println("脱离的queueName="+queueName);
					AllInit.INSTANCE.unRegConsumer(mqdata.getConsumer().getId(),queueName);
					datas.remove(mqdata.getConsumer().getId());
					return;
				}else if(type.equals("unRegSubscriber")){
					String[] s = datas.get(mqdata.getSubscriber().getId());
					String queueName = s[1];
					System.out.println("脱离的queueName="+queueName);
					AllInit.INSTANCE.unRegSubscriber(mqdata.getSubscriber().getId(),queueName);
					datas.remove(mqdata.getSubscriber().getId());
					return;
				}else if(type.equals("sendQueueMessage")){
					String id = mqdata.getProducer().getId();
					String queueName = mqdata.getQueuename();
					if(mqdata.getDataCount() > 0){
						List<ByteString> bsList = mqdata.getDataList();
						AllInit.INSTANCE.sendQueueMessage(id, mqdata.getProducer().getName(),bsList.get(0),queueName);
					}
					int tranCount = mqdata.getTranMessagesCount();
					if(tranCount>0){
						List<TranMessages> tmList = mqdata.getTranMessagesList();
						for(int i=0;i<tranCount;i++){
							TranMessages tranMessages = tmList.get(i);
							String transtatus = tranMessages.getTranstatus();
							String transName = tranMessages.getTransname();
							if(transtatus.equals("send")){
								AllInit.INSTANCE.sendQueueTranMessage(id, tranMessages.getId(), mqdata.getProducer().getName(), 
									tranMessages.getData(), queueName, transName, channel);//接收事务消息
							}else if(transtatus.equals("commit")){
								AllInit.INSTANCE.commitQueueTranMessage(transName, queueName);//提交
							}else if(transtatus.equals("rollback")){
								AllInit.INSTANCE.rollbackQueueTranMessage(transName, queueName);//回滚
							}
						}
					}
					return;
				}else if(type.equals("sendTopicMessage")){
					String id = mqdata.getPublisher().getId();
					String queueName = mqdata.getQueuename();
					if(mqdata.getDataCount() > 0){
						List<ByteString> bsList = mqdata.getDataList();
						AllInit.INSTANCE.sendTopicMessage(id, mqdata.getPublisher().getName(), bsList.get(0),queueName);
					}
					int tranCount = mqdata.getTranMessagesCount();
					if(tranCount>0){
						List<TranMessages> tmList = mqdata.getTranMessagesList();
						for(int i=0;i<tranCount;i++){
							TranMessages tranMessages = tmList.get(i);
							String transtatus = tranMessages.getTranstatus();
							String transName = tranMessages.getTransname();
							if(transtatus.equals("send")){
								AllInit.INSTANCE.sendTopicTranMessage(id, tranMessages.getId(), mqdata.getPublisher().getName(), 
										tranMessages.getData(), queueName, transName, channel);//接收事务消息
							}else if(transtatus.equals("commit")){
								AllInit.INSTANCE.commitTopicTranMessage(transName, queueName);//提交
							}else if(transtatus.equals("rollback")){
								AllInit.INSTANCE.rollbackTopicTranMessage(transName, queueName);//回滚
							}
						}
					}
					return;
				}else if(type.equals("consumerUnchannel")){
					AllInit.INSTANCE.channelUnConsumer(mqdata.getConsumer().getId(),mqdata.getQueuename());
					return;
				}else if(type.equals("subscriberUnchannel")){
					AllInit.INSTANCE.channelUnSubscriber(mqdata.getSubscriber().getId(),mqdata.getQueuename());
					return;
				}else if(type.equals("lineProducer")){
					AllInit.INSTANCE.queueTranLine(channel, mqdata.getProducer().getId());
					return;
				}else if(type.equals("linePublisher")){
					AllInit.INSTANCE.topicTranLine(channel, mqdata.getPublisher().getId());
					return;
				} 
			}
		} catch (Throwable e) {
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
