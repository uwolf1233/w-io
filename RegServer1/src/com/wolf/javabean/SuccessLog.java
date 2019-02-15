package com.wolf.javabean;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.LogBean.Logs;

import io.netty.channel.Channel;

public class SuccessLog {

	private String mainId;
	private String parentId;
	private String id;
	private String time;
	private String message;
	private String path;
	private String serverName;
	private String ip;
	private String type;
	private Channel logChannel;
	
	public Channel getLogChannel() {
		return logChannel;
	}
	public void setLogChannel(Channel logChannel) {
		this.logChannel = logChannel;
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
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public void parentIdCreate(){
		this.parentId = this.parentId + "-" + this.id;
	}
	
	public void send(String message,String type){
		if(logChannel == null){
			throw new RuntimeException("Log channel is null");
		}
		if(type == null || (!type.equals("0") && !type.equals("1"))){
			throw new RuntimeException("type error");
		}
		if(message == null){
			throw new RuntimeException("message is null");
		}
		LogDatas.Builder datasBuilder = LogDatas.newBuilder();
		Logs.Builder logsBuilder = datasBuilder.getLogsBuilder();
		logsBuilder.setMainId(this.mainId);
		logsBuilder.setParentId(this.parentId);
		logsBuilder.setId(this.id);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = format.format(new Date());
		logsBuilder.setTime(date);
		logsBuilder.setMessage(message);
		logsBuilder.setPath(this.path);
		logsBuilder.setServerName(this.serverName);
		logsBuilder.setIp(this.ip);
		logsBuilder.setType(this.type);
		this.logChannel.writeAndFlush(logsBuilder);
	}
	
}
