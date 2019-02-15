package com.wolf.flow.javabean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlowBean {
	
	public static Set<FlowBean> flowBeanSet = new HashSet<FlowBean>();

	private String id;
	private String flowname;
	private String initNum;
	private List<NodeBean> nodeBeans;
	private List<LineBean> lineBeans;
	private List<UserBean> userBeans;
	private List<DeptBean> deptBeans;
	private List<RoleBean> roleBeans;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public void addNodeBean(NodeBean nodeBean){
		if(nodeBeans == null){
			nodeBeans = new ArrayList<NodeBean>();
		}
		nodeBeans.add(nodeBean);
	}
	
	public void addLineBean(LineBean lineBean){
		if(lineBeans == null){
			lineBeans = new ArrayList<LineBean>();
		}
		lineBeans.add(lineBean); 
	}
	
	public void addUserBean(UserBean userBean){
		if(userBeans == null){
			userBeans = new ArrayList<UserBean>();
		}
		userBeans.add(userBean);
	}
	
	public void addDeptBean(DeptBean deptBean){
		if(deptBeans == null){
			deptBeans = new ArrayList<DeptBean>();
		}
		deptBeans.add(deptBean);
	}
	
	public void addRoleBean(RoleBean roleBean){
		if(roleBeans == null){
			roleBeans = new ArrayList<RoleBean>();
		}
		roleBeans.add(roleBean);
	}
	
	public FlowBean(){
		flowBeanSet.add(this);
	}
	
	public FlowBean(boolean isave){
		if(isave){
			flowBeanSet.add(this);
		}
	}
	
	public FlowBean setNodeBeans(List<NodeBean> nbList){
		this.nodeBeans = nbList;
		return this;
	}
	
	public FlowBean setLineBeans(List<LineBean> lbList){
		this.lineBeans = lbList;
		return this;
	}
	
	public List<NodeBean> getNodeBeans() {
		return nodeBeans;
	}
	public List<LineBean> getLineBeans() {
		return lineBeans;
	}
	
	public NodeBean nextNode(String curName){
		if(lineBeans == null || nodeBeans == null){
			throw new RuntimeException("flow is null");
		}
		NodeBean nodeBean = null;
		for(LineBean lineBean : lineBeans){
			String from = lineBean.getFroms();
			if(from.equals(curName)){
				String to = lineBean.getTos();
				nodeBean = getNode(to);
				break;
			}
		}
		return nodeBean;
	}
	
	public NodeBean preNode(String curName){
		if(lineBeans == null || nodeBeans == null){
			throw new RuntimeException("flow is null");
		}
		NodeBean nodeBean = null;
		for(LineBean lineBean : lineBeans){
			String to = lineBean.getFroms();
			if(to.equals(curName)){
				String from = lineBean.getFroms();
				nodeBean = getNode(from);
				break;
			}
		}
		return nodeBean;
	}
	
	public NodeBean getNode(String name){
		NodeBean rnodeBean = null;
		for(NodeBean nodeBean : nodeBeans){
			String ename = nodeBean.getNodeename();
			if(ename.equals(name)){
				rnodeBean = nodeBean;
				break;
			}
		}
		return rnodeBean;
	}
	public String getFlowname() {
		return flowname;
	}
	public FlowBean setFlowname(String flowname) {
		this.flowname = flowname;return this;
	}
	public String getInitNum() {
		return initNum;
	}
	public FlowBean setInitNum(String initNum) {
		this.initNum = initNum;return this;
	}
	public List<UserBean> getUserBeans() {
		return userBeans;
	}
	public void setUserBeans(List<UserBean> userBeans) {
		this.userBeans = userBeans;
	}
	public List<DeptBean> getDeptBeans() {
		return deptBeans;
	}
	public void setDeptBeans(List<DeptBean> deptBeans) {
		this.deptBeans = deptBeans;
	}
	public List<RoleBean> getRoleBeans() {
		return roleBeans;
	}
	public void setRoleBeans(List<RoleBean> roleBeans) {
		this.roleBeans = roleBeans;
	}
	
}
