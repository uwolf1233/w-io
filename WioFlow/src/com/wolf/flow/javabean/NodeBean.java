package com.wolf.flow.javabean;

import java.util.HashSet;
import java.util.Set;

public class NodeBean {

	private String flowId;
	private String nodeename;
	private String nodecname;
	private String type;
	private Object lefts;
	private Object tops;
	private Object width;
	private Object height;
	private String user;
	private String dept;
	private String role;
	
	public String getNodeename() {
		return nodeename;
	}
	public NodeBean setNodeename(String nodeename) {
		this.nodeename = nodeename;return this;
	}
	public String getNodecname() {
		return nodecname;
	}
	public NodeBean setNodecname(String nodecname) {
		this.nodecname = nodecname;return this;
	}
	public String getType() {
		return type;
	}
	public NodeBean setType(String type) {
		this.type = type;return this;
	}
	public Object getLefts() {
		return lefts;
	}
	public NodeBean setLefts(Object lefts) {
		this.lefts = lefts;return this;
	}
	public Object getTops() {
		return tops;
	}
	public NodeBean setTops(Object tops) {
		this.tops = tops;return this;
	}
	public Object getWidth() {
		return width;
	}
	public NodeBean setWidth(Object width) {
		this.width = width;return this;
	}
	public Object getHeight() {
		return height;
	}
	public NodeBean setHeight(Object height) {
		this.height = height;return this;
	}
	
	public NodeBean init(){
		String leftstr = lefts.toString();
		lefts = leftstr.substring(0, leftstr.lastIndexOf("."));
		String topstr = tops.toString();
		tops = topstr.substring(0, topstr.lastIndexOf("."));
		String widthstr = width.toString();
		width = widthstr.substring(0, widthstr.lastIndexOf("."));
		String heightstr = height.toString();
		height = heightstr.substring(0, heightstr.lastIndexOf("."));
		return this;
	}
	
//	public NodeBean init(){
//		if(lefts instanceof String){
//			lefts = Integer.parseInt(lefts.toString());
//		}
//		if(tops instanceof String){
//			tops = Integer.parseInt(tops.toString());
//		}
//		if(width instanceof String){
//			width = Integer.parseInt(width.toString());
//		}
//		if(height instanceof String){
//			height = Integer.parseInt(height.toString());
//		}
//		return this;
//	}
	public String getFlowId() {
		return flowId;
	}
	public NodeBean setFlowId(String flowId) {
		this.flowId = flowId;return this;
	}
	public String getUser() {
		return user;
	}
	public NodeBean setUser(String user) {
		this.user = user;return this;
	}
	public String getDept() {
		return dept;
	}
	public NodeBean setDept(String dept) {
		this.dept = dept;return this;
	}
	public String getRole() {
		return role;
	}
	public NodeBean setRole(String role) {
		this.role = role;return this;
	}
	
}
