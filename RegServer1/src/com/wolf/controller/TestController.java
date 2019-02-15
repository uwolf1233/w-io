package com.wolf.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.wolf.cache.SendToProxyCache;
import com.wolf.javabean.FileBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SystemNet.HttpServletRequest;
import com.wolf.javabean.SystemNet.HttpServletResponse;
import com.wolf.javabean.SystemNet.HttpSession;
import com.wolf.server.MethodMapping;
import com.wolf.serverLine.ServerRequestBean;

public class TestController {
	
	@MethodMapping(path="/testa1")//基本测试,全局负载均衡
	public void testa1(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1111");
		rrb.setResponseKV("aa", "aa1_testa1");
	}
	
	@MethodMapping(path="/testa2",threadPoolNumMax=20)//基本测试,路径负载均衡
	public void testa2(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		rrb.setResponseKV("aa", "aa1_testa2");
	}
	
	@MethodMapping(path="/testa3",threadPoolNumMax=30,interfacetype="h")//基本测试,路径负载均衡,调用本地方法
	public void testa3(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			rrb.setResponseKV("aa", "aa1_testa3");
			Map<String,String> map = new HashMap<String,String>();
			map.put("server1message", "server1 say test2");
			ServerRequestBean srb = new ServerRequestBean();
			srb.send("/testa4", map, false,rrb);
			System.out.println(123);
			Object o = srb.send("/testa5", map, true,rrb);
			System.out.println("testa5==="+o);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/testa4",threadPoolNumMax=40)//基本测试,路径负载均衡,调用本地方法，被调用，异步
	public void testa4(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		System.out.println("requestData===="+rrb.getRequestMap());
		rrb.setResponseKV("aa", "aa1_testa4");
	}
	
	@MethodMapping(path="/testa5",threadPoolNumMax=50)//基本测试,路径负载均衡,调用本地方法，被调用，同步
	public void testa5(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		System.out.println("requestData===="+rrb.getRequestMap());
		rrb.setResponseKV("aa", "aa1_testa5");
	}
	
	@MethodMapping(path="/testa6",threadPoolNumMax=70,trans=true,lockNames="testa6")//基本测试,路径负载均衡,调用本地方法,事务
	public void testa6(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			Thread.sleep(5000);
			System.out.println("server1");
			rrb.setResponseKV("aa", "aa1_testa6");
			Map<String,String> map = new HashMap<String,String>();
			map.put("server1message", "server1 say test2");
			ServerRequestBean srb = new ServerRequestBean();
			Object o = srb.send("/testb7", map, true,rrb);
			System.out.println("testb7==="+o);
			rrb.thransThread(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/testa7",threadPoolNumMax=80,trans=true,interfacetype="h")//基本测试,路径负载均衡,调用本地方法，事务，被调用，同步
	public void testa7(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1_testa7");
		System.out.println("requestData===="+rrb.getRequestMap());
		rrb.setResponseKV("aa", "aa1_testa7");
		rrb.thransThread(1);
	}
	
	@MethodMapping(path="/testa8",lockNames="testa8")//基本测试,路径负载均衡,锁
	public void testa8(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			System.out.println("requestData===="+rrb.getRequestMap());
			rrb.setResponseKV("aa", "aa1_testa8");
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/test1file")//基本测试,路径负载均衡,锁
	public String test1file(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println(123);
		return "/Test/Test";
	}
	
	@MethodMapping(path="/ajaxtest")//基本测试,路径负载均衡,锁
	public void ajaxtest(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("ajaxtest");
	}
	
	@MethodMapping(path="/test1",interfacetype="h")//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		return "/Test/Test";
	}
	
	@MethodMapping(path="/fileuptest1",hasFile=true,fileIn=true,interfacetype="h")//文件上传测试
	public void fileuptest1(ReqResBean rrb,Map<String,String> sessionMap){
		String tempPath = "F:/fileUpTest/";//临时存储目录，可自己定义
		if(rrb.getFileBean()!=null && rrb.getFileBean().size()>0){
			List<FileBean> fileList = rrb.getFileBean();
			for(FileBean fb : fileList){
				String fileNames = fb.getFileName()+"."+fb.getHz();
				FileOutputStream fos = null;
				try{
					File file = new File(tempPath+fileNames);
					fos = new FileOutputStream(file);
					fos.write(fb.getFiledata());
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					try {
						if(fos != null){
							fos.flush();
							fos.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		rrb.setResponseKV("type", "success");
	}
	
	@MethodMapping(path="/test2cookie")//基本测试,cookie
	public void test2cookie(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		rrb.addCookie("abcd","1234");
		rrb.addCookie("efgh","5678");
	}
	
	@MethodMapping(path="/test2downfile")//基本测试,文件下载
	public void test2downfile(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		rrb.outFile("F:/fileUpTest/1/jdbcTemplate手动式事务.txt");
	}
	
	
	@MethodMapping(path="/testa3file",threadPoolNumMax=30)//基本测试,路径负载均衡,调用本地方法发送文件，文件交换
	public void testa3file(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			rrb.setResponseKV("aa", "aa1_testa3file");
			String tempPath = "F:/fileUpTest/1/";
			rrb.addServerOutFileBean(new File("F:/fileUpTest/1/jdbcTemplate手动式事务.txt"));
			rrb.addServerOutFileBean(new File("F:/fileUpTest/1/进销存架构设计图.png"));
			Map<String,String> map = new HashMap<String,String>();
			map.put("server1message", "server1 say test2");
			ServerRequestBean srb = new ServerRequestBean();
			Object o = srb.send("/testa4file", map, true,rrb);
			if(o instanceof Map){//主要为了判断是否有文件
				Map<String,Object> maps = (Map<String,Object>)o;
				if(maps.containsKey("$fileBeans")){
					List<FileBean> FileBeanList = (List<FileBean>)maps.get("$fileBeans");
					for(FileBean fb : FileBeanList){
						String fileNames = fb.getFileName()+"."+fb.getHz();
						FileOutputStream fos = null;
						try{
							File file = new File(tempPath+fileNames);
							fos = new FileOutputStream(file);
							fos.write(fb.getFiledata());
						}catch(Exception e){
							e.printStackTrace();
						}finally{
							try {
								if(fos != null){
									fos.flush();
									fos.close();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
//			Object o = srb.send("/testa5file", map, true,rrb);
//			System.out.println("testa5==="+o);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/testa4file",threadPoolNumMax=40)//基本测试,路径负载均衡,调用本地方法，被调用，文件交换
	public void testa4file(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		System.out.println("requestData===="+rrb.getRequestMap());
		List<FileBean> FileBeanList = rrb.getServerInFileBean();
		String tempPath = "F:/fileUpTest/2/";
		File[] listFile = new File(tempPath).listFiles();
		rrb.addServerResponseFile(listFile);
		for(FileBean fb : FileBeanList){
			String fileNames = fb.getFileName()+"."+fb.getHz();
			FileOutputStream fos = null;
			try{
				File file = new File(tempPath+fileNames);
				fos = new FileOutputStream(file);
				fos.write(fb.getFiledata());
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try {
					if(fos != null){
						fos.flush();
						fos.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		rrb.setResponseKV("aa", "aa1_testa4file");
	}
	
	@MethodMapping(path="/test1webSocket")//基本测试,websocket
	public void test1webSocket(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("当前是test1webSocket");
		System.out.println("接收到的消息---"+rrb.getWebSocketBean().getInMessage());
		rrb.sendWebSocket(rrb.getWebSocketGroup(rrb.getWebSocketBean()),"websocket1 group test");//按组发
		rrb.sendWebSocket(rrb.getWebSocketBean(), "websocket1 bean test");//单个发
	}
	
	@MethodMapping(path="/test2webSocket")//基本测试,websocket
	public void test2webSocket(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("当前是test2webSocket");
		System.out.println("接收到的消息---"+rrb.getWebSocketBean().getInMessage());
		rrb.sendWebSocket(rrb.getWebSocketGroup(rrb.getWebSocketBean()),"websocket2 group test");//按组发
		rrb.sendWebSocket(rrb.getWebSocketBean(), "websocket2 bean test");//单个发
	}
	
	@MethodMapping(path="/cacheTest1",threadPoolNumMax=40,interfacetype="h")//缓存测试
	public void cacheTest1(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1");
		List<Map<String,String>> dataLm = new ArrayList<Map<String,String>>();
		for(int i=0;i<10000;i++){
			Map<String,String> map = new HashMap<String,String>();
			map.put("aa", "123是不是");
			map.put("bb", "456");
			dataLm.add(map);
		}
		Gson gson = new Gson();
		Map<String,Object> map = new HashMap<String,Object>();
		String data = gson.toJson(dataLm);
		rrb.setResponseKV("cacheTest1data", data);
		map.put("cacheTest1data", data);
		String datas = gson.toJson(map);
		SendToProxyCache.send(datas, "/cacheTest1");//执行完返回数据后，执行一次缓存主推
	}
	
	@MethodMapping(path="/cacheTest2",threadPoolNumMax=30,interfacetype="h")//基本测试,调用本地方法，缓存
	public void cacheTest2(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			ServerRequestBean srb = new ServerRequestBean();
			Map<String,String> map = new HashMap<String,String>();
			Object o = srb.send("/cacheTest3", map, true,rrb);//肯定是同步方法才会需要缓存
			rrb.setResponseKV("cacheTest2data", o.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/cacheTest3",threadPoolNumMax=30,interfacetype="s")//基本测试,调用本地方法，缓存
	public void cacheTest3(ReqResBean rrb,Map<String,String> sessionMap){
		try {
			System.out.println("server1");
			List<Map<String,String>> dataLm = new ArrayList<Map<String,String>>();
			for(int i=0;i<10000;i++){
				Map<String,String> map = new HashMap<String,String>();
				map.put("aa", "123");
				map.put("bb", "456");
				dataLm.add(map);
			}
			Gson gson = new Gson();
			String data = gson.toJson(dataLm);
			rrb.setResponseKV("data", data);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("data", dataLm);
			String datas = gson.toJson(map);
			SendToProxyCache.send(datas, "/cacheTest3");//执行完返回数据后，执行一次缓存主推
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@MethodMapping(path="/testlog",interfacetype="h")
	public void testlog(ReqResBean rrb,Map<String,String> sessionMap){
		System.out.println("server1_testlog");
		rrb.log("success", "1");
		rrb.log("error", "0");
	}
	
}
