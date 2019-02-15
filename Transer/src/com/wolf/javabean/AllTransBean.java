package com.wolf.javabean;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.ChannelWriter.LogWriter;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.TranNet.TransDatas;
import com.wolf.log.LogServer;
import com.wolf.server.TransServer;

public class AllTransBean {
	
	//private ReentrantReadWriteLock wrlock = new ReentrantReadWriteLock(false);
	private List<TransBean> tbList = new ArrayList<TransBean>();//事务容器
	private Boolean allCommiting;//所有事务确定，第二次确认，只要一个事务未通过，则所有事务未通过
	private int allStatus = 0;//总状态，0是回滚，1是提交

	public void addTransBean(TransBean tb){//添加事务
		System.out.println("添加事务");
		tbList.add(tb);
		tb.getDatasBuilder().getTransBuilder().setStatus(1);
		writeTrans(tb.getCtx().channel(), tb.getDatasBuilder());
		System.out.println(tb.getServerTranId()+"添加事务完成");
	}
	
	//第二阶段，如果不是主控事务，就自己确认自己的事务
	public void commiting(String serverTranId,boolean isMain,long status,LogsBean logsBean,String mainId){
		System.out.println("当前事务数量---"+tbList.size());
		System.out.println("确认");
		if(isMain){
			//-----日志------
			LogDatas.Builder logDatasBuilder = 
					logsBean.getLogDatasBuilder("提交中,当前主控事务,主控ID:"+mainId+",当前ID:"+serverTranId,
							"1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
			if(status == 0){
				//告诉所有服务器全部回滚
				allRollback();
				System.out.println("全部回滚");
				//-----日志------
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("提交中,当前非主控事务,主控ID:"+mainId+",当前ID:"+serverTranId+",主控事务正在提交，但全部回滚",
								"1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
			}else{
				allStatus = 1;
				List<TransBean> isall = new ArrayList<TransBean>();//用于保存还没进入第二步的事务，也许是断线了，可以设置等待超时程序，目前暂时不处理
				for(TransBean tb : tbList){
					if(tb.getServerTranId().equals(serverTranId)){
						tb.setCommiting(true);
						tb.setStatus(status);
						//-----日志------
						logDatasBuilder = 
								logsBean.getLogDatasBuilder("提交中,主控ID:"+mainId+",当前ID:"+serverTranId+",已找到对应的预提交",
										"1", false);
						LogWriter.logWriter.write(logDatasBuilder);
						//-----日志------
					}
					if(tb.getCommiting() == null){
						isall.add(tb);//不知道什么原因未能进入第二步的事务
						//-----日志------
						logDatasBuilder = 
								logsBean.getLogDatasBuilder("提交中,主控ID:"+mainId+",当前ID:"+serverTranId+",未能进入第二步事务",
										"1", false);
						LogWriter.logWriter.write(logDatasBuilder);
						//-----日志------
					}
					if(tb.getStatus() == 0){
						allStatus = 0;
					}
				}
				if(allStatus == 0){
					//告诉所有服务器全部回滚
					allRollback();
					System.out.println("全部回滚");
					//-----日志------
					logDatasBuilder = 
							logsBean.getLogDatasBuilder("提交中,主控ID:"+mainId+",当前ID:"+serverTranId+",全部回滚",
									"1", false);
					LogWriter.logWriter.write(logDatasBuilder);
					//-----日志------
				}else{
					//告诉所有服务器全部提交
					allCommit();
					System.out.println("全部提交");
					//-----日志------
					logDatasBuilder = 
							logsBean.getLogDatasBuilder("提交中,主控ID:"+mainId+",当前ID:"+serverTranId+",全部提交",
									"1", false);
					LogWriter.logWriter.write(logDatasBuilder);
					//-----日志------
				}
			}
			System.out.println("确认完成");
			//-----日志------
			logDatasBuilder = 
					logsBean.getLogDatasBuilder("提交中,主控ID:"+mainId+",当前ID:"+serverTranId+",全部提交或全部回滚确认完成",
							"1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
		}else{
			for(TransBean tb : tbList){
				if(tb.getServerTranId().equals(serverTranId)){
					tb.setCommiting(true);//设置正在提交
					tb.setStatus(status);
					//-----日志------
					LogDatas.Builder logDatasBuilder = 
							logsBean.getLogDatasBuilder("提交中,主控ID:"+mainId+",当前ID:"+serverTranId+",当前非主控事务，但正在提交",
									"1", false);
					LogWriter.logWriter.write(logDatasBuilder);
					//-----日志------
				}
			}
		}
	}
	
	public void allCommitend(String serverTranId,boolean isMain,long status,LogsBean logsBean,String mainId){//事务结尾，可能是回滚可能是提交
		System.out.println(serverTranId+"事务结尾");
		//-----日志------
		LogDatas.Builder logDatasBuilder = 
				logsBean.getLogDatasBuilder("事务结尾,主控ID:"+mainId+",当前ID:"+serverTranId+",已进入事务结尾",
						"1", false);
		LogWriter.logWriter.write(logDatasBuilder);
		//-----日志------
		if(isMain){
			boolean isAllCompensate = false;//是否全部进入补偿程序
			for(TransBean tb : tbList){
				if(tb.getServerTranId().equals(serverTranId)){
					tb.setCommitend(true);
					tb.setStatus(status);
				}
				if(tb.getStatus() != allStatus){
					//如果出现已经提交事务状态和当前事务状态不符
					if(tb.getStatus() == 1 && allStatus == 0){
						//需要回滚的时候不回滚，则需要进入补偿程序
						tb.setOtherCompensate(true);
						tb.returnServer();
						System.out.println("当前事务进入补偿");
						//-----日志------
						logDatasBuilder = 
								logsBean.getLogDatasBuilder("事务结尾,主控ID:"+mainId+",当前ID:"+serverTranId+",当前事务进入补偿",
										"1", false);
						LogWriter.logWriter.write(logDatasBuilder);
						//-----日志------
					}else if(tb.getStatus() == 0 && allStatus == 1){
						//本身已经回滚了但其他事务却已经提交
						isAllCompensate = true;
						tb.setOtherCompensate(true);
						//-----日志------
						logDatasBuilder = 
								logsBean.getLogDatasBuilder("事务结尾,主控ID:"+mainId+",当前ID:"+serverTranId+",当前事务回滚，其他事务进入补偿",
										"1", false);
						LogWriter.logWriter.write(logDatasBuilder);
						//-----日志------
					}
				}
			}
			if(isAllCompensate){
				for(TransBean tb : tbList){
					if(tb.getOtherCompensate() != null && tb.getOtherCompensate()){//自己已经回滚了
						continue;
					}
					tb.setCompensateTrue();
					writeTrans(tb.getCtx().channel(), tb.getDatasBuilder());//其他系统的事务设置补偿
				}
			}else{
				for(TransBean tb : tbList){
					tb.returnServer();
				}
				//-----日志------
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("事务结尾,主控ID:"+mainId+",当前ID:"+serverTranId+",所有事务完结，不需要补偿",
								"1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
			}
			System.out.println("事务结尾完成");
		}else{
			for(TransBean tb : tbList){
				if(tb.getServerTranId().equals(serverTranId)){
					tb.setCommitend(true);
					tb.setStatus(status);
				}
			}
			//-----日志------
			logDatasBuilder = 
					logsBean.getLogDatasBuilder("事务结尾,主控ID:"+mainId+",当前ID:"+serverTranId+",非主控事务完结中",
							"1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
		}
	}
	
	private void allRollback(){
		allCommiting = true;
		allStatus = 0;
		for(TransBean tb : tbList){
			tb.setRollback();
			tb.returnServer();
		}
	}
	
	private void allCommit(){
		allCommiting = true;
		allStatus = 1;
		for(TransBean tb : tbList){
			tb.setCommit();
			tb.returnServer();
		}
	}
	
	private boolean writeTrans(Channel channel,TransDatas.Builder datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
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
					channel.writeAndFlush(datas);
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
