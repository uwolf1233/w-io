package com.wolf.role.javaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.wolf.javabean.ReqResBean;
import com.wolf.role.lock.Locks;

public class RoleBean {

	private String id;
	private String name;
	private String systemname;
	private String pid;
	private String isend;
	
	private static Set<RoleBean> roleBeanSet = new HashSet<RoleBean>();
	
	public static void clean(){
		roleBeanSet.clear();
	}
	
	public static String getAllRole(ReqResBean rrb){
		rrb.log("获取所有角色数据", "1");
		boolean b = false;
		try {
			b = Locks.rolewrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取角色加锁失败", "0");
		}
		try{
			return StaticBeans.gson.toJson(roleBeanSet);
		}catch(Exception e){
			rrb.log("获取所有角色数据失败", "0");
			return "";
		}finally{
			if(b){
				Locks.rolewrLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * 根据id组获取当前类相关对象
	 * @param ids
	 * @param rrb
	 * @return
	 */
	public static RoleBean[] getFromIds(String[] ids,ReqResBean rrb){
		rrb.log("获取角色", "1");
		boolean b = false;
		try {
			b = Locks.rolewrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取角色加锁失败", "0");
		}
		try{
			if(roleBeanSet.size()>0 && b){
				Iterator<RoleBean> it = roleBeanSet.iterator();
				List<RoleBean> list = new ArrayList<RoleBean>();
				itfor:
				while(it.hasNext()){
					RoleBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							list.add(pb);
							continue itfor;
						}
					}
				}
				RoleBean[] s = new RoleBean[list.size()];
				rrb.log("获取到的角色长度"+s.length, "1");
				return list.toArray(s);
			}else{
				rrb.log("获取角色失败", "0");
				return null;
			}
		}catch(Exception e){
			rrb.log("获取角色失败", "0");
			return null;
		}finally{
			if(b){
				Locks.rolewrLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * 根据id组获取当前类相关对象json组
	 * @param ids
	 * @param rrb
	 * @return
	 */
	public static String getJsonFromIds(String[] ids,ReqResBean rrb){
		rrb.log("获取角色", "1");
		boolean b = false;
		try {
			b = Locks.rolewrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取角色加锁失败", "0");
		}
		try{
			if(roleBeanSet.size()>0 && b){
				Iterator<RoleBean> it = roleBeanSet.iterator();
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				itfor:
				while(it.hasNext()){
					RoleBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("id", pb.getId());
							map.put("name", pb.getName());
							list.add(map);
							continue itfor;
						}
					}
				}
				rrb.log("获取到的角色长度"+list.size(), "1");
				return StaticBeans.gson.toJson(list);
			}else{
				return "";
			}
		}catch(Exception e){
			rrb.log("获取角色失败", "0");
			return "";
		}finally{
			if(b){
				Locks.rolewrLock.readLock().unlock();
			}
		}
	}
	
	public RoleBean(){
		roleBeanSet.add(this);
	}
	
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
	public String getSystemname() {
		return systemname;
	}
	public void setSystemname(String systemname) {
		this.systemname = systemname;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getIsend() {
		return isend;
	}
	public void setIsend(String isend) {
		this.isend = isend;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((isend == null) ? 0 : isend.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
		result = prime * result
				+ ((systemname == null) ? 0 : systemname.hashCode());
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
		RoleBean other = (RoleBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isend == null) {
			if (other.isend != null)
				return false;
		} else if (!isend.equals(other.isend))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pid == null) {
			if (other.pid != null)
				return false;
		} else if (!pid.equals(other.pid))
			return false;
		if (systemname == null) {
			if (other.systemname != null)
				return false;
		} else if (!systemname.equals(other.systemname))
			return false;
		return true;
	}
	
	/**
	 * 根据id组获取当前类相关对象
	 * @param ids
	 * @param rrb
	 * @return
	 */
	public static String getMapFromIds(String[] ids,ReqResBean rrb){
		rrb.log("获取部门", "1");
		boolean b = false;
		try {
			b = Locks.rolewrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取角色加锁失败", "0");
		}
		try{
			if(roleBeanSet.size()>0 && b){
				Iterator<RoleBean> it = roleBeanSet.iterator();
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				itfor:
				while(it.hasNext()){
					RoleBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							list.add(toMap(pb));
							continue itfor;
						}
					}
				}
				RoleBean[] s = new RoleBean[list.size()];
				rrb.log("获取到的角色长度"+s.length, "1");
				return StaticBeans.gson.toJson(list);
			}else{
				rrb.log("获取角色失败", "0");
				return "";
			}
		}catch(Exception e){
			rrb.log("获取角色失败", "0");
			return "";
		}finally{
			if(b){
				Locks.rolewrLock.readLock().unlock();
			}
		}
	}
	
	public static Map<String,Object> toMap(RoleBean roleBean){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", roleBean.getId());
		map.put("name", roleBean.getName());
		map.put("systemname", roleBean.getSystemname());
		map.put("pid", roleBean.getPid());
		map.put("isend", roleBean.getIsend());
		return map;
	} 
	
}
