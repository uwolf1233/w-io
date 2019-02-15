package com.wolf.role.javaBean;

import java.util.HashSet;
import java.util.Set;

import com.wolf.role.dao.RoleDao;

public class Userids {

	public String id;
	public String[] roleids;
	public RoleBean[] roleBean;
	
	private static Set<Userids> useridsSet = new HashSet<Userids>();
	
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
	public String[] getRoleids() {
		return roleids;
	}
	public void setRoleids(String[] roleids) {
		this.roleids = roleids;
	}
	public RoleBean[] getRoleBean() {
		return roleBean;
	}
	public void setRoleBean(RoleBean[] roleBean) {
		this.roleBean = roleBean;
	}

	
}
