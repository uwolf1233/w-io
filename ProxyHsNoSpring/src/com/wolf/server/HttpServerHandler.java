package com.wolf.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.ByteString;
import com.wolf.ChannelWriter.LogWriter;
import com.wolf.cache.StaticFileCache;
import com.wolf.center.CenterHandle;
import com.wolf.center.CenterServer;
import com.wolf.javabean.GsonBean;
import com.wolf.javabean.LogsBean;
import com.wolf.javabean.ServerBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.SystemNet;
import com.wolf.javabean.LogBean.LogDatas;
import com.wolf.javabean.SystemNet.Files;
import com.wolf.log.LogServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryFileUpload;
import io.netty.handler.codec.http.multipart.MixedFileUpload;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

//一次连接都会有一个handle
public class HttpServerHandler extends ChannelInboundHandlerAdapter{

	private HttpRequest request;//当前连接的request
	private AsciiString method;//读取请求的方法
	private boolean ishandle = false;//是否正在处理
	private SystemNet.Datas.Builder datas = SystemNet.Datas.newBuilder();//自定义协议
	private int readCount = 0;//断线尝试重连次数
	public static int maxReadCont = 0;//最大尝试重连数
	private ChannelHandlerContext ctxs = null;//当前连接的ChannelHandlerContext对象，用于获取channel
	//private String responseId;//用于辨别发送和返回的response，因为访问各个服务器还是长连接
	public CenterHandle centerHandle;//注册中心的handle，用于转发请求
	private static AtomicInteger reqnum = new AtomicInteger();//前端请求数
	private Map<String,String> map = null;
	private HttpPostRequestDecoder decoderFile = null;
	private final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
	public LogsBean logsBean = null;
	
	
    @Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	logsBean = new LogsBean(UUID.randomUUID().toString().replace("-", ""), "", "", "httpChannelActive", "proxyHs", HttpServer.ip);
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		if(maxReadCont == 0){return;}
    	IdleStateEvent ide = (IdleStateEvent)evt;
    	//心跳检测是否断线，3次关闭(可作为配置)
    	if(ide.state() == IdleState.READER_IDLE){
    		if(readCount >= maxReadCont){
    			ctx.close();
    			return;
    		}
    		readCount ++;
    	}
	}
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
		System.out.println("reqnums==="+reqnum.incrementAndGet());
		System.out.println(ctx.channel().id().asLongText());
		datas.clear();
    	try{
    		if(ctxs == null){
    			ctxs = ctx;//ctx赋值给当前对象
    		}
    		if(readCount>0){
    			readCount = 0;
    		}
	    	if(msg instanceof HttpRequest){
	    		map = new HashMap<String,String>();
	    		String path = "";
	    		request = (HttpRequest)msg;
	    		method = request.method().asciiName();
	    		URI uri = new URI(request.uri());
    			path = uri.getPath();
	    		if(method.toString().equals("GET")){//如果是GET，按GET的方式
	    			if(path.equals("/favicon.ico")){//暂时退回
	    				ControllerProxy.INSTANCE().sendHtmlFile("/imgs/title.ico", ctx.channel());
	    				return;
	    			}
	    			
	    			if(getSystemConfigs(path, ctx)){//读取系统配置页相关
	    				return;
	    			}
	    			
	    			boolean downViewFile = false;
	    			String htmlFilePath = "";
	    			for(String s : ControllerProxy.INSTANCE().viewFileType){//静态资源的设置，如果结尾为静态文件，则返回文件
	    				if(path.endsWith(s)){
	    					downViewFile = true;
	    					htmlFilePath = path;
	    				}
	    			}
	    			if(downViewFile){//发送html文件
	    				ControllerProxy.INSTANCE().sendHtmlFile(htmlFilePath, ctx.channel());
	    				return;
	    			}
	    			//------日志--------
	    			LogDatas.Builder logDatasBuilder = this.logsBean.getLogDatasBuilder("http请求入口,GET", "1",false);
	    			LogWriter.logWriter.write(logDatasBuilder);
	    			//------日志--------
	    			QueryStringDecoder decoder = new QueryStringDecoder(request.uri());//获取请求数据
	    			Map<String, List<String>> dataML = decoder.parameters();//获取请求的参数
	    			for(String key : dataML.keySet()){
	    				map.put(key, dataML.get(key).get(0));
	    			}
	    			
	    			ControllerProxy.INSTANCE().sendConMethod(datas, path, map.get("sessionId")+"",this);//暂时前端写sessionId
	    			ishandle = true;
	    		}else if(method.toString().equals("POST")){
	    			if(path!=null && !path.trim().equals("") && CenterServer.serverMap.get(path).isHasFile()){
	    				decoderFile = new HttpPostRequestDecoder(factory,request);
	    			}
	    		}
	    	}
	    	if(msg instanceof HttpContent){
	    		map = new HashMap<String,String>();
	    		String path = "";
	    		HttpContent content = null;
	    		try{
		    		content = (HttpContent)msg;
		    		if(method.toString().equals("POST")){//post请求接收，和GET差不多
			    		URI uri = new URI(request.uri());
			    		path = uri.getPath();
			    		//------日志--------
		    			LogDatas.Builder logDatasBuilder = this.logsBean.getLogDatasBuilder("http请求入口,POST", "1",false);
		    			LogWriter.logWriter.write(logDatasBuilder);
		    			//------日志--------
			    		if(CenterServer.serverMap.get(path).isHasFile() && decoderFile != null){//是否有文件
			    			decoderFile.offer(content);
			    			while(decoderFile.hasNext()){
			    				InterfaceHttpData data = decoderFile.next();
			    				if(data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute){
				    	            Attribute attribute = (Attribute) data;
				    	            String value;
				    	            try {
				    	                value = attribute.getValue();
				    	            } catch (IOException e1) {
				    	                e1.printStackTrace();
				    	                return;
				    	            }
				    	            System.out.println("传输中......");
				    			}else if(data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload){
				    				FileUpload ful = (FileUpload)data;
				    				if(ful.isCompleted()){
//				    					String upPath = "F:/fileUpTest";//测试目录
//				    					File file = new File(upPath+"/"+ful.getFilename());
//				    					if(!ful.isInMemory()){//如果文件不在内存中，就将临时文件删除
//				    						String tempPath = ful.getFile().getAbsolutePath();
//					    					ful.renameTo(file);
//					    					new File(tempPath).delete();
//				    					}else{
//				    						ful.renameTo(file);
//				    					}
				    				//上面的注释代码是将文件放到注册中心服务器，后面版本将而外提供服务器
				    					ServerBean sb = CenterServer.serverMap.get(path);
					    				if(sb.isFileIn()){//当业务方法设置为文件传输过去时，才将文件发送过去
						    				byte[] bytes = ful.get();
						    				if(bytes.length == 0){continue;}
						    				String fileNames = ful.getFilename();
						    				String fileName = fileNames.substring(0, fileNames.lastIndexOf("."));
						    				String hz = fileNames.substring(fileNames.lastIndexOf(".")+1);
						    				long readlong = bytes.length;//先写满，由于可能负载压力过大，后期会分离
						    				long readAllLong = bytes.length;
						    				Files.Builder filesBuilder = Files.newBuilder();
						    				ByteString bs = ByteString.copyFrom(bytes);
						    				filesBuilder.setFiledata(bs);
						    				filesBuilder.setFileName(fileName);
						    				filesBuilder.setReadlong(readlong);
						    				filesBuilder.setRealAllLong(readAllLong);
						    				filesBuilder.setHz(hz);
						    				datas.getHttpBeanBuilder().getRequestBuilder().addFiles(filesBuilder);
						    				//如果有磁盘临时文件，最好做一次清理，如果是混合模式文件上传，要注意区分内存和磁盘
					    				}
				    				}
				    			}
			    			}
			    		}else{
			    			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
				    		//decoder.offer(content);
				    		sendConMethod(decoder, path);
			    		}
			    		ishandle = true;
		    		}
	    		}catch(EndOfDataDecoderException e){
	    			System.out.println("文件上传完成");
	    			sendConMethod(decoderFile, path);
	    		}
	    		catch(Exception e){
	    			e.printStackTrace();
	    		}finally{
	    			if(decoderFile!=null && content instanceof LastHttpContent){
	    				decoderFile.destroy();
		    			decoderFile = null;
	    			}
	    		}
	    	}
	    	if(ishandle){
	    		ishandle = false;
	    	}
    	}catch(Throwable e){
    		e.printStackTrace();
    	}finally{
    		ReferenceCountUtil.release(msg);
    	}
    }
	
	private void sendConMethod(HttpPostRequestDecoder paramDecoder,String path) throws Exception{
		List<InterfaceHttpData> list = paramDecoder.getBodyHttpDatas();
		for(InterfaceHttpData it : list){
			if(it instanceof Attribute){
				Attribute data = (Attribute)it;
				map.put(data.getName(), data.getValue());
			}
		}
		ControllerProxy.INSTANCE().sendConMethod(datas, path, map.get("sessionId")+"",this);
		
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
    
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelRegistered(ctx);
	}
	public ChannelHandlerContext getCtxs() {
		return ctxs;
	}
	public void setCtxs(ChannelHandlerContext ctxs) {
		this.ctxs = ctxs;
	}
//	public String getResponseId() {
//		return responseId;
//	}
//	public void setResponseId(String responseId) {
//		this.responseId = responseId;
//	}
	public HttpRequest getRequest() {
		return request;
	}
	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	public AsciiString getMethod() {
		return method;
	}
	public void setMethod(AsciiString method) {
		this.method = method;
	}
	public boolean isIshandle() {
		return ishandle;
	}
	public void setIshandle(boolean ishandle) {
		this.ishandle = ishandle;
	}
	public SystemNet.Datas.Builder getDatas() {
		return datas;
	}
	public void setDatas(SystemNet.Datas.Builder datas) {
		this.datas = datas;
	}
	public int getReadCount() {
		return readCount;
	}
	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}
	public static int getMaxReadCont() {
		return maxReadCont;
	}
	public static void setMaxReadCont(int maxReadCont) {
		HttpServerHandler.maxReadCont = maxReadCont;
	}
	public CenterHandle getCenterHandle() {
		return centerHandle;
	}
	public void setCenterHandle(CenterHandle centerHandle) {
		this.centerHandle = centerHandle;
	}
	public static AtomicInteger getReqnum() {
		return reqnum;
	}
	public static void setReqnum(AtomicInteger reqnum) {
		HttpServerHandler.reqnum = reqnum;
	}
	public Map<String, String> getMap() {
		return map;
	}
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	public HttpPostRequestDecoder getDecoderFile() {
		return decoderFile;
	}
	public void setDecoderFile(HttpPostRequestDecoder decoderFile) {
		this.decoderFile = decoderFile;
	}
	public LogsBean getLogsBean() {
		return logsBean;
	}
	public void setLogsBean(LogsBean logsBean) {
		this.logsBean = logsBean;
	}
	public HttpDataFactory getFactory() {
		return factory;
	}
	
	public boolean getSystemConfigs(String path,ChannelHandlerContext ctx){
		if(path.equals("/systemRefreshStatic")){
			StaticFileCache.getStaticFile();
			ControllerProxy.INSTANCE().sendData(ctx.channel(), Unpooled.wrappedBuffer("success".getBytes(Charset.forName("utf-8"))));
			return true;//测试用
		}
		if(path.equals("/getSystemConfigHttp")){
			Object o = GetAndSetConfig.INSTANCE.gets("h");
			String s = GsonBean.gson.toJson(o);
			ControllerProxy.INSTANCE().sendData(ctx.channel(), Unpooled.wrappedBuffer(s.getBytes(Charset.forName("utf-8"))));
			return true;
		}
		if(path.equals("/getSystemConfigWebSocket")){
			Object o = GetAndSetConfig.INSTANCE.gets("w");
			String s = GsonBean.gson.toJson(o);
			ControllerProxy.INSTANCE().sendData(ctx.channel(), Unpooled.wrappedBuffer(s.getBytes(Charset.forName("utf-8"))));
			return true;
		}
		if(path.equals("/getSystemConfigServer")){
			Object o = GetAndSetConfig.INSTANCE.gets("s");
			String s = GsonBean.gson.toJson(o);
			ControllerProxy.INSTANCE().sendData(ctx.channel(), Unpooled.wrappedBuffer(s.getBytes(Charset.forName("utf-8"))));
			return true;
		}
		return false;
	}
 
    
    
}
