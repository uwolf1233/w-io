package com.wolf.dept.javaBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.wolf.dept.dao.DeptDao;
import com.wolf.dept.lock.Locks;
import com.wolf.javabean.ReqResBean;

public class DeptBean {

	private String id;
	private String name;
	private String depttype;
	private String deptproperty;
	private String level;
	private String systemname;
	private String pid;
	private String isend;
	private DeptTypeBean deptTypeBean;
	private DeptPropertyBean deptPropertyBean;
	
	private static Set<DeptBean> deptBeanSet = new HashSet<DeptBean>();
	
	public static Map<String,Object> toMap(DeptBean deptBean){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", deptBean.getId());
		map.put("name", deptBean.getName());
		map.put("depttype", deptBean.getDepttype());
		map.put("deptproperty", deptBean.getDeptproperty());
		map.put("systemname", deptBean.getSystemname());
		map.put("pid", deptBean.getPid());
		map.put("isend", deptBean.getIsend());
		return map;
	} 
	
	public static void clean(){
		deptBeanSet.clear();
	}
	
	public static String getAllRole(ReqResBean rrb){
		rrb.log("获取所有部门数据", "1");
		boolean b = false;
		try {
			b = Locks.deptwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取部门加锁失败", "0");
		}
		try{
			return StaticBeans.gson.toJson(deptBeanSet);
		}catch(Exception e){
			rrb.log("获取所有部门数据失败", "0");
			return "";
		}finally{
			if(b){
				Locks.deptwrLock.readLock().unlock();
			}
		}
	}
	
	/**
	 * 根据id组获取当前类相关对象
	 * @param ids
	 * @param rrb
	 * @return
	 */
	public static DeptBean[] getFromIds(String[] ids,ReqResBean rrb){
		rrb.log("获取部门", "1");
		boolean b = false;
		try {
			b = Locks.deptwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取部门加锁失败", "0");
		}
		try{
			if(deptBeanSet.size()>0 && b){
				Iterator<DeptBean> it = deptBeanSet.iterator();
				List<DeptBean> list = new ArrayList<DeptBean>();
				itfor:
				while(it.hasNext()){
					DeptBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							list.add(pb);
							continue itfor;
						}
					}
				}
				DeptBean[] s = new DeptBean[list.size()];
				rrb.log("获取到的部门长度"+s.length, "1");
				return list.toArray(s);
			}else{
				rrb.log("获取部门失败", "0");
				return null;
			}
		}catch(Exception e){
			rrb.log("获取部门失败", "0");
			return null;
		}finally{
			if(b){
				Locks.deptwrLock.readLock().unlock();
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
		rrb.log("获取部门", "1");
		boolean b = false;
		try {
			b = Locks.deptwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取部门加锁失败", "0");
		}
		try{
			if(deptBeanSet.size()>0 && b){
				Iterator<DeptBean> it = deptBeanSet.iterator();
				DeptBean deptBean = null;
				itfor:
				while(it.hasNext()){
					DeptBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							deptBean = pb;
							continue itfor;
						}
					}
				}
				if(deptBean == null){
					deptBean = new DeptBean();
				}
				return StaticBeans.gson.toJson(deptBean);
			}else{
				return "";
			}
		}catch(Exception e){
			rrb.log("获取部门失败", "0");
			return "";
		}finally{
			if(b){
				Locks.deptwrLock.readLock().unlock();
			}
		}
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
			b = Locks.deptwrLock.readLock().tryLock(3000, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			rrb.log("获取部门加锁失败", "0");
		}
		try{
			if(deptBeanSet.size()>0 && b){
				Iterator<DeptBean> it = deptBeanSet.iterator();
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				itfor:
				while(it.hasNext()){
					DeptBean pb = it.next();
					for(String id : ids){
						if(id.equals(pb.getId())){
							list.add(toMap(pb));
							continue itfor;
						}
					}
				}
				DeptBean[] s = new DeptBean[list.size()];
				rrb.log("获取到的部门长度"+s.length, "1");
				return StaticBeans.gson.toJson(list);
			}else{
				rrb.log("获取部门失败", "0");
				return "";
			}
		}catch(Exception e){
			rrb.log("获取部门失败", "0");
			return "";
		}finally{
			if(b){
				Locks.deptwrLock.readLock().unlock();
			}
		}
	}
	
	public DeptBean(){
		deptBeanSet.add(this);
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
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
		result = prime * result
				+ ((deptproperty == null) ? 0 : deptproperty.hashCode());
		result = prime * result
				+ ((systemname == null) ? 0 : systemname.hashCode());
		result = prime * result + ((depttype == null) ? 0 : depttype.hashCode());
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
		DeptBean other = (DeptBean) obj;
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
		if (level == null) {
			if (other.level != null)
				return false;
		} else if (!level.equals(other.level))
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
		if (deptproperty == null) {
			if (other.deptproperty != null)
				return false;
		} else if (!deptproperty.equals(other.deptproperty))
			return false;
		if (systemname == null) {
			if (other.systemname != null)
				return false;
		} else if (!systemname.equals(other.systemname))
			return false;
		if (depttype == null) {
			if (other.depttype != null)
				return false;
		} else if (!depttype.equals(other.depttype))
			return false;
		return true;
	}

	public DeptTypeBean getDeptTypeBean() {
		return deptTypeBean;
	}

	public void setDeptTypeBean(DeptTypeBean deptTypeBean) {
		this.deptTypeBean = deptTypeBean;
	}

	public DeptPropertyBean getDeptPropertyBean() {
		return deptPropertyBean;
	}

	public void setDeptPropertyBean(DeptPropertyBean deptPropertyBean) {
		this.deptPropertyBean = deptPropertyBean;
	}

	public String getDepttype() {
		return depttype;
	}

	public void setDepttype(String depttype) {
		this.depttype = depttype;
	}

	public String getDeptproperty() {
		return deptproperty;
	}

	public void setDeptProperty(String deptproperty) {
		this.deptproperty = deptproperty;
	}
	
	public static void setOtherData(){
		if(deptBeanSet.size()>0){
			Iterator<DeptBean> it = deptBeanSet.iterator();
			while(it.hasNext()){
				DeptBean deptBean = it.next();
				deptBean.setDeptType();
				deptBean.setDeptProperty();
			}
		}
	}
	
	public void setDeptType(){//设置部门类型
		Iterator<DeptTypeBean> dtbIt = DeptTypeBean.deptTypeSet.iterator();
		while(dtbIt.hasNext()){
			DeptTypeBean dtb = dtbIt.next();
			if(dtb.getId().equals(this.depttype)){
				this.deptTypeBean = dtb;
				break;
			}
		}
	}
	
	public void setDeptProperty(){//设置部门类型
		Iterator<DeptPropertyBean> dtbIt = DeptPropertyBean.deptPropertySet.iterator();
		while(dtbIt.hasNext()){
			DeptPropertyBean dtb = dtbIt.next();
			if(dtb.getId().equals(this.deptproperty)){
				this.deptPropertyBean = dtb;
				break;
			}
		}
	}
	
}
