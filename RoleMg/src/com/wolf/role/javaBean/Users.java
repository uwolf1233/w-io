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
import com.wolf.javabean.ReqResBean;
import com.wolf.role.locks.AllLocks;

public class Users {

	private String id;
	private String name;
	
	private Set<Roles> roles;
	private Set<Permi> permis;
	private Set<Datas> datas;
	//private LogsBean logsBean;
	
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
	public Set<Roles> getRoles() {
		return roles;
	}
	public void setRoles(Set<Roles> roles) {
		this.roles = roles;
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
	
	/**
	 * 如果已经有roles，可以用这个
	 * @param permis
	 * @param datas
	 */
	public void handle(Set<Permi> tpermis){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			this.datas = new HashSet<Datas>();
			if(roles!=null && tpermis!=null && tpermis.size()>0){
				Iterator<Roles> rolesIt = roles.iterator();
				while(rolesIt.hasNext()){
					Roles roles = rolesIt.next();
					Set<Permi> cdatas = roles.getPermis();
					tpermis.addAll(cdatas);
				}
			}
			this.permis = tpermis;
			if(permis!=null){
				Iterator<Permi> permiIt = permis.iterator();
				while(permiIt.hasNext()){
					Permi permi = permiIt.next();
					Set<Datas> pdatas = permi.getDatas();
					datas.addAll(pdatas);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	/**
	 * 无锁刷新,先清理
	 */
	public void handle_unlock(){
		try{
			if(datas == null){
				datas = new HashSet<Datas>();
			}
			datas.clear();
			if(permis == null){
				permis = new HashSet<Permi>();
			}
			permis.clear();
			if(roles!=null && permis!=null){
				Iterator<Roles> rolesIt = roles.iterator();
				while(rolesIt.hasNext()){
					Roles roles = rolesIt.next();
					Set<Permi> cdatas = roles.getPermis();
					permis.addAll(cdatas);
				}
			}
			if(permis!=null && permis.size()>0){
				Iterator<Permi> permiIt = permis.iterator();
				while(permiIt.hasNext()){
					Permi permi = permiIt.next();
					Set<Datas> pdatas = permi.getDatas();
					datas.addAll(pdatas);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
	}
	
	/**
	 * 如果已经有permis,可以使用这个
	 * @param datas
	 */
	public void datasHandle(){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			this.datas = new HashSet<Datas>();
			if(permis!=null){
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
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	public static List<Users> usersList = new ArrayList<Users>();
	
	public Users(String name,LogsBean logsBean){
		//this.logsBean = logsBean;
		AllLocks.usersRwLock.writeLock().lock();
		try {
			usersList.add(this);
			this.name = name;
			logsBean.send("用户"+name+"已加入", "1", false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logsBean.send("用户"+name+"加入失败", "0", false);
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public Users(){
		AllLocks.usersRwLock.writeLock().lock();
		try {
			usersList.add(this);
			//logsBean.send("用户"+name+"已加入", "1", false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//logsBean.send("用户"+name+"加入失败", "0", false);
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static Map<String,Object> getRolsMg(String id,ReqResBean rrb){
		rrb.log("用户id:"+id+"获取角色及权限数据", "1");
		AllLocks.usersRwLock.readLock().lock();
		Map<String,Object> map = new HashMap<String,Object>();
		try{
			boolean hasUser = false;
			for(Users users : usersList){
				if(users.getId().equals(id)){
					map.put("roles", users.getRoles());
					map.put("permis", users.getPermis());
					map.put("datas", users.getDatas());
					hasUser = true;
					break;
				}
			}
			if(!hasUser){
				//尝试数据库获取，--db--
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.readLock().unlock();
		}
		
		return map;
	}
	
	public static boolean removeUserFromName(String name){
		AllLocks.usersRwLock.writeLock().lock();
		boolean flag = false;
		try{
			for(Users users : usersList){
				if(users.getName().equals("name")){
					flag = usersList.remove(users);
					if(flag){
						//users.logsBean.send("用户"+users.name+"删除成功", "1", false);
					}else{
						//users.logsBean.send("用户"+users.name+"删除失败", "0", false);
					}
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			flag = false;
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
		return flag;
	}
	
	public static void removeRole(Roles role){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getRoles().remove(role);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static void removeRole(List<Roles> role){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getRoles().removeAll(role);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static void removePermi(Permi permi){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getPermis().remove(permi);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static void removePermi(List<Permi> permi){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getPermis().removeAll(permi);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static void removeDatas(Datas datas){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getDatas().remove(datas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static void removeDatas(List<Datas> datas){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getDatas().removeAll(datas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static void removePermis(List<Permi> permis){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				users.getPermis().removeAll(permis);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
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
		Users other = (Users) obj;
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
	
	public void update(Users user){
		AllLocks.usersRwLock.writeLock().lock();
		try{
			for(Users users : usersList){
				if(users.getId().equals(user.getId())){
					users.setName(name);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
		}
	}
	
	public static boolean userAddRole(String userids,String roleids){
		AllLocks.usersRwLock.writeLock().lock();
		AllLocks.rolesRwLock.writeLock().lock();
		try{
			String useridArray[] = userids.split(",");
			List<Users> cuserList = new ArrayList<Users>();
			String roleidsArray[] = roleids.split(",");
			Set<Roles> rolesList = new HashSet<Roles>();
			int allUserLen = Users.usersList.size();
			int useridArrayLen = useridArray.length;
			for(int i=0;i<allUserLen;i++){
				for(int z=0;z<useridArrayLen;z++){
					if(Users.usersList.get(i).equals(useridArray[z])){
						cuserList.add(Users.usersList.get(i));
					}
				}
			}
			int allRoleLen = Roles.rolesSet.size();
			int roleidsArrayLen = roleidsArray.length;
			for(int i=0;i<allRoleLen;i++){
				for(int z=0;z<roleidsArrayLen;z++){
					if(Roles.rolesSet.get(i).equals(roleidsArray[z])){
						rolesList.add(Roles.rolesSet.get(i));
					}
				}
			}
			int cuserListSize = cuserList.size();
			for(int i=0;i<cuserListSize;i++){
				Users user = cuserList.get(i);
				user.getRoles().clear();
				user.getRoles().addAll(rolesList);
				user.handle_unlock();
			}
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			AllLocks.usersRwLock.writeLock().unlock();
			AllLocks.rolesRwLock.writeLock().unlock();
		}
	}
	
}
