package com.wolf.role.javaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.role.locks.AllLocks;

public class Permi {

	private String id;
	private String name;
	
	private Set<Datas> datas;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Datas> getDatas() {
		return datas;
	}
	public void setDatas(Set<Datas> datas) {
		this.datas = datas;
	}
	
	public static List<Permi> permiSet = new ArrayList<Permi>();
	
	public Permi(){
		AllLocks.permisRwLock.writeLock().lock();
		try{
			permiSet.add(this);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.permisRwLock.writeLock().unlock();
		}
	}
	
	public static Map<String,Object> getDatas(String id){
		Map<String,Object> map = new HashMap<String,Object>();
		AllLocks.permisRwLock.readLock().lock();
		try{
			for(Permi permi : permiSet){
				if(permi.getId().equals(id)){
					map.put("datas", permi.getDatas());
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.permisRwLock.readLock().unlock();
		}
		return map;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Permi other = (Permi) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public static void removeDatas(Datas datas){
		AllLocks.permisRwLock.writeLock().lock();
		try{
			for(Permi permi : permiSet){
				permi.getDatas().remove(datas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.permisRwLock.writeLock().unlock();
		}
	}
	
	public static void removeDatas(List<Datas> datas){
		AllLocks.permisRwLock.writeLock().lock();
		try{
			for(Permi permi : permiSet){
				permi.getDatas().removeAll(datas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.permisRwLock.writeLock().unlock();
		}
	}
	
	public static void update(Permi permi){
		AllLocks.permisRwLock.writeLock().lock();
		try{
			for(Permi cpermi : permiSet){
				if(cpermi.getId().equals(permi.getId())){
					cpermi.setName(permi.getName());
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.permisRwLock.writeLock().unlock();
		}
	}
	
	public static void deleteDatas(String[] ids,String id){
		AllLocks.permisRwLock.writeLock().lock();
		AllLocks.rolesRwLock.writeLock().lock();
		AllLocks.usersRwLock.writeLock().lock();
		try{
			Permi cpermi = null;
			for(Permi permi : permiSet){
				if(permi.getId().equals(id)){
					cpermi = permi;
					break;
				}
			}
			List<Datas> removeDatas = new ArrayList<Datas>();
			for(Datas cdatas : cpermi.getDatas()){
				for(String cid : ids){
					if(cdatas.getId().equals(cid)){
						removeDatas.add(cdatas);
					}
				}
			}
			cpermi.getDatas().removeAll(removeDatas);
			int rolesLen = Roles.rolesSet.size();
			for(int i=0;i<rolesLen;i++){
				Roles.rolesSet.get(i).datasHandle_unlock();
			}
			int usersLen = Users.usersList.size();
			for(int i=0;i<usersLen;i++){
				Users.usersList.get(i).handle_unlock();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.permisRwLock.writeLock().unlock();
			AllLocks.rolesRwLock.writeLock().unlock();
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	/**
	 * 先清理
	 * @param datasList
	 * @param id
	 */
	public static boolean add(List<Datas> datasList,String id){
		boolean flag = false;
		AllLocks.permisRwLock.writeLock().lock();
		AllLocks.rolesRwLock.writeLock().lock();
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Permi permi : permiSet){
				if(permi.getId().equals(id)){
					permi.getDatas().clear();
					permi.getDatas().addAll(datasList);
				}
			}
			int rolesLen = Roles.rolesSet.size();
			for(int i=0;i<rolesLen;i++){
				Roles.rolesSet.get(i).datasHandle_unlock();
			}
			int usersLen = Users.usersList.size();
			for(int i=0;i<usersLen;i++){
				Users.usersList.get(i).handle_unlock();
			}
			flag = true;
		}catch(Exception e){
			e.printStackTrace();
			flag = false;
		}finally{
			AllLocks.permisRwLock.writeLock().unlock();
			AllLocks.rolesRwLock.writeLock().unlock();
			AllLocks.usersRwLock.writeLock().unlock();
		}
		return flag;
	}
	
	public static boolean removePermiFromName(String name){
		boolean flag = false;
		AllLocks.permisRwLock.readLock().lock();
		try{
			List<Permi> permis = new ArrayList<Permi>();
			for(Permi permi : permiSet){
				if(permi.equals(name)){
					flag = permiSet.remove(permi);
					if(flag){
						permis.add(permi);
					}
					break;
				}
			}
			Users.removePermis(permis);
			Roles.removePermi(permis);
			for(Roles role : Roles.rolesSet){
				role.datasHandle_unlock();
			}
			for(Users users : Users.usersList){
				users.handle_unlock();
			}
		}catch(Exception e){
			e.printStackTrace();
			flag = false;
		}finally{
			AllLocks.permisRwLock.readLock().unlock();
		}
		return flag;
	}
	
}
