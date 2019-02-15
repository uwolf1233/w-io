package com.wolf.dept.javaBean;

import java.util.HashSet;
import java.util.Set;

import com.wolf.dept.dao.DeptDao;

public class Userids implements IdMoneyToMoney{

	public String id;
	public String[] deptids;
	public DeptBean[] deptBean;
	
	private static Set<Userids> useridsSet = new HashSet<Userids>();
	
	public Userids(){
		useridsSet.add(this);
	}
	
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
		Userids other = (Userids) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String[] getDeptids() {
		return deptids;
	}
	public void setDeptids(String[] deptids) {
		this.deptids = deptids;
	}
	public DeptBean[] getDeptBean() {
		return deptBean;
	}
	public void setDeptBean(DeptBean[] deptBean) {
		this.deptBean = deptBean;
	}

	@Override
	public void findObj() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIds(String[] ids) {
		// TODO Auto-generated method stub
		
	}
	
}
