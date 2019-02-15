package com.wolf.javabean;

import io.netty.channel.Channel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.ByteString;
import com.wolf.javabean.MQ.MQData;
import com.wolf.javabean.MQ.TranMessages;

public class TranMessage {

	public static int maxTime = 10;//最大超时时间
	
	private String id;
	private String type;//类型 queue/topic
	private Producer producer;
	private Publisher publisher;
	private Long starttime;
	private Long endtime;//用于判断是否需要确认
	private Channel channel;
	private boolean isReturnWait;//是否已经回去进行事务确认
	private String queueName;
	private String transName;
	private ByteString byteString;//数据
	private String ppid;//不管是什么模式，断线重连对应ID
	
	public String getTransName() {
		return transName;
	}
	public TranMessage setTransName(String transName) {
		this.transName = transName;return this;
	}
	public String getQueueName() {
		return queueName;
	}
	public TranMessage setQueueName(String queueName) {
		this.queueName = queueName;return this;
	}
	public boolean isReturnWait() {
		return isReturnWait;
	}
	public TranMessage setReturnWait(boolean isReturnWait) {
		this.isReturnWait = isReturnWait;
		return this;
	}
	public String getId() {
		return id;
	}
	public TranMessage setId(String id) {
		this.id = id;return this;
	}
	public String getType() {
		return type;
	}
	public TranMessage setType(String type) {
		this.type = type;return this;
	}
	public Producer getProducer() {
		return producer;
	}
	public TranMessage setProducer(Producer producer) {
		this.producer = producer;return this;
	}
	public Publisher getPublisher() {
		return publisher;
	}
	public TranMessage setPublisher(Publisher publisher) {
		this.publisher = publisher;return this;
	}
	
	/**
	 * 是否需要确认,只能用于扫描判断
	 * @return
	 */
	public boolean isun(){
		if(endtime == null){
			return false;
		}
		Date now = new Date();
//		System.out.println("id----"+id);
//		System.out.println("时间超时----"+(now.getTime() >= endtime));
//		System.out.println("当前时间---"+now.getTime());
//		System.out.println("结束时间---"+endtime);
		if(now.getTime() >= endtime){
			return false;
		}else{
			return true;
		}
		
	}
	public Channel getChannel() {
		return channel;
	}
	public TranMessage setChannel(Channel channel) {
		this.channel = channel;return this;
	}
	public ByteString getByteString() {
		return byteString;
	}
	public TranMessage setByteString(ByteString byteString) {
		this.byteString = byteString;return this;
	}
	
	public TranMessage refresh(){
		Date startTimeDate = new Date();
		this.starttime = startTimeDate.getTime();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, maxTime);
		Date endtimeDate = calendar.getTime();
		endtime = endtimeDate.getTime();
		return this;
	}
	
//	public void returnSuccess(){//返回发送成功
//		if(channel!=null && channel.isOpen() && channel.isActive()){
//			MQData.Builder builder = MQData.newBuilder();
//			builder.setType("returnProducer");
//			TranMessages.Builder tmBuilder = TranMessages.newBuilder();
//			tmBuilder.setId(id);
//			tmBuilder.setTranstatus("sendsuccess");//返回接受成功
//			tmBuilder.setTransname(transName);
//			builder.getTranMessagesBuilderList().add(tmBuilder);
//			channel.writeAndFlush(builder);
//			System.out.println("服务器接收事务消息后返回");
//		}
//	}
	public String getPpid() {
		return ppid;
	}
	public TranMessage setPpid(String ppid) {
		this.ppid = ppid;return this;
	}
	public Long getStarttime() {
		return starttime;
	}
	public void setStarttime(Long starttime) {
		this.starttime = starttime;
	}
	public Long getEndtime() {
		return endtime;
	}
	public void setEndtime(Long endtime) {
		this.endtime = endtime;
	}
	
}
