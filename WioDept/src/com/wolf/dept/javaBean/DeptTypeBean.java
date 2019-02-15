package com.wolf.dept.javaBean;

import java.util.HashSet;
import java.util.Set;

public class DeptTypeBean {

	private String id;
	private String depttype;
	private String systemname;
	
	public static Set<DeptTypeBean> deptTypeSet = new HashSet<DeptTypeBean>();
	
	public DeptTypeBean(){
		deptTypeSet.add(this);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDepttype() {
		return depttype;
	}
	public void setDepttype(String depttype) {
		this.depttype = depttype;
	}
	public String getSystemname() {
		return systemname;
	}
	public void setSystemname(String systemname) {
		this.systemname = systemname;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((depttype == null) ? 0 : depttype.hashCode());
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
		DeptTypeBean other = (DeptTypeBean) obj;
		if (depttype == null) {
			if (other.depttype != null)
				return false;
		} else if (!depttype.equals(other.depttype))
			return false;
		if (systemname == null) {
			if (other.systemname != null)
				return false;
		} else if (!systemname.equals(other.systemname))
			return false;
		return true;
	}
	
}
