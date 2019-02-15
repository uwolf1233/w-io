package com.wolf.role.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.wolf.javabean.LogsBean;
import com.wolf.javabean.StaticBean;
import com.wolf.role.javaBean.Datas;
import com.wolf.role.javaBean.Permi;
import com.wolf.role.javaBean.Roles;
import com.wolf.role.javaBean.Users;
import com.wolf.role.locks.AllLocks;

public class RolesService {

	public Object getUsers(){
		AllLocks.usersRwLock.readLock().lock();
		AllLocks.rolesRwLock.readLock().lock();
		AllLocks.permisRwLock.readLock().lock();
		AllLocks.datasRwLock.readLock().lock();
		try{
			List<Users> usersList = Users.usersList;
			return StaticBean.gson.toJson(usersList);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.usersRwLock.readLock().unlock();
			AllLocks.rolesRwLock.readLock().unlock();
			AllLocks.permisRwLock.readLock().unlock();
			AllLocks.datasRwLock.readLock().unlock();
		}
	}
	
	public Object getRole(){
		AllLocks.rolesRwLock.readLock().lock();
		try{
			return StaticBean.gson.toJson(Roles.rolesSet);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.rolesRwLock.readLock().unlock();
		}
	}
	
	public Object getPermi(){
		AllLocks.permisRwLock.readLock().lock();
		try{
			return StaticBean.gson.toJson(Permi.permiSet);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.permisRwLock.readLock().unlock();
		}
	}
	
	public Object getDatas(){
		AllLocks.datasRwLock.readLock().lock();
		try{
			return StaticBean.gson.toJson(Datas.datasList);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.datasRwLock.readLock().unlock();
		}
	}
	
	public Object getRoleDatas(String id){
		AllLocks.rolesRwLock.readLock().lock();
		try{
			int len = Roles.rolesSet.size();
			Set<Datas> datas = null;
			for(int i=0;i<len;i++){
				if(Roles.rolesSet.get(i).getId().equals(id)){
					datas = Roles.rolesSet.get(i).getDatas();
					break;
				}
			}
			return StaticBean.gson.toJson(datas);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.rolesRwLock.readLock().unlock();
		}
	}
	
	public Object getUsersPermi(String id){
		AllLocks.usersRwLock.readLock().lock();
		try{
			int len = Users.usersList.size();
			Set<Permi> permis = null;
			for(int i=0;i<len;i++){
				if(Users.usersList.get(i).getId().equals(id)){
					permis = Users.usersList.get(i).getPermis();
					break;
				}
			}
			return StaticBean.gson.toJson(permis);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.usersRwLock.readLock().unlock();
		}
	}
	
	public Object getUsersDatas(String id){
		AllLocks.usersRwLock.readLock().lock();
		try{
			int len = Users.usersList.size();
			Set<Datas> datas = null;
			for(int i=0;i<len;i++){
				if(Users.usersList.get(i).getId().equals(id)){
					datas = Users.usersList.get(i).getDatas();
					break;
				}
			}
			return StaticBean.gson.toJson(datas);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			AllLocks.usersRwLock.readLock().unlock();
		}
	}
	
	public Object addRole(LogsBean logsBean,String name){
		String id = addRoleFromDB(name);
		Map<String,Object> map = new HashMap<String,Object>();
		if(id == null || id.trim().equals("")){
			map.put("code", "0");
			map.put("msg", "addError");
			return map;
		}
		Roles roles = new Roles(logsBean,name);
		roles.setId(id);
		map.put("code", "1");
		map.put("data", roles);
		return map;
	}
	
	public String addRoleFromDB(String name){
		return UUID.randomUUID().toString().replace("-", "");//临时
	}
	
	public Object userAddRole(String userids,String roleids){
		Map<String,Object> map = new HashMap<String,Object>();
		if(Users.userAddRole(userids, roleids)){
			map.put("code", "1");
			map.put("datas", getUsers());
		}else{
			map.put("code","0");
			map.put("msg", "error");
		}
		return map;
	}
	
	public boolean userAddRoleDB(String userids,String roleids){
		return true;
	}
	
	public Object roleAddPermi(String roleids,String permiids){
		Map<String,Object> map = new HashMap<String,Object>();
		if(Roles.roleAddPermi(roleids, permiids)){
			map.put("code", "1");
			map.put("datas", getUsers());
		}else{
			map.put("code","0");
			map.put("msg", "error");
		}
		return map;
	}
	
	public boolean roleAddPermiDB(String roleids,String permiids){
		return true;
	}
	
	public static void loadRolesFromDB(){
		
	}
	
}
