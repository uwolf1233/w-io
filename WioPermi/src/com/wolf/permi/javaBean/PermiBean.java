package com.wolf.permi.javaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.wolf.javabean.ReqResBean;
import com.wolf.permi.lock.Locks;

public class PermiBean {

	private String id;
	private String name;
	private String systemname;
	private String pid;
	private String isend;
	
	private static Set<PermiBean> permiBeanSet = new HashSet<PermiBean>();
	
	public static String getAllPermi(ReqResBean rrb){
		rrb.log("获取所有权限数据", "1");
		boolean b = false;
		try {
			b = Locks.permiwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取权限加锁失败", "0");
		}
		try{
			return StaticBeans.jsonStringHandle(StaticBeans.gson.toJson(permiBeanSet));
		}catch(Exception e){
			rrb.log("获取所有权限数据失败", "0");
			return "";
		}finally{
			if(b){
				Locks.permiwrLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * 根据id组获取当前类相关对象
	 * @param ids
	 * @param rrb
	 * @return
	 */
	public static PermiBean[] getFromIds(String[] ids,ReqResBean rrb){
		rrb.log("获取权限", "1");
		boolean b = false;
		try {
			b = Locks.permiwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取权限加锁失败", "0");
		}
		try{
			if(permiBeanSet.size()>0 && b){
				Iterator<PermiBean> it = permiBeanSet.iterator();
				List<PermiBean> list = new ArrayList<PermiBean>();
				itfor:
				while(it.hasNext()){
					PermiBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							list.add(pb);
							continue itfor;
						}
					}
				}
				PermiBean[] s = new PermiBean[list.size()];
				rrb.log("获取到的权限长度"+s.length, "1");
				return list.toArray(s);
			}else{
				rrb.log("获取权限失败", "0");
				return null;
			}
		}catch(Exception e){
			rrb.log("获取权限失败", "0");
			return null;
		}finally{
			if(b){
				Locks.permiwrLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * 根据id组获取当前类相关对象
	 * @param ids
	 * @param rrb
	 * @return
	 */
	public static PermiBean[] getFromIds(String[] ids){
		boolean b = false;
		try {
			b = Locks.permiwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try{
			if(permiBeanSet.size()>0 && b){
				Iterator<PermiBean> it = permiBeanSet.iterator();
				List<PermiBean> list = new ArrayList<PermiBean>();
				itfor:
				while(it.hasNext()){
					PermiBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							list.add(pb);
							continue itfor;
						}
					}
				}
				PermiBean[] s = new PermiBean[list.size()];
				System.out.println("获取到的权限长度"+s.length);
				return list.toArray(s);
			}else{
				System.out.println("获取权限失败");
				return null;
			}
		}catch(Exception e){
			System.out.println("获取权限失败");
			return null;
		}finally{
			if(b){
				Locks.permiwrLock.readLock().unlock();
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
		rrb.log("获取权限", "1");
		boolean b = false;
		try {
			b = Locks.permiwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取权限加锁失败", "0");
		}
		try{
			if(permiBeanSet.size()>0 && b){
				Iterator<PermiBean> it = permiBeanSet.iterator();
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				itfor:
				while(it.hasNext()){
					PermiBean pb = it.next();
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
				rrb.log("获取到的权限长度"+list.size(), "1");
				return StaticBeans.gson.toJson(list);
			}else{
				return "";
			}
		}catch(Exception e){
			rrb.log("获取权限失败", "0");
			return "";
		}finally{
			if(b){
				Locks.permiwrLock.readLock().unlock();
			}
		}
	}
	
	public PermiBean(){
		permiBeanSet.add(this);
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
		PermiBean other = (PermiBean) obj;
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
	
}
