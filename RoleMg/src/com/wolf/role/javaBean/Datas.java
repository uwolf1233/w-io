package com.wolf.role.javaBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wolf.role.locks.AllLocks;

public class Datas {

	private String id;
	private Object data;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public static List<Datas> datasList = new ArrayList<Datas>();
	
	public Datas(){
		AllLocks.datasRwLock.writeLock().lock();
		try{
			datasList.add(this);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.datasRwLock.writeLock().unlock();
		}
	}
	
	public static void removeDatasFromId(String id){
		AllLocks.datasRwLock.writeLock().lock();
		try{
			Datas cdatas = null;
			for(Datas datas : datasList){
				if(datas.getId().equals(id)){
					cdatas = datas;
					break;
				}
			}
			datasList.remove(cdatas);
			Permi.removeDatas(cdatas);
			Roles.removeDatas(cdatas);
			Users.removeDatas(cdatas);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.datasRwLock.writeLock().unlock();
		}
	}
	
	public static boolean removeDatasFromId(List<String> ids){
		boolean flag = false;
		AllLocks.datasRwLock.writeLock().lock();
		try{
			List<Datas> cdatas = new ArrayList<Datas>();
			for(Datas datas : datasList){
				if(ids.contains(datas.getId())){
					cdatas.add(datas);
				}
			}
			datasList.remove(cdatas);
			Permi.removeDatas(cdatas);
			Roles.removeDatas(cdatas);
			Users.removeDatas(cdatas);
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
			flag = false;
		}finally{
			AllLocks.datasRwLock.writeLock().unlock();
		}
		return flag;
	}
	
	public static void update(List<Datas> datas){
		AllLocks.datasRwLock.writeLock().lock();
		try{
			for(Datas cdatas : datasList){
				for(Datas tdatas : datas){
					if(cdatas.getId().equals(tdatas.getId())){
						cdatas.setId(tdatas.getId());
						cdatas.setData(tdatas.getData());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.datasRwLock.writeLock().unlock();
		}
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public static List<Datas> getDatasList() {
		return datasList;
	}
	public static void setDatasList(List<Datas> datasList) {
		Datas.datasList = datasList;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Datas other = (Datas) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public static List<Datas> getFromIds(List<String> ids){
		List<Datas> datasList = new ArrayList<Datas>();
		AllLocks.datasRwLock.readLock().lock();
		try {
			allfor:
			for(Datas datas : datasList){
				for(String id : ids){
					if(datas.getId().equals(id)){
						datasList.add(datas);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			AllLocks.datasRwLock.readLock().unlock();
		}
		return datasList;
	}
	
}
