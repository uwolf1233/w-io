package com.wolf.flow.service;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wolf.dao.FlowDao;
import com.wolf.flow.javabean.DeptBean;
import com.wolf.flow.javabean.FlowBean;
import com.wolf.flow.javabean.LineBean;
import com.wolf.flow.javabean.NodeBean;
import com.wolf.flow.javabean.RoleBean;
import com.wolf.flow.javabean.StaticBeans;
import com.wolf.flow.javabean.UserBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.sqls.Sqls;

public class FlowService {
	
	private FlowService(){}
	
	public static FlowService INSTANCE = new FlowService();
	
	public void saveFlow(String jsonData,ReqResBean rrb){
		try {
			Map<String,Object> map = StaticBeans.gson.fromJson(jsonData, new TypeToken<HashMap<String,Object>>(){}.getType());
			String flowName = map.get("title")+"";
			if(FlowDao.INSTANCE.hasFlowFromName(flowName, rrb)){
				rrb.log("已经有名为:"+flowName+"的流程", "0");
				throw new RuntimeException("已经有名为:"+flowName+"的流程");
			}
			Map<String,Object> nodesMap = (Map<String, Object>) map.get("nodes");
			String flowId = UUID.randomUUID().toString().replace("-", "");
			FlowBean fb = new FlowBean();
			fb.setId(flowId);
			fb.setFlowname(flowName);
			String initNum = map.get("initNum")+"";
			fb.setInitNum(initNum.substring(0, initNum.lastIndexOf(".")));
			for(String key : nodesMap.keySet()){
				Map<String,Object> nodeMap = (Map<String, Object>) nodesMap.get(key);
				String left = nodeMap.get("left")+"";
				String top = nodeMap.get("top")+"";
				String width = nodeMap.get("width")+"";
				String height = nodeMap.get("height")+"";
				NodeBean nodeBean = new NodeBean()
					.setFlowId(flowId)
					.setNodeename(key)
					.setNodecname(nodeMap.get("name")+"")
					.setType(nodeMap.get("type")+"")
					.setLefts(left)
					.setTops(top)
					.setWidth(width)
					.setHeight(height)
					.init();
				fb.addNodeBean(nodeBean);
				if(nodeMap.containsKey("user") && !nodeMap.get("user").equals("")){
					UserBean userBean = new UserBean().setFlowid(flowId)
					.setNodeename(key)
					.setUserid(nodeMap.get("user")+"");
					fb.addUserBean(userBean);
				}
				if(nodeMap.containsKey("dept") && !nodeMap.get("dept").equals("")){
					DeptBean deptBean = new DeptBean().setFlowid(flowId)
					.setNodeename(key)
					.setDeptid(nodeMap.get("dept")+"");
					fb.addDeptBean(deptBean);
				}
				if(nodeMap.containsKey("role") && !nodeMap.get("role").equals("")){
					RoleBean roleBean = new RoleBean().setFlowid(flowId)
					.setNodeename(key)
					.setRoleid(nodeMap.get("role")+"");
					fb.addRoleBean(roleBean);
				}
			}
			Map<String,Object> linesMap = (Map<String, Object>) map.get("lines");
			for(String key : linesMap.keySet()){
				Map<String,Object> lineMap = (Map<String, Object>) linesMap.get(key);
				LineBean lineBean = new LineBean()
					.setFlowId(flowId)
					.setLinename(key)
					.setType(lineMap.get("type")+"")
					.setFroms(lineMap.get("from")+"")
					.setTos(lineMap.get("to")+"")
					.setStatus(lineMap.get("status")+"")
					.setName(lineMap.get("name")+"")
					.setM(lineMap.get("M")+"");
				fb.addLineBean(lineBean);
			}
			FlowDao.INSTANCE.saveFlow(fb, rrb);
			rrb.log("工作流保存成功,id:"+flowId, "1");
			rrb.setResponseKV("flowid", flowId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.log("工作流保存异常", "0");
			throw new RuntimeException(e);
		}
	}
	
	public NodeBean getNext(String flowname,String curNodeename,String status){
		if(flowname == null || flowname.trim().equals("")){
			throw new RuntimeException("flowname不能为空");
		}
		Set<FlowBean> flowBeanSet = FlowBean.flowBeanSet;
		NodeBean nb = null;
		for(FlowBean fb : flowBeanSet){
			if(fb.getFlowname().equals(flowname)){
				List<LineBean> lbList = fb.getLineBeans();
				List<NodeBean> nbList = fb.getNodeBeans();
				nb = getNodeFromEname(getToFromLine(curNodeename,lbList,status),nbList);
				break;
			}
		}
		return nb;
	}
	
	public NodeBean getNextFromId(String flowid,String curNodeename,String status){
		if(flowid == null || flowid.trim().equals("")){
			throw new RuntimeException("flowid不能为空");
		}
		Set<FlowBean> flowBeanSet = FlowBean.flowBeanSet;
		NodeBean nb = null;
		for(FlowBean fb : flowBeanSet){
			if(fb.getId().equals(flowid)){
				List<LineBean> lbList = fb.getLineBeans();
				List<NodeBean> nbList = fb.getNodeBeans();
				nb = getNodeFromEname(getToFromLine(curNodeename,lbList,status),nbList);
				break;
			}
		}
		return nb;
	}
	
	public NodeBean getCurFromId(String flowid,String curNodeename){
		if(flowid == null || flowid.trim().equals("")){
			throw new RuntimeException("flowid不能为空");
		}
		Set<FlowBean> flowBeanSet = FlowBean.flowBeanSet;
		NodeBean nb = null;
		for(FlowBean fb : flowBeanSet){
			if(fb.getId().equals(flowid)){
				List<NodeBean> nbList = fb.getNodeBeans();
				nb = getNodeFromEname(curNodeename,nbList);
				break;
			}
		}
		return nb;
	}
	
	private String getToFromLine(String from,List<LineBean> lbList,String status){
		String to = "";
		String linestatus = null;
		for(LineBean lineBean : lbList){
			if(lineBean.getFroms().equals(from)){
				to = lineBean.getTos();
				linestatus = lineBean.getStatus();
				break;
			}
		}
		if(linestatus!=null && linestatus.equals(status)){
			getToFromLine(from, lbList, linestatus);
		}
		return to;
	}
	
	private NodeBean getNodeFromEname(String ename,List<NodeBean> nodeBeans){
		NodeBean nb = null;
		for(NodeBean nodeBean : nodeBeans){
			if(nodeBean.getNodeename().equals(ename)){
				nb = nodeBean;
				break;
			}
		}
		return nb;
	}
	
	public boolean updateFlow(String flowid,String jsonData,ReqResBean rrb){
		boolean b = false;
		int i = 0;
		try {	
			if(!FlowDao.INSTANCE.hasFlow(flowid, rrb)){//如果没有旧流程
				rrb.thransThread(0);
				return false;
			}
			Set<FlowBean> flowBeanSet = FlowBean.flowBeanSet;
			FlowBean fb = null;
			for(FlowBean flowBean : flowBeanSet){
				if(flowBean.getId().equals(flowid)){
					fb = flowBean;
					break;
				}
			}
			if(fb == null){
				return false;
			}
			Map<String,Object> map = StaticBeans.gson.fromJson(jsonData, new TypeToken<HashMap<String,Object>>(){}.getType());
			Map<String,Object> nodesMap = (Map<String, Object>) map.get("nodes");
			String newflowId = UUID.randomUUID().toString().replace("-", "");
			FlowBean nfb = new FlowBean(false);
			nfb.setId(newflowId);
			nfb.setFlowname(map.get("title")+"");
			String initNum = map.get("initNum")+"";
			nfb.setInitNum(initNum);
			for(String key : nodesMap.keySet()){
				Map<String,Object> nodeMap = (Map<String, Object>) nodesMap.get(key);
				NodeBean nodeBean = new NodeBean()
					.setFlowId(newflowId)
					.setNodeename(key)
					.setNodecname(nodeMap.get("name")+"")
					.setType(nodeMap.get("type")+"")
					.setLefts(nodeMap.get("left")+"")
					.setTops(nodeMap.get("top")+"")
					.setWidth(nodeMap.get("width")+"")
					.setHeight(nodeMap.get("height")+"")
					.init();
				nfb.addNodeBean(nodeBean);
				if(nodeMap.containsKey("user") && !nodeMap.get("user").equals("")){
					UserBean userBean = new UserBean().setFlowid(nodeBean.getFlowId())
					.setNodeename(key)
					.setUserid(nodeMap.get("user")+"");
					nfb.addUserBean(userBean);
				}
				if(nodeMap.containsKey("dept") && !nodeMap.get("dept").equals("")){
					DeptBean deptBean = new DeptBean().setFlowid(nodeBean.getFlowId())
					.setNodeename(key)
					.setDeptid(nodeMap.get("dept")+"");
					nfb.addDeptBean(deptBean);
				}
				if(nodeMap.containsKey("role") && !nodeMap.get("role").equals("")){
					RoleBean roleBean = new RoleBean().setFlowid(nodeBean.getFlowId())
					.setNodeename(key)
					.setRoleid(nodeMap.get("role")+"");
					nfb.addRoleBean(roleBean);
				}
			}
			Map<String,Object> linesMap = (Map<String, Object>) map.get("lines");
			for(String key : linesMap.keySet()){
				Map<String,Object> lineMap = (Map<String, Object>) linesMap.get(key);
				LineBean lineBean = new LineBean()
					.setFlowId(newflowId)
					.setLinename(key)
					.setType(lineMap.get("type")+"")
					.setFroms(lineMap.get("from")+"")
					.setTos(lineMap.get("to")+"")
					.setStatus(lineMap.get("status")+"")
					.setName(lineMap.get("name")+"")
					.setM(lineMap.get("M")+"");
				nfb.addLineBean(lineBean);
			}
			if(FlowDao.INSTANCE.saveFlow(nfb, rrb) && FlowDao.INSTANCE.flowToHiflow(flowid, rrb) && 
					FlowDao.INSTANCE.removeOldFlow(flowid, rrb)){
				//System.out.println(flowBeanSet.contains(fb));
				b = flowBeanSet.remove(fb);
				if(!b){//如果对象删除失败，就回滚，内存和数据库必须保证强一致性
					rrb.thransThread(0);
					return false;
				}
				FlowBean.flowBeanSet.add(nfb);
				rrb.thransThread(1);
				return true;
			}else{
				rrb.thransThread(0);
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
			rrb.thransThread(0);
			return false;
		}
	}
	
	public void removeFlow(String flowid,ReqResBean rrb){
		try {
			if(FlowDao.INSTANCE.flowToHiflow(flowid, rrb) && FlowDao.INSTANCE.removeOldFlow(flowid, rrb)){
				rrb.thransThread(1);
			}else{
				rrb.thransThread(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.thransThread(0);
		}
	}
	
}
