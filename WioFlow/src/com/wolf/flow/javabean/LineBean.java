package com.wolf.flow.javabean;

public class LineBean {

	private String flowId;
	private String linename;
	private String froms;
	private String tos;
	private String type;
	private String status;
	private String name;
	private String m;
	
	public String getLinename() {
		return linename;
	}
	public LineBean setLinename(String linename) {
		this.linename = linename;return this;
	}
	public String getFroms() {
		return froms;
	}
	public LineBean setFroms(String froms) {
		this.froms = froms;return this;
	}
	public String getTos() {
		return tos;
	}
	public LineBean setTos(String tos) {
		this.tos = tos;return this;
	}
	public String getType() {
		return type;
	}
	public LineBean setType(String type) {
		this.type = type;return this;
	}
	public String getFlowId() {
		return flowId;
	}
	public LineBean setFlowId(String flowId) {
		this.flowId = flowId;return this;
	}
	public String getStatus() {
		return status;
	}
	public LineBean setStatus(String status) {
		this.status = status;return this;
	}
	public String getName() {
		return name;
	}
	public LineBean setName(String name) {
		this.name = name;return this;
	}
	public String getM() {
		return m;
	}
	public LineBean setM(String m) {
		this.m = m;return this;
	}
	
}
