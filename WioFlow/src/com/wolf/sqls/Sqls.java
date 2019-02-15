package com.wolf.sqls;

public class Sqls {

	public static String saveflowSql = "insert into flow(id,flowname,initNum) values(?,?,?)";
	public static String initFlowSql = "select * from flow";
	public static String initFlowFromIdSql = "select * from flow where id = ?";
	public static String initNodeSql = "select * from node where flowid = ?";
	public static String initLineSql = "select * from line where flowid = ?";
	public static String initUserSql = "select * from flowuser where flowid = ?";
	public static String initDeptSql = "select * from flowdept where flowid = ?";
	public static String initRoleSql = "select * from flowrole where flowid = ?";
	
	public static String delFlowSql = "delete from flow where id = ?";
	
	public static String createTaskSql = "insert into task(id,flowid,dataid,status) values(?,?,?,?)";
	public static String addruntaskSql = "insert into runtask(id,num,taskid,curnodeename,memo) values(?,?,?,?,?)";
	public static String getruntaskCountSql = "select count(*) c from runtask where taskid = ?";
	public static String getCurFlowNodeSql = "select * from runtask where taskid = ? and runtask = (select max(num) from taskid)";
	public static String getTaskSql = "select * from task where dataid = ? and status = '0'";
	public static String getRunTaskFromNumSql = "select * from runtask where num = ? and taskid = ?";
	public static String getFlowIdFromTaskIdSql = "select * from task where id = ?";
	
	public static String getHiFlowFromFlowIdSql = "select * from hi_flow where flowid = ?";
	public static String saveHiFlowSql = "insert into hi_flow select * from flow where id = ?";
	public static String removeFlowSql = "delete from flow where id = ?";
	public static String saveHiNodeSql = "insert into hi_node select * from node where flowid = ?";
	public static String removeNodeSql = "delete from node where flowid = ?";
	public static String saveHiLineSql = "insert into hi_line select * from line where flowid = ?";
	public static String removeLineSql = "delete from line where flowid = ?";
	public static String saveHiFlowUserSql = "insert into hi_flowuser select * from flowuser where flowid = ?";
	public static String removeUserSql = "delete from flowuser where flowid = ?";
	public static String saveHiFlowDeptSql = "insert into hi_flowdept select * from flowdept where flowid = ?";
	public static String removeDeptSql = "delete from flowdept where flowid = ?";
	public static String saveHiFlowRoleSql = "insert into hi_flowrole select * from flowrole where flowid = ?";
	public static String removeRoleSql = "delete from flowrole where flowid = ?";
	
	public static String getNextNodeSql = "select t2.nodeename,t2.type from line t1 join "
			+ " node t2 on t1.tos = t2.nodeename where t1.flowid = ? "
			+ " and t1.froms = ? and t1.status = ? ";
	
	public static String getNextHiNodeSql = "select t2.nodeename,t2.type from hi_line t1 join "
			+ " hi_node t2 on t1.tos = t2.nodeename where t1.flowid = ? "
			+ " and t1.froms = ? and t1.status = ? ";
	
	public static String updateTaskStatusSql = "update task set status = ? where id = ?";
	
	public static String getRunTaskFromDataSql = "select top 1 t1.num,t2.nodecname,t1.taskid from runtask t1 join node "
			+ "t2 on t1.curnodeename = t2.nodeename join task t3 on t1.taskid = t3.id where t3.dataid = ? "
			+ "order by cast(t1.num as Integer) desc";
	
	public static String hasFlowSql = "select count(*) c from flow where id = ?";
	public static String hihashFlowSql = "select count(*) c from hi_flow where id = ?";
	
	public static String getHiNodeSql = "select * from hi_node where flowid = ? and nodeename = ?";
	
	public static String getNodeSql = "select * from node where flowid = ? and nodeename = ?";
	
	public static String getHiNextNodeSql = "select * from hi_node where nodeename in (select tos from hi_line "
			+ " where froms = ? and flowid = ?) "
			+ " and flowid = ? ";
	
	public static String dataHasInflowSql = "select count(*) c from task where dataid = ? and status = '0'";
	
	public static String getCurRunTaskMaxFromDataSql = "select top(1) * from runtask where taskid in (select id from task where dataid = ?) " +
			"order by cast(num as Integer) desc";
	
	public static String hasFlowFromNameSql = "select count(*) c from flow where flowname = ?";
	
}

















