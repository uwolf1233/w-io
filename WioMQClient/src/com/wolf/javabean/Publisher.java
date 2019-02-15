package com.wolf.javabean;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.DatagramPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.protobuf.ByteString;
import com.wolf.Service.TCPSendMessage;
import com.wolf.Service.UDPSendMessage;
import com.wolf.dao.MQClientDao;
import com.wolf.db.MQDBImpl;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.MQ.TranMessages;
import com.wolf.listener.MessageListener;

public class Publisher {

	private String id;
	private String name;
	private String queueName;
	private MessageListener messageListener;
	private Channel channel;
	private String tranType;//事务状态
	
	//事务名:状态，sendsuccess,commit,rollback
	private static Map<String, Publisher> transtatusMap = new HashMap<String, Publisher>();
	private static ReentrantReadWriteLock tmrwLock = new ReentrantReadWriteLock(false);
	
	public static void allLineTran(Channel channel){//断线后重新连接需要执行的代码
		boolean flag = false;
		try {
			flag = tmrwLock.writeLock().tryLock(30000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("发送超时");
			return;
		}
		try{
			if(flag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					for(String key : transtatusMap.keySet()){
						Publisher publisher = transtatusMap.get(key);
						publisher.lineTran(channel);
					}
				}else{
					List<Object[]> oslist = MQClientDao.INSTANCE.getPublisherToLine();
					for(Object[] os : oslist){
						if(os!=null && os[0]!=null){
							lineTran(channel, os[0]+"");
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(flag){
				tmrwLock.writeLock().unlock();
			}
		}
	}
	
	public void lineTran(Channel channel){//断线后重新连接需要执行的代码
		this.channel = channel;
		if(channel != null && channel.isActive() && channel.isOpen()){
			MQData.Builder builder = MQData.newBuilder();
			builder.setType("linePublisher");
			builder.getPublisherBuilder().setId(id);
			channel.writeAndFlush(builder);
		}
	}
	
	public static void lineTran(Channel channel,String publisherId){
		if(channel != null && channel.isActive() && channel.isOpen()){
			MQData.Builder builder = MQData.newBuilder();
			builder.setType("linePublisher");
			builder.getPublisherBuilder().setId(publisherId);
			channel.writeAndFlush(builder);
		}
	}
	
	public static void getTransStatus(String transname){//MQ发现事务消息超时后反查事务状态
		boolean flag = false;
		try {
			flag = tmrwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("发送超时");
			return;
		}
		try{
			if(flag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					if(transtatusMap.containsKey(transname)){
						Publisher publisher = transtatusMap.get(transname);
						if(publisher.getTranType().equals("commit") || publisher.getTranType().equals("rollback")){
							getTransStatusSend(publisher.getId(), publisher.getName(), publisher.getQueueName(), 
									publisher.getTranType(), transname, TCPSendMessage.channel,"noDB");
						}else{
							return;
						}
					}
				}else{
					String dbtranType = MQClientDao.INSTANCE.getPublisherTranStatus(transname);
					if(dbtranType.equals("commit") || dbtranType.equals("rollback")){
						Object[] os = MQClientDao.INSTANCE.tranpublisherAndCur(new String[]{
								"publisherid","publishername","queuename","transname","transtatus"
						},transname);
						getTransStatusSend(os[0]+"", os[1]+"", os[2]+"", os[4]+"",os[3]+"", TCPSendMessage.channel,"isDB");
						MQClientDao.INSTANCE.updatePublisherStatus(transname);//目前恒认为服务端已接收到确认事务消息
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(flag){
				tmrwLock.writeLock().unlock();
			}
		}
	}
	
	private static void getTransStatusSend(String publisherId,String publisherName,String queueName,final String tranType,
			final String transname,Channel channel,final String type){
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("sendTopicMessage");
		builder.getPublisherBuilder().setId(publisherId);
		builder.getPublisherBuilder().setName(publisherName);
		builder.setQueuename(queueName);
		TranMessages.Builder tm = TranMessages.newBuilder();
		tm.setTranstatus(tranType);
		tm.setTransname(transname);
		builder.addTranMessages(tm);
		channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if(future.isSuccess()){
					System.out.println("本地确认消息已操作事务，正在将事务状态发送给MQ:"+tranType);
					if(type.equals("isDB")){
						MQClientDao.INSTANCE.updatePublisherStatus(transname);//目前恒认为服务端已接收到确认事务消息
					}else{
						transtatusMap.remove(transname);
					}
				}else{
					System.out.println("本地事务发送失败:"+tranType);
				}
			}
		});
	}
	
	public static void putTranstatusMap(String transname,String transtatus){//给returnMessageHandle提供
		boolean flag = false;
		try {
			flag = tmrwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("发送超时");
			return;
		}
		try{
			if(flag){
				if(transtatus.equals("sendqueuesuccess")){
					if(!MQDBImpl.INSTANCE.getIsDB()){
						if(transtatusMap.containsKey(transname)){
							Publisher publisher = transtatusMap.get(transname);
							if(publisher.getTranType().equals("send")){
								publisher.setTranType("sendqueuesuccess");
								transtatusMap.put(transname, publisher);
								System.out.println("本地确认消息已被服务端接收");
							}else if(publisher.getTranType().equals("commit") || publisher.getTranType().equals("rollback")){
								sendTranFromReturn(publisher.getId(), publisher.getName(), publisher.getQueueName(), 
										publisher.getTranType(), publisher.getChannel(), transname,"noDB");
							}
						}
//						else{
//							Publisher publisher = new Publisher();
//							publisher.setTranType("error");
//							transtatusMap.put(transname, publisher);//如果没有，就定义为错误
//						}
					}else{
						String dbtranType = MQClientDao.INSTANCE.getPublisherTranStatus(transname);
						if(dbtranType != null && !dbtranType.trim().equals("") && dbtranType.equals("send")){
							MQClientDao.INSTANCE.publisherCommitOrRollback(transtatus, transname);
							System.out.println("本地确认消息已被服务端接收");
						}else if(dbtranType != null && !dbtranType.trim().equals("") 
								&& (dbtranType.equals("commit") || dbtranType.equals("rollback"))){
							Object[] os = MQClientDao.INSTANCE.tranpublisherAndCur(new String[]{
									"publisherid","publishername","queuename","transtatus"
							},transname);
							sendTranFromReturn(os[0]+"", os[1]+"", os[2]+"", os[3]+"", TCPSendMessage.channel, transname,"isDB");
							MQClientDao.INSTANCE.updatePublisherStatus(transname);//目前恒认为服务端已接收到确认事务消息
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(flag){
				tmrwLock.writeLock().unlock();
			}
		}
	}
	
	private static void sendTranFromReturn(String publisherId,String publisherName,String queueName,final String tranType,
			Channel channel,final String transname,final String type){
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("sendTopicMessage");
		builder.getPublisherBuilder().setId(publisherId);
		builder.getPublisherBuilder().setName(publisherName);
		builder.setQueuename(queueName);
		TranMessages.Builder tm = TranMessages.newBuilder();
		tm.setTranstatus(tranType);
		tm.setTransname(transname);
		builder.addTranMessages(tm);
		channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if(future.isSuccess()){
					System.out.println("本地确认消息已被服务端接收并操作事务:"+tranType);
					if(type.equals("isDB")){
						MQClientDao.INSTANCE.updatePublisherStatus(transname);//目前恒认为服务端已接收到确认事务消息
					}else{
						transtatusMap.remove(transname);
					}
				}else{
					System.out.println("本地事务发送失败:"+tranType);
				}
			}
		});
	}
	
	/**
	 * 事务提交或者回滚
	 * @param transname
	 * @param transtatus commit rollback
	 * @return
	 */
	public boolean sendTran(String transname, String transtatus){
		if(id == null){
			System.out.println("id不能为空");
			return false;
		}
		if(name == null){
			System.out.println("name不能为空");
			return false;
		}
		if(queueName == null){
			System.out.println("queueName不能为空");
			return false;
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("sendTopicMessage");
		builder.getPublisherBuilder().setId(id);
		builder.getPublisherBuilder().setName(name);
		builder.setQueuename(queueName);
		TranMessages.Builder tm = TranMessages.newBuilder();
		tm.setTranstatus(transtatus);
		tm.setTransname(transname);
		builder.addTranMessages(tm);
		boolean flag = false;
		try {
			flag = tmrwLock.writeLock().tryLock(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("发送超时");
			return false;
		}
		try{
			if(flag){
				if(!MQDBImpl.INSTANCE.getIsDB()){
					if(transtatusMap.containsKey(transname) && transtatusMap.get(transname).getTranType().equals("sendqueuesuccess")){
						transtatusMap.put(transname, this);
						tranType = transtatus;
					}else if(transtatusMap.containsKey(transname) && transtatusMap.get(transname).getTranType().equals("send")){//如果还在发送，需要返回确认发送成功后自己操作
						transtatusMap.put(transname, this);
						tranType = transtatus;
						return true;//恒成功
					}else if(transtatusMap.containsKey(transname) && transtatusMap.get(transname).getTranType().equals("error")){
						throw new RuntimeException("本地事务异常，服务端已确认接收但本地对象不存在");
					}
					else{
						builder.clear();
						builder = null;
						return false;
					}
				}else{
					String dbtranType = MQClientDao.INSTANCE.getPublisherTranStatus(transname);
					if(dbtranType != null && !dbtranType.trim().equals("") && dbtranType.equals("sendqueuesuccess")){//服务端已经确认了
						MQClientDao.INSTANCE.publisherCommitOrRollback(transtatus, transname);//db操作后面考虑要不要加锁
						MQClientDao.INSTANCE.updatePublisherStatus(transname);//不管是否改过，都再次修改
					}else if(dbtranType != null && dbtranType.equals("send")){//如果还在发送，需要返回确认发送成功后自己操作
						MQClientDao.INSTANCE.publisherCommitOrRollback(transtatus, transname);
						MQClientDao.INSTANCE.updatePublisherStatus(transname);//不管是否改过，都再次修改
						return true;//恒成功
					}else if(dbtranType != null && dbtranType.equals("error")){
						throw new RuntimeException("本地事务异常，服务端已确认接收但本地对象不存在");
					}
					else{
						builder.clear();
						builder = null;
						return false;
					}
				}
				if(channel == null || !channel.isOpen() || !channel.isActive()){
					builder.clear();
					builder = null;
					System.out.println("channel失效");
					return false;
				}else{
					channel.writeAndFlush(builder);
					System.out.println("发送事务操作:"+id+"-事务名:"+transname+"-事务状态:"+transtatus);
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			builder.clear();
			builder = null;
			return false;
		}finally{
			if(flag){
				tmrwLock.writeLock().unlock();
			}
		}
		return flag;
	}
	
	public Publisher tcpSendMessage(Object o,String queueName,String transId,String transtatus,String transname){
		if(o == null){
			throw new RuntimeException("数据不能为null");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("sendTopicMessage");
		if(id == null || id.length() == 0){
			id = UUID.randomUUID().toString().replace("-", "");
		}
		builder.getPublisherBuilder().setId(id);
		builder.getPublisherBuilder().setName(name);
		this.queueName = queueName;
		builder.setQueuename(queueName);
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			ByteString bs = ByteString.copyFrom(baos.toByteArray());
			if(transId!=null && !transId.equals("")){//有transId表示是事务消息
				TranMessages.Builder tm = TranMessages.newBuilder();
				tm.setId(transId);
				tm.setData(bs);
				tm.setTranstatus(transtatus);
				tm.setTransname(transname);
				builder.addTranMessages(tm);
				if(!MQDBImpl.INSTANCE.getIsDB()){
					tranType = transtatus;
					transtatusMap.put(transname, this);
				}else{
					MQClientDao.INSTANCE.savePublisherToDB(this.id, this.name, queueName, transname, transtatus);
				}
				System.out.println("发送事务消息:"+transId+"-事务名:"+transname+"-事务状态:"+transtatus);
			}else{
				builder.addData(bs);
			}
			if(channel == null || !channel.isOpen() || !channel.isActive()){
				builder.clear();
				builder = null;
				throw new RuntimeException("channel失效");
			}else{
				channel.writeAndFlush(builder).addListener(new ChannelFutureListener() {//监听是否成功
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						// TODO Auto-generated method stub
						if(future.isSuccess()){
							messageListener.tcpSend("success");
						}else{
							messageListener.tcpSend("error");
						}
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(baos != null){
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(oos != null){
					oos.close();
					oos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public Publisher udpSendMessage(Object o,String queueName){
		if(o == null){
			throw new RuntimeException("数据不能为null");
		}
		MQData.Builder builder = MQData.newBuilder();
		builder.setType("sendTopicMessage");
		builder.getPublisherBuilder().setId(UUID.randomUUID().toString().replace("-", ""));
		builder.getPublisherBuilder().setName(name);
		builder.setQueuename(queueName);
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			ByteString bs = ByteString.copyFrom(baos.toByteArray());
			builder.addData(bs);
			if(channel == null || !channel.isOpen() || !channel.isActive()){
				builder.clear();
				builder = null;
				throw new RuntimeException("channel失效");
			}else{
				DatagramPacket dp = new DatagramPacket(Unpooled.wrappedBuffer(builder.build().toByteArray()),
				new InetSocketAddress(UDPSendMessage.ip, UDPSendMessage.port));
				channel.writeAndFlush(dp).addListener(new ChannelFutureListener() {//监听是否成功
					
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						// TODO Auto-generated method stub
						if(future.isSuccess()){
							messageListener.udpSend("success");
						}else{
							messageListener.udpSend("error");
						}
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(baos != null){
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(oos != null){
					oos.close();
					oos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MessageListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getTranType() {
		return tranType;
	}

	public void setTranType(String tranType) {
		this.tranType = tranType;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	} 
	
}
