package com.wolf.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.wolf.ChannelWriter.LogWriter;
import com.wolf.javabean.FileBean;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.SystemNet.Files;
import com.wolf.javabean.SystemNet.HttpBean;
import com.wolf.javabean.SystemNet.HttpSession;
import com.wolf.javabean.SystemNet.LineLog;
import com.wolf.javabean.SystemNet.ServerResponse;
import com.wolf.server.HttHandle.MyThread;
import com.wolf.serverLine.ServerRequestBean;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

//用来处理服务器内部调用
public class ServerChannelHandle extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		SystemNet.Datas datas = (SystemNet.Datas)msg;
		if(datas.getHandletype().equals("ServerRequest") || datas.getHandletype().equals("ServerResponse")){
			OtherServer.pool.execute(new MyThread(datas,ctx.channel()));
		}
		else{
			ctx.fireChannelRead(msg);
		}
	}

	class MyThread implements Runnable{

		private SystemNet.Datas datas;
		private Channel channel;
		
		public MyThread(SystemNet.Datas datas,Channel channel){
			this.datas = datas;
			this.channel = channel;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean flag = true;
			while(flag){
				ServerRequestBean srb = null;
				try{
					if(!datas.getServerRequest().getPath().trim().equals("") && datas.getHandletype().equals("ServerRequest")){
						SystemNet.Datas.Builder datasBuilder = SystemNet.Datas.newBuilder();
						
						SessionLine sessionLine = new SessionLine();
						sessionHandle(datas.getHttpBean(), datasBuilder.getHttpBeanBuilder(), sessionLine);
						datasBuilder.setServerResponse(datas.getServerResponse());
						ReqResBean rrb = null;
						try {
							rrb = RefController.INSTANCE.invokeMethod(datas,datasBuilder);//执行对应方法
							
							LineLog lineLog = datas.getLineLog();
							LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(), 
									datas.getServerRequest().getPath(), OtherServer.serverName, OtherServer.ip);
							LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("服务已调用", "1", false);
							LogWriter.logWriter.write(logDatasBuilder);
							
							if(datas.getServerResponse().getIsReturn()){
								datasBuilder.setHandletype("ServerResponse");
								LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("服务调用有返回值", "1", false);
								LogWriter.logWriter.write(logDatasBuilder1);
								//这里写入文件
							}else{
								datasBuilder.setHandletype("ServerResponseNoReturn");
								LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("服务调用没返回值", "1", false);
								LogWriter.logWriter.write(logDatasBuilder1);
							}
							LineLog.Builder linelogBuilder = datasBuilder.getLineLogBuilder();
							linelogBuilder.setMainId(logsBean.getMainId());
							linelogBuilder.setParentId(logsBean.getParentId());
							linelogBuilder.setId(logsBean.getId());
							datasBuilder.getServerRequestBuilder().setPath(datas.getServerRequest().getPath());
							writeServer(channel, datasBuilder);
							writeServer(channel, datasBuilder);
							if(rrb != null && rrb.isTran()){
								boolean b = rrb.transEnd();//事务等待结束(补偿程序设计未完成)
								if(!b){
									System.out.println("进入补偿程序");
									LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("进入补偿程序", "1", false);
									LogWriter.logWriter.write(logDatasBuilder1);
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							if(rrb.getCon() != null && !rrb.getCon().isClosed()){
								rrb.getCon().close();
							}
						}
					}else if(datas.getHandletype().equals("ServerResponse")){
						String responseId = datas.getServerResponse().getResponseId();//找到家
						srb = ServerRequestBean.serRMap.get(responseId);
						ServerResponse response = datas.getServerResponse();
						
						LineLog lineLog = datas.getLineLog();
						LogsBean logsBean = new LogsBean(lineLog.getMainId(), lineLog.getParentId(), lineLog.getId(), 
								datas.getServerRequest().getPath(), OtherServer.serverName, OtherServer.ip);
						LogDatas.Builder logDatasBuilder = logsBean.getLogDatasBuilder("服务已调用并已进入返回阶段", "1", false);
						LogWriter.logWriter.write(logDatasBuilder);
						
						if(srb!=null){
							String sendDataStr = response.getServerlineData().getSendDataStr();
							Map<String,String> map = response.getServerlineData().getAttrMap();
							if(map==null || map.size() == 0 || sendDataStr == null || sendDataStr.trim().equals("")){
								ByteString byteString = response.getServerlineData().getSendDataStrBytes();
								byte[] bytes = byteString.toByteArray();
								sendDataStr = new String(bytes,Charset.forName("UTF-8"));//可能是缓存
								System.out.println();
								LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("服务调用返回阶段，缓存的数据", "1", false);
								LogWriter.logWriter.write(logDatasBuilder1);
							}
							if(response.getFilesList()!=null && response.getFilesList().size()>0){
								Map<String,Object> maps = new HashMap<String,Object>();
								List<Files> filesList = response.getFilesList();
								List<FileBean> fbList = FileBean.filesToFileBean(filesList);
								maps.put("$fileBeans", fbList);
								LogDatas.Builder logDatasBuilder1 = logsBean.getLogDatasBuilder("服务调用返回阶段，文件传输", "1", false);
								LogWriter.logWriter.write(logDatasBuilder1);
								if(sendDataStr!=null && !sendDataStr.trim().equals("")){
									maps.put("sendDataStr", sendDataStr);//server之间调用返回
								}else if(map!=null && map.size()>0){
									maps.putAll(map);
								}
								srb.setResponse(maps);
							}else if(sendDataStr!=null && !sendDataStr.trim().equals("")){
								srb.setResponse(sendDataStr);//server之间调用返回
							}else if(map!=null){
								srb.setResponse(map);
							}else{
								srb.setResponse("error");
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					if(srb!=null){
						try {
							srb.setResponse("error");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}finally{
					flag = false;
				}
			}
		}
		
	}
	
	/**
	 * 将session数据加入(后期将改为根据业务服务器传session数据)
	 * @param httpBean
	 * @param builder
	 * @param map
	 */
//	private void sessionHandle(HttpBean httpBean,HttpBean.Builder builder,Map<String,String> map){
//		HttpSession session = httpBean.getRequest().getSession();
//		Map<String,String> sessionMap = session.getSessionMap();
//		map.putAll(sessionMap);
//		HttpSession.Builder sessionBuilder = builder.getRequestBuilder().getSessionBuilder();
//		sessionBuilder.setServerName(OtherServer.serverName);
//		sessionBuilder.setSessionId(session.getSessionId());
//	}
	
	/**
	 * 将session数据加入(后期将改为根据业务服务器传session数据)
	 * @param httpBean
	 * @param builder
	 * @param map
	 */
	private void sessionHandle(HttpBean httpBean,HttpBean.Builder builder,SessionLine sessionLine){
		HttpSession session = httpBean.getRequest().getSession();
		HttpSession.Builder sessionBuilder = builder.getRequestBuilder().getSessionBuilder();
		sessionBuilder.setSessionId(session.getSessionId());
		sessionLine.setSessionId(session.getSessionId());
	}
	
	private boolean writeServer(Channel channel,SystemNet.Datas.Builder datas){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(datas);
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
