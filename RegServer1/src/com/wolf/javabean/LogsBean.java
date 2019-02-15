package com.wolf.javabean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.LogBean.Logs;
import com.wolf.server.LogServer;

public class LogsBean {

	private String mainId;
	private String parentId;
	private String id;
	private String path;
	private String serverName;
	private String ip;
	
	public LogsBean(){}
	
	public LogsBean(String mainId,String parentId,String id,String path,String serverName,String ip){
		this.mainId = mainId;
		this.parentId = parentId;
		this.id = id;
		this.path = path;
		this.serverName = serverName;
		this.ip = ip;
	}
	
	public String getMainId() {
		return mainId;
	}
	public void setMainId(String mainId) {
		this.mainId = mainId;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public LogDatas.Builder getLogDatasBuilder(String message,String type,boolean init){
		LogDatas.Builder logDatasBuilder = LogDatas.newBuilder();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		Logs.Builder logsBuilder = logDatasBuilder.getLogsBuilder();
		logsBuilder.setMainId(mainId);
		if(!init){
			if(parentId.equals("")){
				parentId = mainId;
			}else{
				parentId = parentId + "-" + id;
			}
			id = UUID.randomUUID().toString().replace("-", "");
		}
		logsBuilder.setParentId(parentId);
		logsBuilder.setId(id);
		logsBuilder.setTime(time);
		logsBuilder.setMessage(message);
		logsBuilder.setType(type);
		logsBuilder.setPath(path);
		logsBuilder.setServerName(serverName);
		logsBuilder.setIp(ip);
		return logDatasBuilder;
	}
	
	public void send(String message,String type,boolean init){
		if(LogServer.logChannel.isOpen() && LogServer.logChannel.isActive()){
			LogDatas.Builder logDatasBuilder = this.getLogDatasBuilder(message, type, init);
			LogServer.logChannel.writeAndFlush(logDatasBuilder);
		}
	}
	
}
