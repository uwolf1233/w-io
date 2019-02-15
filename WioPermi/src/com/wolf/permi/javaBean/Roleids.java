package com.wolf.permi.javaBean;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.wolf.permi.dao.PermiDao;
import com.wolf.permi.lock.Locks;

public class Roleids implements IdMoneyToMoney{

	public String id;
	public String[] permiids;
	public PermiBean[] permiBean;
	
	private static Set<Roleids> roleidsSet = new HashSet<Roleids>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
		Roleids other = (Roleids) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String[] getPermiids() {
		return permiids;
	}
	public void setPermiids(String[] permiids) {
		this.permiids = permiids;
		
	}
	
	public void setIds(String ids[]){
		this.permiids = ids;
	}
	public PermiBean[] getPermiBean() {
		return permiBean;
	}
	public void setPermiBean(PermiBean[] permiBean) {
		this.permiBean = permiBean;
	}
	@Override
	public void findObj() {//填充permiBean数组
		// TODO Auto-generated method stub
		if(id != null && permiids != null && permiids.length > 0){
			permiBean = PermiBean.getFromIds(permiids);
		}
	}
	
}
