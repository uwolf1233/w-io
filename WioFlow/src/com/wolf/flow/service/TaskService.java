package com.wolf.flow.service;

import java.util.List;
import java.util.UUID;

import com.wolf.dao.FlowDao;
import com.wolf.flow.javabean.FlowBean;
import com.wolf.flow.javabean.LineBean;
import com.wolf.flow.javabean.NodeBean;
import com.wolf.flow.javabean.TaskBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.sqls.Sqls;

public class TaskService {
	
	private TaskService(){}
	
	public static TaskService INSTANCE = new TaskService();
	
	public void createTask(String flowName,String dataid,ReqResBean rrb){
		try {
			NodeBean currentInitNode = null;//可能是打回的
			TaskBean tb = null;
			if(FlowDao.INSTANCE.dataHasInflow(dataid, rrb)){//如果当前数据已经有流程，将不能再新建流程
				Object[] os = FlowDao.INSTANCE.getCurRunTaskNode(dataid, rrb);
				currentInitNode = (NodeBean)os[0];
				tb = (TaskBean)os[1];
				if(currentInitNode == null || !currentInitNode.getType().equals("state") 
						|| !currentInitNode.getNodecname().equals("新增")){
					rrb.thransThread(0);
					rrb.log("数据id为"+dataid+"已经存在于流程中", "0");
					throw new RuntimeException("数据id为"+dataid+"已经存在于流程中");
				}
			}
			FlowBean flowBean = null;
			for(FlowBean cflowBean : FlowBean.flowBeanSet){
				if(cflowBean.getFlowname().equals(flowName)){
					flowBean = cflowBean;
					break;
				}
			}
			if(flowBean == null){
				rrb.thransThread(0);
				rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"找不到流程定义", "0");
				throw new RuntimeException("找不到流程定义");
			}
			if(currentInitNode!=null){
				String nnodeName = getNextNodeName(flowBean.getLineBeans(), currentInitNode.getNodeename());//根据开始节点找到第一个节点
				nnodeName = getNextNodeName(flowBean.getLineBeans(), nnodeName);//根据开始节点找到第一个节点
				boolean b = FlowDao.INSTANCE.runtask(tb.getId(),nnodeName, "", "",rrb);
				if(!b){
					rrb.thransThread(0);
					rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"重新提交失败", "0");
					throw new RuntimeException("创建失败");
				}else{
					rrb.thransThread(1);
					rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"重新提交成功", "1");
				}
			}else{
				String taskid = UUID.randomUUID().toString().replace("-", "");
				boolean b = FlowDao.INSTANCE.createTask(taskid,flowBean.getId(), dataid, rrb);
				if(!b){
					rrb.thransThread(0);
					rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"创建失败", "0");
					throw new RuntimeException("创建失败");
				}else{
					List<NodeBean> nbList = flowBean.getNodeBeans();
					for(NodeBean nb : nbList){
						if(nb.getType().equals("start round mix")){
							NodeBean initNode = getNextNode(flowBean.getLineBeans(), nbList, nb.getNodeename());
							if(initNode.getType().equals("state") && initNode.getNodecname().equals("新增")){//工作流设计，开始之后第一步必须是新增，并且是状态节点
								String nnodeName = getNextNodeName(flowBean.getLineBeans(), initNode.getNodeename());//根据开始节点找到第一个节点
								nnodeName = getNextNodeName(flowBean.getLineBeans(), nnodeName);//根据开始节点找到第一个节点
								b = FlowDao.INSTANCE.runtask(taskid,nnodeName, "", "",rrb);
								break;
							}else{
								rrb.thransThread(0);
								throw new RuntimeException("开始之后第一步必须是新增，并且是状态节点");
							}
						}
					}
					if(!b){
						rrb.thransThread(0);
						rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"创建失败", "0");
						throw new RuntimeException("创建失败");
					}else{
						rrb.thransThread(1);
						rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"创建成功", "1");
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.thransThread(0);
			rrb.log("请求的流程名称为:"+flowName+",数据id为"+dataid+"创建失败", "0");
			throw new RuntimeException("创建失败");
		}
	}
	
	public NodeBean getNextNode(List<LineBean> lbList,List<NodeBean> nbList,String froms){
		String tos = "";
		for(LineBean lb : lbList){
			if(lb.getFroms().equals(froms)){
				tos = lb.getTos();
			}
		}
		NodeBean retnb = null;
		for(NodeBean nb : nbList){
			if(nb.getNodeename().equals(tos)){
				retnb = nb;
				break;
			}
		}
		return retnb;
	}
	
	public String getNextNodeName(List<LineBean> lbList,String froms){
		String tos = "";
		for(LineBean lb : lbList){
			if(lb.getFroms().equals(froms)){
				tos = lb.getTos();
			}
		}
		return tos;
	}
	
	public void runNext(String dataid,String memo,String status,ReqResBean rrb){
//		FlowBean flowBean = null;
//		String flowid = "";
//		for(FlowBean cflowBean : FlowBean.flowBeanSet){
//			if(cflowBean.getId().equals(flowid)){
//				flowBean = cflowBean;
//				break;
//			}
//		}
//		if(flowBean == null){
//			rrb.thransThread(0);
//			rrb.log("请求的流程id为:"+flowid+",数据id为"+dataid+"找不到流程定义", "0");
//			return "找不到流程定义";
//		}
		try {
			TaskBean tb = MyJdbc.INSTANCE.queryForObj(Sqls.getTaskSql, new Object[]{dataid}, TaskBean.class, rrb);
			if(tb == null){
				rrb.log("数据id:"+dataid+"找不到行进中的流程","0");
				throw new RuntimeException("找不到行进中的流程");//这里后面需要加入历史流程的判断，避免流程更新后原流程的节点消失或修改
			}
			boolean b = FlowDao.INSTANCE.runtask(tb.getId(),"", memo, status ,rrb);
			if(!b){
				rrb.log("数据id:"+dataid+"行进到下一个流程节点失败","0");
				throw new RuntimeException("数据id:"+dataid+"行进到下一个流程节点失败");
			}
			rrb.log("数据id:"+dataid+"行进到下一个流程节点成功","1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("流程执行失败");
		}
	}
	
}














