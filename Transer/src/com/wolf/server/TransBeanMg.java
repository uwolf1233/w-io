package com.wolf.server;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.wolf.ChannelWriter.LogWriter;
import com.wolf.javabean.AllTransBean;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.TranNet.LineLog;
import com.wolf.javabean.TranNet.Trans;
import com.wolf.javabean.TranNet.TransDatas;
import com.wolf.javabean.TransBean;
import com.wolf.log.LogServer;

//事务控制器
public class TransBeanMg {

	public static TransBeanMg INSTANCE = new TransBeanMg();
	
	public static ConcurrentMap<String, AllTransBean> allTransBean = new ConcurrentHashMap<String, AllTransBean>();
	
	public void getAllTransBean(TransDatas trans,ChannelHandlerContext ctx){
		
		LineLog lineLog = trans.getLineLog();
		LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(),
				"/getAllTransBean", "Transer", TransServer.ip);
		
		Trans ts = trans.getTrans();
		String mainId = ts.getMainId();
		boolean isMain = ts.getIsmain();
		String type = ts.getType();
		System.out.println("当前事务类型"+type);
		AllTransBean atb = null;
		if(type.equals("canCommit")){
			if(isMain){
				atb = new AllTransBean();
				allTransBean.put(mainId, atb);
				//-----日志------
				LogDatas.Builder logDatasBuilder = 
						logsBean.getLogDatasBuilder("预提交,当前为主控事务,主控ID:"+mainId, "1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
			}else{
				atb = allTransBean.get(mainId);
				//-----日志------
				LogDatas.Builder logDatasBuilder = 
						logsBean.getLogDatasBuilder("预提交,当前非主控事务,主控ID:"+mainId+",当前ID:"+ts.getServerId(),
								"1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
			}
			atb.addTransBean(new TransBean(ctx, ts.getServerId()));
		}else if(type.equals("commiting")){
			atb = allTransBean.get(mainId);
			atb.commiting(ts.getServerId(), isMain, ts.getStatus(),logsBean,mainId);
		}else if(type.equals("commitend")){
			atb = allTransBean.get(mainId);
			atb.allCommitend(ts.getServerId(), isMain, ts.getStatus(),logsBean,mainId);
		}
	}
	
}
