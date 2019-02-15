package com.wolf.role.javaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.javabean.LogsBean;
import com.wolf.role.locks.AllLocks;

public class Roles {

	private String id;
	private String name;
	
	private Set<Permi> permis;
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
	public Set<Permi> getPermis() {
		return permis;
	}
	public void setPermis(Set<Permi> permis) {
		this.permis = permis;
	}
	public Set<Datas> getDatas() {
		return datas;
	}
	public void setDatas(Set<Datas> datas) {
		this.datas = datas;
	}
	
	public static List<Roles> rolesSet = new ArrayList<Roles>();
	
	public Roles(){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			rolesSet.add(this);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	public Roles(LogsBean logsBean,String name){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			this.name = name;
			rolesSet.add(this);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	/**
	 * 如果已经有permis，可以使用这个
	 * @param datas
	 */
	public void datasHandle(){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			if(permis!=null && datas !=null){
				Iterator<Permi> permisIt = permis.iterator();
				while(permisIt.hasNext()){
					Permi permi = permisIt.next();
					Set<Datas> cdatas = permi.getDatas();
					datas.addAll(cdatas);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	/**
	 * 无锁刷新，先清理
	 */
	public void datasHandle_unlock(){
		if(permis!=null){
			if(datas == null){
				datas = new HashSet<Datas>();
			}
			datas.clear();
			Iterator<Permi> permisIt = permis.iterator();
			while(permisIt.hasNext()){
				Permi permi = permisIt.next();
				Set<Datas> cdatas = permi.getDatas();
				datas.addAll(cdatas);
			}
		}
	}
	
	public static Map<String,Object> getPermisDatas(String id){
		Map<String,Object> map = null;
		AllLocks.rolesRwLock.readLock().lock();
		try{
			map = new HashMap<String,Object>();
			for(Roles roles : rolesSet){
				if(roles.getId().equals(id)){
					map.put("permis", roles.getPermis());
					map.put("datas", roles.getDatas());
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.readLock().unlock();
		}
		return map;
	}
	
	public static boolean removeRoleFromName(String name){
		boolean flag = false;
		AllLocks.rolesRwLock.readLock().lock();
		try{
			for(Roles roles : rolesSet){
				if(roles.equals(name)){
					flag = rolesSet.remove(roles);
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			flag = false;
		}finally{
			AllLocks.rolesRwLock.readLock().unlock();
		}
		return flag;
	}
	
	public static void removePermi(Permi permi){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			for(Roles roles : rolesSet){
				roles.getPermis().remove(permi);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	public static void removePermi(List<Permi> permi){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			for(Roles roles : rolesSet){
				roles.getPermis().removeAll(permi);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	public static void removeDatas(Datas datas){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			for(Roles roles : rolesSet){
				roles.getDatas().remove(datas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	public static void removeDatas(List<Datas> datas){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			for(Roles roles : rolesSet){
				roles.getDatas().removeAll(datas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
	public static void update(Roles role){
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			for(Roles roles : rolesSet){
				if(roles.getId().equals(role)){
					roles.setName(role.getName());
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
		}
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
		Roles other = (Roles) obj;
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
	
	public static void deletePermi(String[] ids,String id){
		AllLocks.rolesRwLock.writeLock().lock();
		AllLocks.usersRwLock.writeLock().lock();
		try{
			Roles croles = null;
			for(Roles roles : rolesSet){
				if(roles.getId().equals(id)){
					croles = roles;
					break;
				}
			}
			List<Permi> removePermis = new ArrayList<Permi>();
			for(Permi cpermi : croles.getPermis()){
				for(String cid : ids){
					if(cpermi.getId().equals(cid)){
						removePermis.add(cpermi);
					}
				}
			}
			croles.getPermis().removeAll(removePermis);
			int usersLen = Users.usersList.size();
			for(int i=0;i<usersLen;i++){
				Users.usersList.get(i).handle_unlock();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	/**
	 * 先清理
	 * @param datasList
	 * @param id
	 */
	public static void add(List<Permi> permisList,String id){
		AllLocks.rolesRwLock.writeLock().lock();
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Roles role : rolesSet){
				if(role.getId().equals(id)){
					role.getPermis().clear();
					role.getPermis().addAll(permisList);
				}
			}
			int usersLen = Users.usersList.size();
			for(int i=0;i<usersLen;i++){
				Users.usersList.get(i).handle_unlock();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.rolesRwLock.writeLock().unlock();
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static boolean roleAddPermi(String roleids,String permiids){
		AllLocks.usersRwLock.writeLock().lock();
		AllLocks.rolesRwLock.writeLock().lock();
		AllLocks.permisRwLock.writeLock().lock();
		try{
			String roleidsArray[] = roleids.split(",");
			List<Roles> croleList = new ArrayList<Roles>();
			String permiidsArray[] = permiids.split(",");
			Set<Permi> permiList = new HashSet<Permi>();
			int allRoleLen = Roles.rolesSet.size();
			int roleidArrayLen = roleidsArray.length;
			for(int i=0;i<allRoleLen;i++){
				for(int z=0;z<roleidArrayLen;z++){
					if(Roles.rolesSet.get(i).equals(roleidsArray[z])){
						croleList.add(Roles.rolesSet.get(i));
					}
				}
			}
			int allPermiLen = Permi.permiSet.size();
			int permiidsArrayLen = permiidsArray.length;
			for(int i=0;i<allPermiLen;i++){
				for(int z=0;z<permiidsArrayLen;z++){
					if(Permi.permiSet.get(i).equals(roleidsArray[z])){
						permiList.add(Permi.permiSet.get(i));
					}
				}
			}
			int croleListSize = croleList.size();
			for(int i=0;i<croleListSize;i++){
				Roles role = croleList.get(i);
				role.getPermis().clear();
				role.getPermis().addAll(permiList);
				role.datasHandle_unlock();
			}
			int allUserSize = Users.usersList.size();
			for(int i=0;i<allUserSize;i++){
				Users.usersList.get(i).handle_unlock();
			}
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
			AllLocks.rolesRwLock.writeLock().unlock();
			AllLocks.permisRwLock.writeLock().unlock();
		}
	}
	
}
