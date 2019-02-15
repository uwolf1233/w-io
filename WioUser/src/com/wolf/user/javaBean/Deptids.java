package com.wolf.user.javaBean;

import java.util.HashSet;
import java.util.Set;

import com.wolf.user.dao.UserDao;

public class Deptids {

	public String id;
	public String[] userids;
	public UserBean[] userBean;
	
	private static Set<Deptids> deptidsSet = new HashSet<Deptids>();
	
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
		Deptids other = (Deptids) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String[] getUserids() {
		return userids;
	}
	public void setUserids(String[] userids) {
		this.userids = userids;
	}
	public UserBean[] getUserBean() {
		return userBean;
	}
	public void setUserBean(UserBean[] userBean) {
		this.userBean = userBean;
	}
	
}
