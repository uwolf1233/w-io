package com.wolf.flow.controller;

import java.util.List;
import java.util.Map;

import com.wolf.dao.FlowDao;
import com.wolf.flow.javabean.RunTaskBean;
import com.wolf.flow.javabean.ShowRunTaskBean;
import com.wolf.flow.javabean.StaticBeans;
import com.wolf.flow.service.FlowService;
import com.wolf.flow.service.TaskService;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.StaticBean;
import com.wolf.server.MethodMapping;

public class FlowController {
	
	@MethodMapping(path="/systemShow",interfacetype="h",threadPoolNumMax=2)//基本测试,全局负载均衡
	public String test1(ReqResBean rrb,SessionLine sessionLine){
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/flowShow",interfacetype="h",threadPoolNumMax=2)//基本测试,全局负载均衡
	public String flowShow(ReqResBean rrb,SessionLine sessionLine){
		rrb.addCookie("wsessionId", "123123");
		return "/flow/flow";
	}

	@MethodMapping(path="/saveFlow",interfacetype="h",trans=true)
	public void saveFlow(ReqResBean rrb,SessionLine sessionLine){
		try {
			String jsonData = rrb.getRequestMap().get("jsonData");
			FlowService.INSTANCE.saveFlow(jsonData, rrb);
			rrb.setResponseKV("type", "success");
			rrb.thransThread(1);
		} catch (Exception e) {
			rrb.thransThread(0);
			rrb.setResponseKV("type", "error");
			rrb.setResponseKV("data", e.getMessage());
		}
	}
	
	@MethodMapping(path="/removeFlow",interfacetype="h",trans=true)
	public void removeFlow(ReqResBean rrb,SessionLine sessionLine){
		String flowid = rrb.getRequestMap().get("flowid");
		FlowService.INSTANCE.removeFlow(flowid, rrb);
		rrb.setResponseKV("type", "success");
	}
	
	@MethodMapping(path="/updateFlow",interfacetype="h",trans=true)
	public void updateFlow(ReqResBean rrb,SessionLine sessionLine){
		String flowid = rrb.getRequestMap().get("flowid");
		String jsonData = rrb.getRequestMap().get("jsonData");
		if(FlowService.INSTANCE.updateFlow(flowid, jsonData, rrb)){
			rrb.setResponseKV("type", "success");
		}else{
			rrb.setResponseKV("type", "error");
		}
	}
	
	@MethodMapping(path="/createTask",interfacetype="sh",trans=true)
	public void createTask(ReqResBean rrb,SessionLine sessionLine){
		try {
			TaskService task = TaskService.INSTANCE;
			task.createTask(rrb.getRequestMap().get("flowName"), rrb.getRequestMap().get("dataid"), rrb);
			rrb.setResponseKV("type", "success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.setResponseKV("type", "error");
			rrb.setResponseKV("data", e.getMessage());
		}
	}
	
	@MethodMapping(path="/runTask",interfacetype="sh",trans=true)
	public void runTask(ReqResBean rrb,SessionLine sessionLine){
		TaskService task = TaskService.INSTANCE;
		try {
			task.runNext(rrb.getRequestMap().get("dataid"), rrb.getRequestMap().get("memo"), 
					rrb.getRequestMap().get("status"),rrb);
			rrb.setResponseKV("type", "success");
			rrb.thransThread(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.setResponseKV("type", "error");
			rrb.setResponseKV("data", e.getMessage());
			rrb.thransThread(0);
		}
	}
	
	@MethodMapping(path="/getDataTask",interfacetype="sh",trans=true)
	public void getDataTask(ReqResBean rrb,SessionLine sessionLine){
		List<ShowRunTaskBean> list = FlowDao.INSTANCE.getRunTaskFromData(rrb);
		String s = StaticBeans.gson.toJson(list);
		rrb.setResponseData(s);
	}
	
	@MethodMapping(path="/getAllFlowBean",interfacetype="h")
	public void getAllFlowBean(ReqResBean rrb,SessionLine sessionLine){
		rrb.setResponseData(StaticBean.gson.toJson(FlowDao.INSTANCE.getAllFlowBean(rrb)));
	}
	
	//当前提交的流程废弃
	@MethodMapping(path="/getAllFlowBean",interfacetype="sh",trans=true)
	public void discardCurrentTask(ReqResBean rrb,SessionLine sessionLine){
		
	}
	
	@MethodMapping(path="/getFlowFromId",interfacetype="h")
	public void getFlowFromId(ReqResBean rrb,SessionLine sessionLine){
		String flowId = rrb.getRequestMap().get("flowid");
		String s = StaticBeans.gson.toJson(FlowDao.INSTANCE.getFlowFromId(flowId, rrb));
		rrb.setResponseData(s);
	}
	
}







