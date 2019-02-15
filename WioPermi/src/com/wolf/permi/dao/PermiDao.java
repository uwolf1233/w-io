package com.wolf.permi.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.permi.javaBean.PermiBean;
import com.wolf.permi.lock.Locks;
import com.wolf.permi.sql.Sqls;

public class PermiDao {

	private PermiDao(){}
	
	public static PermiDao INSTANCE = new PermiDao();
	
	public String getPermisToJson(String[] ids,ReqResBean rrb){
		return PermiBean.getJsonFromIds(ids, rrb);
	}
	
//	public void getPermis(){
//		String sql = Sqls.getSystemPermisSql;
//		Locks.permiwrLock.writeLock().lock();
//		try{
//			MyJdbc.INSTANCE.queryForList(sql, null, PermiBean.class);
//		}catch(Exception e){
//			e.printStackTrace();
//		}finally{
//			Locks.permiwrLock.writeLock().unlock();
//		}
//	}
	
	public String getAllPermi(ReqResBean rrb){
		return PermiBean.getAllPermi(rrb);
	}
	
	public String getRolePermiStr(String roleid,String dataclass,ReqResBean rrb){
		return null;
	}
	
	public Map<String,String> getPermiFromIds(String[] roleidArray,String dataClass,ReqResBean rrb){
		StringBuilder idsBuilder = new StringBuilder("");
		int roleidArrayLen = roleidArray.length;
		for(int i=0;i<roleidArrayLen;i++){
			idsBuilder.append(i==0?roleidArray[i]:(","+roleidArray[i]));
		}
		String getRolePermiSql = Sqls.getRolePermiSql;
		getRolePermiSql = getRolePermiSql.replace("${roleid}", "'"+idsBuilder.toString()+"'");
		List<Object[]> objList = MyJdbc.INSTANCE.queryForFieldsList(getRolePermiSql, 
				new Object[]{dataClass}, new String[]{"datas","datatype"});
		Map<String,String> map = new HashMap<String,String>();
		int objListSize = objList.size();
		for(int i=0;i<objListSize;i++){
			byte[] datasbyte = (byte[])objList.get(i)[0];
			String datas = new String(datasbyte);
			String datatype = objList.get(i)[1]+"";
			if(datatype.equals("sql")){//如果是sql语句
				if(!map.containsKey("sql")){
					map.put("sql", " and "+datas+" ");
				}else{
					map.put("sql", map.get("sql") + datas);
				}
			}else if(datatype.equals("id")){//如果是id
				if(!map.containsKey("ids")){
					map.put("ids", datas);
				}else{
					map.put("ids", map.get("ids") + "," +datas);
				}
			}
		}
		return map;
	}
	
	public int savePermi(String permiid,String name,String systemname,String pid,String isend,String datas,String datatype,
			String dataclass,String roleid,ReqResBean rrb){
		String save_system_permiSql = Sqls.save_system_permiSql;
		String save_system_permi_dataSql = Sqls.save_system_permi_dataSql;
		String save_system_role_permiSql = Sqls.save_system_role_permiSql;
		try{
			
			int i = MyJdbc.INSTANCE.update(save_system_permiSql, new Object[]{permiid,name,systemname,pid,isend}, rrb);
			if(i > 0){
				i = MyJdbc.INSTANCE.update(save_system_permi_dataSql, new Object[]{permiid,datas.getBytes(),datatype,dataclass}, rrb);
				if(i > 0){
					i = MyJdbc.INSTANCE.update(save_system_role_permiSql,new Object[]{permiid,roleid,systemname},rrb);
				}
			}
			return i;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
}











