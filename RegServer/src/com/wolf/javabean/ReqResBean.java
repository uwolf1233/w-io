package com.wolf.javabean;

import io.netty.channel.Channel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.ByteString;
import com.wolf.ChannelWriter.LogWriter;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.LogBean.Logs;
import com.wolf.javabean.SystemNet.Cookies;
import com.wolf.javabean.SystemNet.Files;
import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.HttpServletRequest;
import com.wolf.javabean.SystemNet.HttpServletResponse;
import com.wolf.javabean.SystemNet.ServerRequest;
import com.wolf.javabean.SystemNet.ServerResponse;
import com.wolf.javabean.TranNet.LineLog;
import com.wolf.javabean.TranNet.Trans;
import com.wolf.javabean.TranNet.TransDatas;
import com.wolf.server.LineTransServer;
import com.wolf.server.LogServer;
import com.wolf.server.OtherServer;
import com.wolf.server.WebSocketHandle;

public class ReqResBean {

	private String type;
	private HttpServletResponse.Builder cresponse = null;
	private HttpServletRequest crequest = null;
	private ServerRequest cserverRequest = null;
	private ServerResponse.Builder cserverResponse = null;
	private String sessionId = null;
	private Connection con = null;//开启事务为前提
	private Channel channel = null;
	private String mainId = null;
	private String serverId = null;
	private Boolean isMain = null;
	private TransDatas.Builder trans = null;
	private BlockingQueue<TransDatas> tranQueue = null;//事务线程控制
	private boolean isTran = false;//是否事务控制
	private List<FileBean> fileBean;//此fileBean是用于http上传进来文件接收
	private List<Files.Builder> serverOutfileBean;//此fileBean是用于内部接口调用发送
	private List<FileBean> serverInfileBean;//此fileBean是用于内部接口调用接收
	private WebSocketBean webSocketBean;
	private Channel logChannel;
	private LogsBean logsBean;
	private TwoTranListener twoTranListener;

	public LogsBean getLogsBean() {
		return logsBean;
	}

	public void setLogsBean(LogsBean logsBean) {
		this.logsBean = logsBean;
	}

	public Channel getLogChannel() {
		return logChannel;
	}

	public void setLogChannel(Channel logChannel) {
		this.logChannel = logChannel;
	}

	/**
	 * 分开统一
	 * @param type
	 * @param response
	 * @param request
	 */
	public ReqResBean(String type,Object response,Object request,String sessionId){
		this.type = type;
		this.sessionId = sessionId;
		List<Files> files = null;
		if(type.equals("http")){
			cresponse = (HttpServletResponse.Builder)response;
			crequest = (HttpServletRequest)request;
			files = crequest.getFilesList();
		}else if(type.equals("server")){
			cserverResponse = (ServerResponse.Builder)response;
			cserverRequest = (ServerRequest)request;
		}
		//如果有文件，将文件写入文件类
		if(files!=null && files.size()>0){
			readInFile(files,"http");
		}else if(cserverRequest!=null && cserverRequest.getFilesList() != null && cserverRequest.getFilesList().size() > 0){//请求进来的文件
			readInFile(cserverRequest.getFilesList(),"server");
		}
	}
	
	/**
	 * 提供给websocket操作
	 * @param 
	 */
	public ReqResBean(WebSocketBean webSocketBean,String sessionId){
		this.webSocketBean = webSocketBean;
	}
	
	private void readInFile(List<Files> files,String type){
		fileBean = new ArrayList<FileBean>();
		int filesSize = files.size();
		System.out.println("当前文件数量"+filesSize);
		for(int i=0;i<filesSize;i++){
			Files fs = files.get(i);
			ByteString bs = fs.getFiledata();
			byte[] bytes = bs.toByteArray();
			if(bytes.length == 0){
				continue;
			}
			FileBean fb = new FileBean();
			fb.setFiledata(bytes);
			fb.setFileName(fs.getFileName());
			fb.setHz(fs.getHz());
			fb.setReadlong(fs.getReadlong());
			fb.setRealAllLong(fs.getRealAllLong());
			if(type.equals("http")){
				if(fileBean == null){
					fileBean = new ArrayList<FileBean>();
				}
				fileBean.add(fb);
			}else if(type.equals("server")){
				if(serverInfileBean == null){
					serverInfileBean = new ArrayList<FileBean>();
				}
				serverInfileBean.add(fb);
			}
		}
	}
	
	public Map<String,String> getRequestMap(){
		if(type.equals("http")){
			return crequest.getParameterMap();
		}else if(type.equals("server")){
			return cserverRequest.getServerlineData().getAttrMap();
		}else{
			return null;
		}
	}
	
	public void setResponseMap(Map<String,String> map){
		if(type.equals("http")){
			cresponse.putAllAttr(map);
		}else if(type.equals("server")){
			cserverResponse.getServerlineDataBuilder().putAllAttr(map);
		}
	}
	
	public void setResponseKV(String key,String value){
		if(type.equals("http")){
			cresponse.putAttr(key, value);
		}else if(type.equals("server")){
			cserverResponse.getServerlineDataBuilder().putAttr(key, value);
		}
	}
	
	/**
	 * 接口调用返回文件
	 */
	public void addServerResponseFile(File[] files){
		for(File file : files){
			if(file.exists()){
				String fileName = file.getName();
				String fn = fileName.substring(0, fileName.lastIndexOf("."));
				String hz = fileName.substring(fileName.lastIndexOf("."));
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					byte[] bytes = new byte[fis.available()];
					fis.read(bytes);
					ByteString bs = ByteString.copyFrom(bytes);
					Files.Builder fb = Files.newBuilder();
					fb.setFiledata(bs);
					fb.setFileName(fn);
					fb.setHz(hz);
					fb.setReadlong(bytes.length);
					fb.setRealAllLong(bytes.length);
					cserverResponse.addFiles(fb);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						if(fis!=null){
							fis.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void setResponseData(String data){
		if(type.equals("http")){
			cresponse.setResponseData(data);
		}else if(type.equals("server")){
			cserverResponse.getServerlineDataBuilder().setSendDataStr(data);
		}
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public void trans(Connection con,Channel channel,String mainId,String serverId,Boolean isMain){
		if(channel == null){
			try {
				throw new Exception("trans not line");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.con = con;
		this.channel = channel;
		this.mainId = mainId;
		this.serverId = serverId;
		this.isMain = isMain;
		this.trans = TransDatas.newBuilder();
		this.isTran = true;
		Trans.Builder ts = trans.getTransBuilder();
		ts.setMainId(mainId);
		ts.setServerId(serverId);
		ts.setIsmain(isMain);
		LineTransServer.transTaskMap.put(serverId, this);
		tranQueue = new ArrayBlockingQueue<>(1);
	}
	
	public boolean thransThreadInit(){
		try {
			Trans.Builder ts = trans.getTransBuilder();
			ts.setStatus(1);
			ts.setType("canCommit");//第一次确认
			
			//-----日志------
			LogDatas.Builder logDatasBuilder = 
					logsBean.getLogDatasBuilder("预提交,主控ID:"+mainId+",当前ID:"+serverId+",第一次正在确认", "1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
			
			LineLog.Builder lineLogBuilder = trans.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			writeTran(channel, trans);
			TransDatas tds = tranQueue.poll(5000, TimeUnit.SECONDS);
			if(tds == null){
				System.out.println("第一次确认失败");
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("预提交,主控ID:"+mainId+",当前ID:"+serverId+",第一次确认失败", "0", false);
				LogWriter.logWriter.write(logDatasBuilder);
				return false;
			}
			System.out.println("第一次已确认");
			//-----日志------
			logDatasBuilder = 
					logsBean.getLogDatasBuilder("预提交,主控ID:"+mainId+",当前ID:"+serverId+",第一次已确认", "1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if(con!=null){
					con.close();
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
	}
	
	//返回是否有补偿，status，1是提交，0是回滚
	public void thransThread(int status){//事务过程
		//事务确认
		try {
			Trans.Builder ts = trans.getTransBuilder();
			ts.setType("commiting");//第二次确认
			ts.setStatus(status);
			System.out.println("第二次确认");
			
			//-----日志------
			LogDatas.Builder logDatasBuilder = 
					logsBean.getLogDatasBuilder("正在提交,主控ID:"+mainId+",当前ID:"+serverId+",第二次正在确认", "1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
			
			LineLog.Builder lineLogBuilder = trans.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			writeTran(channel, trans);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if(con!=null){
					con.close();
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public boolean transEnd(){
		System.out.println("transEnd");
		try {
			Trans.Builder ts = trans.getTransBuilder();
			TransDatas tds = tranQueue.take();
			
			System.out.println("第二次已确认");
			
			//-----日志------
			LogDatas.Builder logDatasBuilder = 
					logsBean.getLogDatasBuilder("正在提交,主控ID:"+mainId+",当前ID:"+serverId+",第二次已确认", "1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
			
			LineLog.Builder lineLogBuilder = trans.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			long rstatus = tds.getTrans().getStatus();
			if(rstatus == 0){
				System.out.println("业务事务回滚");
				
				//-----日志------
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("第二次已确认,主控ID:"+mainId+",当前ID:"+serverId+",业务事务回滚", "1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
				
				lineLogBuilder = trans.getLineLogBuilder();
				lineLogBuilder.setMainId(logsBean.getMainId());
				lineLogBuilder.setParentId(logsBean.getParentId());
				lineLogBuilder.setId(logsBean.getId());
				
			}else{
				System.out.println("业务事务提交");
				//-----日志------
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("第二次已确认,主控ID:"+mainId+",当前ID:"+serverId+",业务事务提交", "1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
				
				lineLogBuilder = trans.getLineLogBuilder();
				lineLogBuilder.setMainId(logsBean.getMainId());
				lineLogBuilder.setParentId(logsBean.getParentId());
				lineLogBuilder.setId(logsBean.getId());
			}
			if(con!=null && !con.isClosed()){
				if(rstatus == 0){
					con.rollback();
					if(twoTranListener!=null){
						twoTranListener.commitOrRollback("rollback");
					}
				}else{
					con.commit();
					if(twoTranListener!=null){
						twoTranListener.commitOrRollback("commit");
					}
				}
			}//连接超时或者被关闭后管理
			ts.setType("commitend");
			ts.setStatus(rstatus);//返回当前属性
			
			//-----日志------
			logDatasBuilder = 
					logsBean.getLogDatasBuilder("第三次正在确认,主控ID:"+mainId+",当前ID:"+serverId+"", "1", false);
			LogWriter.logWriter.write(logDatasBuilder);
			//-----日志------
			
			lineLogBuilder = trans.getLineLogBuilder();
			lineLogBuilder.setMainId(logsBean.getMainId());
			lineLogBuilder.setParentId(logsBean.getParentId());
			lineLogBuilder.setId(logsBean.getId());
			
			writeTran(channel, trans);//第三次，已提交后确认
			tds = tranQueue.poll(5000, TimeUnit.SECONDS);
			if(tds!=null && tds.getTrans().getCompensate()){//如果有补偿
				//-----日志------
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("第三次确认,主控ID:"+mainId+",当前ID:"+serverId+",进入补偿程序", "1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
				
				lineLogBuilder = trans.getLineLogBuilder();
				lineLogBuilder.setMainId(logsBean.getMainId());
				lineLogBuilder.setParentId(logsBean.getParentId());
				lineLogBuilder.setId(logsBean.getId());
				return false;
			}
			System.out.println("业务事务走完");
			if(isMain){
				System.out.println("所有事务走完");
				LineTransServer.transTaskMap.remove(mainId);
				//-----日志------
				logDatasBuilder = 
						logsBean.getLogDatasBuilder("第三次确认,主控ID:"+mainId+",当前ID:"+serverId+",所有事务走完", "1", false);
				LogWriter.logWriter.write(logDatasBuilder);
				//-----日志------
			}
			return true;
		} catch (Exception e) {//这里可能要分开不同异常处理不同问题，提交或者回滚异常和最后一次消息发送异常应该是不一样的
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if(con!=null){
					con.close();
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
	}
	
	public void returnTrans(TransDatas tds){
		try {
			tranQueue.put(tds);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Connection getCon() {
		return con;
	}

	public String getMainId() {
		return mainId;
	}

	public Boolean getIsMain() {
		return isMain;
	}

	public boolean isTran() {
		return isTran;
	}

	public void setTran(boolean isTran) {
		this.isTran = isTran;
	}

	public List<FileBean> getFileBean() {
		return fileBean;
	}
	
	public void addCookie(String key,String value){
		Cookies.Builder cookiesBuilder = Cookies.newBuilder();
		cookiesBuilder.setKey(key);
		cookiesBuilder.setValue(value);
		cresponse.addCookie(cookiesBuilder);
	}
	
	//文件下载，输入文件路径
	public void outFile(String filePath){
		File file = new File(filePath);
		if(!file.exists()){
			cresponse.getFilesBuilder().setFileName("not found");
			return;
		}
		String fileName = file.getName();
		String fn = fileName.substring(0, fileName.lastIndexOf("."));
		String hz = fileName.substring(fileName.lastIndexOf("."));
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			ByteString bs = ByteString.copyFrom(bytes);
			cresponse.getFilesBuilder().setFiledata(bs);
			cresponse.getFilesBuilder().setFileName(fn);
			cresponse.getFilesBuilder().setHz(hz);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fis!=null){
					fis.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//文件下载，字节流传输
	public void outFile(byte[] bytes,String fileName){
		if(bytes.length == 0){
			cresponse.getFilesBuilder().setFileName("not found");
			return;
		}
		String fn = fileName.substring(0, fileName.lastIndexOf("."));
		String hz = fileName.substring(fileName.lastIndexOf("."));
		FileInputStream fis = null;
		try {
			ByteString bs = ByteString.copyFrom(bytes);
			cresponse.getFilesBuilder().setFiledata(bs);
			cresponse.getFilesBuilder().setFileName(fn);
			cresponse.getFilesBuilder().setHz(hz);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fis!=null){
					fis.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void addServerOutFileBean(File file){
		if(file.exists()){
			String fileName = file.getName();
			String fn = fileName.substring(0, fileName.lastIndexOf("."));
			String hz = fileName.substring(fileName.lastIndexOf("."));
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				byte[] bytes = new byte[fis.available()];
				fis.read(bytes);
				if(serverOutfileBean == null){
					serverOutfileBean = new ArrayList<Files.Builder>();
				}
				ByteString bs = ByteString.copyFrom(bytes);
				Files.Builder fb = Files.newBuilder();
				fb.setFiledata(bs);
				fb.setFileName(fn);
				fb.setHz(hz);
				fb.setReadlong(bytes.length);
				fb.setRealAllLong(bytes.length);
				serverOutfileBean.add(fb);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if(fis!=null){
						fis.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//通过bean拿到group
	public WebSocketGroup getWebSocketGroup(WebSocketBean webSocketBean){
		String path = webSocketBean.getPath();
		if(path == null || path.trim().equals("")){
			return null;
		}
		return WebSocketHandle.groupMap.get(path);
	}
	
	public void sendWebSocket(WebSocketGroup wsGroup,String message){//手动输入消息安组发
		wsGroup.sendWebsocketMessage(message);
	}
	
	public void sendWebSocket(WebSocketGroup wsGroup){//已有消息按组发
		wsGroup.sendWebsocketMessage();
	}
	
	public void sendWebSocket(WebSocketBean webSocketBean,String message){//手动输入消息单个发
		WebSocketMessage wsMessage = new WebSocketMessage();
		wsMessage.setType("String");
		wsMessage.setMessage(message);
		webSocketBean.send(wsMessage);//发送message对象
	}
	
	public List<FileBean> getServerInFileBean(){
		return this.serverInfileBean;
	}
	
	public List<Files.Builder> getServerOutFileBean(){
		return this.serverOutfileBean;
	}
	
	public WebSocketBean getWebSocketBean() {
		return webSocketBean;
	}
	
	public void log(String message,String type){//发送日志
		if(this.logChannel != null && this.logChannel.isOpen() && this.logsBean!=null){
			LogWriter.logWriter.write(logsBean.getLogDatasBuilder(message, type,false));
		}
	}

	public void setMainId(String mainId) {
		this.mainId = mainId;
	}

	public TwoTranListener getTwoTranListener() {
		return twoTranListener;
	}

	public void setTwoTranListener(TwoTranListener twoTranListener) {
		this.twoTranListener = twoTranListener;
	}
	
	private boolean writeTran(Channel channel,TransDatas.Builder trans){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(trans);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == OtherServer.timeOut*1000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(trans);
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
