package com.wolf.dept.javaBean;

import java.util.HashSet;
import java.util.Set;

public class DeptPropertyBean {

	private String id;
	private String deptproperty;
	private String systemname;
	
	public static Set<DeptPropertyBean> deptPropertySet = new HashSet<DeptPropertyBean>();
	
	public DeptPropertyBean(){
		deptPropertySet.add(this);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeptproperty() {
		return deptproperty;
	}
	public void setDeptproperty(String deptproperty) {
		this.deptproperty = deptproperty;
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
				+ ((deptproperty == null) ? 0 : deptproperty.hashCode());
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
		DeptPropertyBean other = (DeptPropertyBean) obj;
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
		return true;
	}
	
}
