package com.wolf.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.wolf.flow.javabean.DeptBean;
import com.wolf.flow.javabean.FlowBean;
import com.wolf.flow.javabean.LineBean;
import com.wolf.flow.javabean.NodeBean;
import com.wolf.flow.javabean.RoleBean;
import com.wolf.flow.javabean.RunTaskBean;
import com.wolf.flow.javabean.ShowRunTaskBean;
import com.wolf.flow.javabean.TaskBean;
import com.wolf.flow.javabean.UserBean;
import com.wolf.flow.service.FlowService;
import com.wolf.flow.service.TaskService;
import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.jdbcs.SaveFilter;
import com.wolf.sqls.Sqls;

public class FlowDao {

	private FlowDao(){}
	
	public static FlowDao INSTANCE = new FlowDao();
	
	public boolean saveFlow(FlowBean fb,ReqResBean rrb) throws Exception{
		try {
			String saveflowSql = Sqls.saveflowSql;
			MyJdbc.INSTANCE.update(saveflowSql, new Object[]{fb.getId(),fb.getFlowname(),fb.getInitNum()}, rrb);
			List<NodeBean> nodeBeans = fb.getNodeBeans();
			MyJdbc.INSTANCE.javaBeanListSaveEx(nodeBeans, "node",new SaveFilter() {
				
				public boolean saveNameFilter(String name) {
					if(name.equals("user") || name.equals("dept") || name.equals("role")){
						return true;
					}else{
						return false;
					}
				}
			}, rrb);
			List<LineBean> lineBeans = fb.getLineBeans();
			MyJdbc.INSTANCE.javaBeanListSaveEx(lineBeans, "line", rrb);
			if(fb.getUserBeans() != null){
				MyJdbc.INSTANCE.javaBeanListSaveEx(fb.getUserBeans(), "flowuser", rrb);
			}
			if(fb.getDeptBeans() != null){
				MyJdbc.INSTANCE.javaBeanListSaveEx(fb.getDeptBeans(), "flowdept", rrb);
			}
			if(fb.getRoleBeans() != null){
				MyJdbc.INSTANCE.javaBeanListSaveEx(fb.getRoleBeans(), "flowrole", rrb);
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void initFlow(){//初始化时调用
		try {
			String initFlowSql = Sqls.initFlowSql;
			String initNodeSql = Sqls.initNodeSql;
			String initLineSql = Sqls.initLineSql;
			List<FlowBean> fbList = MyJdbc.INSTANCE.queryForList(initFlowSql, null, FlowBean.class, null);
			for(FlowBean flowBean : fbList){
				List<NodeBean> nbList = MyJdbc.INSTANCE.queryForList(initNodeSql, new Object[]{flowBean.getId()}, NodeBean.class, null);
				flowBean.setNodeBeans(nbList);
				List<LineBean> lbList = MyJdbc.INSTANCE.queryForList(initLineSql, new Object[]{flowBean.getId()}, LineBean.class, null);
				flowBean.setLineBeans(lbList);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("初始化异常");
		}
	}
	
	public boolean createTask(String taskid,String flowid,String dataid,ReqResBean rrb){
		String createTaskSql = Sqls.createTaskSql;
		return MyJdbc.INSTANCE.update(createTaskSql, new Object[]{taskid,flowid,dataid,"0"}, rrb) > 0;
	}
	
	public boolean runtask(String taskid,String curnodeename,String memo,String status,ReqResBean rrb){
		try {
			String id = UUID.randomUUID().toString().replace("-", "");
			String addruntaskSql = Sqls.addruntaskSql;
			long count = MyJdbc.INSTANCE.queryCount(Sqls.getruntaskCountSql, new Object[]{taskid}, "c", rrb)+1;//判断流程到第几步，针对数据库而不是工作流
			if(curnodeename == null || curnodeename.equals("")){//如果是流程初始化后执行的，curnodeename有值
				String getRunTaskFromNumSql = Sqls.getRunTaskFromNumSql;
				RunTaskBean runTask = MyJdbc.INSTANCE.queryForObj(getRunTaskFromNumSql, new Object[]{(count-1)+"",taskid}, 
						RunTaskBean.class, rrb);//查询当前已执行的最后一个步骤的节点名称（是key不是中文名）
				String ccurnodeename = runTask.getCurnodeename();//获取当前已执行的最后一个步骤的节点名称
				String getFlowIdFromTaskIdSql = Sqls.getFlowIdFromTaskIdSql;
				TaskBean task = MyJdbc.INSTANCE.queryForObj(getFlowIdFromTaskIdSql, new Object[]{taskid}, TaskBean.class, rrb);//获取当前的流程任务
				String flowid = task.getFlowid();//获取流程id
				
				boolean isHi = false;
				String hasFlowSql = Sqls.hasFlowSql;
				if(MyJdbc.INSTANCE.queryCount(hasFlowSql, new Object[]{flowid}, "c", rrb) == 0){//判断流程是在历史表还是当前表
					String hihashFlowSql = Sqls.hihashFlowSql;
					if(MyJdbc.INSTANCE.queryCount(hihashFlowSql, new Object[]{flowid}, "c", rrb) == 0){
						rrb.log("任务:"+task.getId()+"查询流程id为"+flowid+",找不到流程", "0");
						throw new RuntimeException("找不到流程");
					}
					isHi = true;
				}
				NodeBean cnb = null;
				if(!isHi){
					cnb = FlowService.INSTANCE.getCurFromId(flowid, ccurnodeename);//获取当前已执行的最后一个流程节点
				}else{//如果是历史流程
					String getHiNodeSql = Sqls.getHiNodeSql;
					cnb = MyJdbc.INSTANCE.queryForObj(getHiNodeSql, new Object[]{flowid,ccurnodeename}, NodeBean.class, rrb);
				}
				if(cnb == null){
					rrb.log("任务:"+task.getId()+"查询流程id为"+flowid+",最后一个已执行的流程节点名称为:"+ccurnodeename+",流程数据异常", "0");
					System.out.println("流程数据异常");
					return false;
				}
				String cnbType = cnb.getType();//这里可以做流转判断，结束判断
				if(cnbType.equals("end") || cnbType.equals("end round")){
					rrb.log("任务:"+task.getId()+"流程已经到最后一个节点,无法继续执行", "0");
					System.out.println("流程已经到最后一个节点");
					return false;
				}
				NodeBean nb = null;
				if(!isHi){//如果不是历史流程
					nb = FlowService.INSTANCE.getNextFromId(flowid, ccurnodeename,status);
				}else{
					String getHiNextNodeSql = Sqls.getHiNextNodeSql;
					nb = MyJdbc.INSTANCE.queryForObj(getHiNextNodeSql, new Object[]{ccurnodeename,flowid,flowid}, NodeBean.class, rrb);
				}
				curnodeename = nb.getNodeename();//根据流程id和当前已执行的流程节点名称获取下一个节点
				String nodeType = nb.getType();//获取节点类型
				//这里可以判断自动流程的程序
				if(nodeType.equals("auto")){
					String getNextNodeSql = "";
					if(!isHi){//判断是否历史流程
						getNextNodeSql = Sqls.getNextNodeSql;
					}else{
						getNextNodeSql = Sqls.getNextHiNodeSql;
					}
					//获取下一个流程的节点
					Object[] oo = MyJdbc.INSTANCE.queryForFields(getNextNodeSql, new Object[]{flowid,curnodeename,status}, new String[]{"nodeename","type"});
					String nextNode = oo[0]+"";
					String cnodeType = oo[1]+"";
					if(nextNode.trim().equals("")){
						return false;
					}
					List<Object[]> olist = new ArrayList<Object[]>();
					olist.add(new Object[]{id,count,taskid,curnodeename,""});
					olist.add(new Object[]{UUID.randomUUID().toString().replace("-", ""),count+1,taskid,nextNode,memo});
					int[] i = MyJdbc.INSTANCE.batchUpdate(addruntaskSql, olist, rrb);//批处理，避免锁表导致的问题
					if(i != null && !Arrays.asList(i).contains(0)){
						if(cnodeType.equals("end") || cnodeType.equals("end round")){//如果认为到了结尾
							String updateTaskStatusSql = Sqls.updateTaskStatusSql;//去结束当前流程
							if(MyJdbc.INSTANCE.update(updateTaskStatusSql, new Object[]{"1",taskid}, rrb) == 0){
								throw new RuntimeException("taskid:"+taskid+"流程结束异常");
							}
						}
						return true;
					}else{
						return false;
					}
				}else if(nodeType.equals("end")){//如果认为到了结尾
					String updateTaskStatusSql = Sqls.updateTaskStatusSql;//去结束当前流程
					if(MyJdbc.INSTANCE.update(updateTaskStatusSql, new Object[]{"1",taskid}, rrb) == 0){
						throw new RuntimeException("taskid:"+taskid+"流程结束异常");
					}
				}
			}
			memo = memo == null ? "" : memo;
			return MyJdbc.INSTANCE.update(addruntaskSql, new Object[]{id,count,taskid,curnodeename,memo}, rrb) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("流程执行失败");
		}
	}
	
	public boolean flowToHiflow(String flowid,ReqResBean rrb){//流程进入历史，新流程代替旧流程
		try {
			String saveHiFlowSql = Sqls.saveHiFlowSql;
			String saveHiNodeSql = Sqls.saveHiNodeSql;
			String saveHiLineSql = Sqls.saveHiLineSql;
			String saveHiFlowUserSql = Sqls.saveHiFlowUserSql;
			String saveHiFlowDeptSql = Sqls.saveHiFlowDeptSql;
			String saveHiFlowRoleSql = Sqls.saveHiFlowRoleSql;
			
			MyJdbc.INSTANCE.update(saveHiFlowSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(saveHiNodeSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(saveHiLineSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(saveHiFlowUserSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(saveHiFlowDeptSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(saveHiFlowRoleSql, new Object[]{flowid}, rrb);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean removeOldFlow(String flowid,ReqResBean rrb){//删除
		try {
			String removeFlowSql = Sqls.removeFlowSql;
			String removeNodeSql = Sqls.removeNodeSql;
			String removeLineSql = Sqls.removeLineSql;
			String removeUserSql = Sqls.removeUserSql;
			String removeDeptSql = Sqls.removeDeptSql;
			String removeRoleSql = Sqls.removeRoleSql;
			
			MyJdbc.INSTANCE.update(removeFlowSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(removeNodeSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(removeLineSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(removeUserSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(removeDeptSql, new Object[]{flowid}, rrb);
			MyJdbc.INSTANCE.update(removeRoleSql, new Object[]{flowid}, rrb);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public List<ShowRunTaskBean> getRunTaskFromData(ReqResBean rrb){
		try {
			String dataid = rrb.getRequestMap().get("dataid");
			String getRunTaskFromDataSql = Sqls.getRunTaskFromDataSql;
			List<ShowRunTaskBean> rtbList = MyJdbc.INSTANCE.queryForList(getRunTaskFromDataSql, new Object[]{dataid}, ShowRunTaskBean.class, rrb);
			return rtbList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<ShowRunTaskBean>();
		}
	}
	
	public boolean hasFlow(String flowId,ReqResBean rrb){
		try {
			String hasFlowSql = Sqls.hasFlowSql;
			return MyJdbc.INSTANCE.queryCount(hasFlowSql, new Object[]{flowId}, "c", rrb) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public boolean dataHasInflow(String dataid,ReqResBean rrb){
		try {
			String dataHasInflowSql = Sqls.dataHasInflowSql;
			return MyJdbc.INSTANCE.queryCount(dataHasInflowSql, new Object[]{dataid}, "c", rrb) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public Object[] getCurRunTaskNode(String dataid,ReqResBean rrb){
		String getCurRunTaskMaxFromDataSql = Sqls.getCurRunTaskMaxFromDataSql;
		RunTaskBean rtb = MyJdbc.INSTANCE.queryForObj(getCurRunTaskMaxFromDataSql, new Object[]{dataid}, RunTaskBean.class, rrb);
		if(rtb == null){
			throw new RuntimeException("数据异常");
		}
		String getFlowIdFromTaskIdSql = Sqls.getFlowIdFromTaskIdSql;
		TaskBean tb = MyJdbc.INSTANCE.queryForObj(getFlowIdFromTaskIdSql, new Object[]{rtb.getTaskid()}, TaskBean.class, rrb);
		if(tb == null){
			throw new RuntimeException("数据异常");
		}
		NodeBean nodeBean = null;
		String getNodeSql = Sqls.getNodeSql;
		nodeBean = MyJdbc.INSTANCE.queryForObj(getNodeSql, new Object[]{tb.getFlowid(),rtb.getCurnodeename()}, NodeBean.class, rrb);
		if(nodeBean == null){
			String getHiNodeSql = Sqls.getHiNodeSql;//可能是历史
			nodeBean = MyJdbc.INSTANCE.queryForObj(getHiNodeSql, new Object[]{tb.getFlowid(),rtb.getCurnodeename()}, NodeBean.class, rrb);
		}
		if(nodeBean == null){
			throw new RuntimeException("数据异常");
		}
		return new Object[]{nodeBean,tb};
	}
	
	public boolean hasFlowFromName(String flowname,ReqResBean rrb){
		try {
			String hasFlowFromNameSql = Sqls.hasFlowFromNameSql;
			return MyJdbc.INSTANCE.queryCount(hasFlowFromNameSql, new Object[]{flowname}, "c", rrb) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public List<FlowBean> getAllFlowBean(ReqResBean rrb){
		String initFlowSql = Sqls.initFlowSql;
		List<FlowBean> fbList = MyJdbc.INSTANCE.queryForList(initFlowSql, null, FlowBean.class, rrb);
		return fbList;
	}
	
	public Map<String,Object> getFlowFromId(String flowId,ReqResBean rrb){
		Map<String, Object> map = null;
		try {
			map = new HashMap<String,Object>();
			String initFlowFromIdSql = Sqls.initFlowFromIdSql;
			String nodeSql = Sqls.initNodeSql;
			String lineSql = Sqls.initLineSql;
			String initUserSql = Sqls.initUserSql;
			String initDeptSql = Sqls.initDeptSql;
			String initRoleSql = Sqls.initRoleSql;
			FlowBean fb = MyJdbc.INSTANCE.queryForObj(initFlowFromIdSql, new Object[]{flowId}, FlowBean.class, rrb);
			List<NodeBean> nbList = MyJdbc.INSTANCE.queryForList(nodeSql, new Object[]{flowId}, NodeBean.class, rrb);
			List<LineBean> lbList = MyJdbc.INSTANCE.queryForList(lineSql, new Object[]{flowId}, LineBean.class, rrb);
			List<UserBean> ubList = MyJdbc.INSTANCE.queryForList(initUserSql,  new Object[]{flowId}, UserBean.class, rrb);
			List<DeptBean> dbList = MyJdbc.INSTANCE.queryForList(initDeptSql,  new Object[]{flowId}, DeptBean.class, rrb);
			List<RoleBean> rbList = MyJdbc.INSTANCE.queryForList(initRoleSql,  new Object[]{flowId}, RoleBean.class, rrb);
			map.put("title", fb.getFlowname());
			//map.put("initNum", Integer.parseInt(fb.getInitNum()));
			map.put("initNum", new BigDecimal(fb.getInitNum()).intValue());
			int ubListSize = ubList.size();
			int dbListSize = dbList.size();
			int rbListSize = rbList.size();
			//-------------node
			Map<String,Object> nodesmap = new HashMap<String,Object>();
			int nbListLen = nbList.size();
			for(int i=0;i<nbListLen;i++){
				NodeBean nb = nbList.get(i);
				Map<String,Object> nodemap = new HashMap<String,Object>();
				nodemap.put("name", nb.getNodecname());
				nodemap.put("left", Integer.parseInt(nb.getLefts()+""));
				nodemap.put("top",  Integer.parseInt(nb.getTops()+""));
				nodemap.put("type", nb.getType());
				nodemap.put("width",  Integer.parseInt(nb.getWidth()+""));
				nodemap.put("height",  Integer.parseInt(nb.getHeight()+""));
				nodemap.put("alt", true);
				
				if(ubListSize>0){
					for(int j=0;j<ubListSize;j++){
						UserBean ub = ubList.get(j);
						if(nb.getNodeename().equals(ub.getNodeename())){
							nodemap.put("user", ub.getUserid());
							break;
						}
					}
				}
				
				if(dbListSize>0){
					for(int j=0;j<dbListSize;j++){
						DeptBean db = dbList.get(j);
						if(nb.getNodeename().equals(db.getNodeename())){
							nodemap.put("dept", db.getDeptid());
							break;
						}
					}
				}
				
				if(rbListSize>0){
					for(int j=0;j<rbListSize;j++){
						RoleBean rb = rbList.get(j);
						if(nb.getNodeename().equals(rb.getNodeename())){
							nodemap.put("role", rb.getRoleid());
							break;
						}
					}
				}
				
				nodesmap.put(nb.getNodeename(), nodemap);
			}
			map.put("nodes", nodesmap);
			//----------node
			//-------------line
			Map<String,Object> linesmap = new HashMap<String,Object>();
			int lbListLen = lbList.size();
			for(int i=0;i<lbListLen;i++){
				LineBean lb = lbList.get(i);
				Map<String,Object> linemap = new HashMap<String,Object>();
				linemap.put("type", lb.getType());
				linemap.put("from", lb.getFroms());
				linemap.put("to", lb.getTos());
				linemap.put("name", lb.getLinename());
				linemap.put("alt", true);
				linemap.put("status", (lb.getStatus() == null || lb.getStatus().equals("null")) ? "" : lb.getStatus());
				linemap.put("name", (lb.getName() == null || lb.getName().equals("null")) ? "" : lb.getName());
				String m = lb.getM();
				if(m != null && !m.equals("null")){
					linemap.put("M", Double.parseDouble(m));
				}
				linesmap.put(lb.getLinename(), linemap);
			}
			map.put("lines", linesmap);
			map.put("areas", new HashMap<String,Object>());
			//----------line
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
}









