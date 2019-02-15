package com.wolf.javabean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WebSocketGroup {

	private List<WebSocketBean> list = new ArrayList<WebSocketBean>();
	private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(false);
	
	public void addGroup(WebSocketBean bean){
		rwLock.writeLock().lock();
		try{
			list.add(bean);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			rwLock.writeLock().unlock();
		}
	}
	
	public void remove(WebSocketBean bean){
		rwLock.writeLock().lock();
		try{
			list.remove(bean);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			rwLock.writeLock().unlock();
		}
	}
	
	public void sendWebsocketMessage(String message){//当前是给组提交String类型
		rwLock.readLock().lock();
		try{
			for(WebSocketBean bean : list){
				WebSocketMessage wsMessage = new WebSocketMessage();
				wsMessage.setType("String");
				wsMessage.setMessage(message);
				bean.send(wsMessage);//发送message对象
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			rwLock.readLock().unlock();
		}
	}
	
	public void sendWebsocketMessage(){
		rwLock.readLock().lock();
		try{
			for(WebSocketBean bean : list){
				bean.send();//发送message对象
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			rwLock.readLock().unlock();
		}
	}
	
}
