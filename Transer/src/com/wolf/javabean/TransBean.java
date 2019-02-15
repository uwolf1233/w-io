package com.wolf.javabean;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.wolf.javabean.TranNet.Trans;
import com.wolf.javabean.TranNet.TransDatas;
import com.wolf.server.TransServer;

public class TransBean {

	private String serverTranId;//业务事务id,各个服务器提交到事务调度器生成的id
	private Boolean canCommit;//事务预提交，第一次确认
	private Boolean commiting;//事务确定，第二次确认
	private Boolean commitend;//事务提交结束，第三次确认
	private long status;//0回滚，1是提交
	private ChannelHandlerContext ctx;
	private Boolean otherCompensate;//其他事务需要补偿
	private TransDatas.Builder datasBuilder = TransDatas.newBuilder();
	
	public TransBean(ChannelHandlerContext ctx,String serverTranId){
		this.ctx = ctx;
		canCommit = true;
		this.serverTranId = serverTranId;
		Trans.Builder tbuilder = datasBuilder.getTransBuilder();
		tbuilder.setServerId(serverTranId);
	}
	
	public void setRollback(){
		Trans.Builder tbuilder = datasBuilder.getTransBuilder();
		tbuilder.setStatus(0);
	}
	
	public void setCommit(){
		Trans.Builder tbuilder = datasBuilder.getTransBuilder();
		tbuilder.setStatus(1);
	}
	
	public void returnServer(){
		writeTrans(ctx.channel(), datasBuilder);
	}
	
	public void setCompensateTrue(){//设置需要补偿
		Trans.Builder tbuilder = datasBuilder.getTransBuilder();
		tbuilder.setCompensate(true);
	}
	
//	public void canCommit(){
//		ctx.channel().writeAndFlush("");
//	}
	
	public String getServerTranId() {
		return serverTranId;
	}
	public void setServerTranId(String serverTranId) {
		this.serverTranId = serverTranId;
	}
	public Boolean getCanCommit() {
		return canCommit;
	}
	public void setCanCommit(boolean canCommit) {
		this.canCommit = canCommit;
	}
	public Boolean getCommiting() {
		return commiting;
	}
	public void setCommiting(boolean commiting) {
		this.commiting = commiting;
	}
	public Boolean getCommitend() {
		return commitend;
	}
	public void setCommitend(boolean commitend) {
		this.commitend = commitend;
	}
	public ChannelHandlerContext getCtx() {
		return ctx;
	}
	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	public void setCanCommit(Boolean canCommit) {
		this.canCommit = canCommit;
	}
	public void setCommiting(Boolean commiting) {
		this.commiting = commiting;
	}
	public void setCommitend(Boolean commitend) {
		this.commitend = commitend;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public Boolean getOtherCompensate() {
		return otherCompensate;
	}

	public void setOtherCompensate(Boolean otherCompensate) {
		this.otherCompensate = otherCompensate;
	}

	public TransDatas.Builder getDatasBuilder() {
		return datasBuilder;
	}
	
	
	private boolean writeTrans(Channel channel,TransDatas.Builder datasBuilder){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datasBuilder);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == TransServer.timeOut*1000){
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
