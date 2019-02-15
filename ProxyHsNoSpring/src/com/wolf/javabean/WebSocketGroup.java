package com.wolf.javabean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.wolf.locks.WebSocketLocks;

public class WebSocketGroup {

	private static List<WebSocketBean> group = null;
	private static Iterator<WebSocketBean> it = null;
	
	static{
		group = new ArrayList<WebSocketBean>();
		it = group.iterator();
	}
	
	//websocket添加入组
	public static void addGroup(WebSocketBean webSocketBean){
		WebSocketLocks.webSocketBeanLock.writeLock().lock();
		try{
			group.add(webSocketBean);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			WebSocketLocks.webSocketBeanLock.writeLock().unlock();
		}
	}
	
	//websocket从组中删除
	public static void removeGroup(String id){
		WebSocketLocks.webSocketBeanLock.writeLock().lock();
		try{
			while(it.hasNext()){
				WebSocketBean webSocketBean = it.next();
				if(webSocketBean.getId().equals(id)){
					it.remove();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			WebSocketLocks.webSocketBeanLock.writeLock().unlock();
		}
	}
	
	public void sendWebSocketMessage(String id,String message){//id组
		WebSocketLocks.webSocketBeanLock.readLock().lock();
		try{
			String[] idArray = id.split(",");
			List<String> idList = Arrays.asList(idArray);
			
			while(it.hasNext()){
				WebSocketBean webSocketBean = it.next();
				if(idList.contains(webSocketBean.getId())){
					//webSocketBean.sendMessage(message);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			WebSocketLocks.webSocketBeanLock.readLock().unlock();
		}
	}
	
}
