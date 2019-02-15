package com.wolf.dept.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wolf.dept.javaBean.DeptBean;
import com.wolf.dept.javaBean.DeptPropertyBean;
import com.wolf.dept.javaBean.DeptTypeBean;
import com.wolf.dept.javaBean.StaticBeans;
import com.wolf.dept.javaBean.UserDept;
import com.wolf.dept.lock.Locks;
import com.wolf.dept.sql.Sqls;
import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;

public class DeptDao {

	private DeptDao(){}
	
	public static DeptDao INSTANCE = new DeptDao();
	
	public String getPermisToJson(String[] ids,ReqResBean rrb){
		return DeptBean.getJsonFromIds(ids, rrb);
	}
	
	public void refreshDeptsNoLock(ReqResBean rrb){
		String sql = Sqls.getSystemDeptsSql;
		DeptBean.clean();
		MyJdbc.INSTANCE.queryForList(sql, null, DeptBean.class,rrb);
	}
	
	public void refreshDepts(ReqResBean rrb){
		boolean flag = false;
		try {
			flag = Locks.deptwrLock.readLock().tryLock(5000, TimeUnit.SECONDS);
			if(flag){
				String sql = Sqls.getSystemDeptsSql;
				DeptBean.clean();
				MyJdbc.INSTANCE.queryForList(sql, null, DeptBean.class,rrb);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally{
			if(flag){
				Locks.deptwrLock.readLock().unlock();
			}
		}
	}
	
	public void getDepts(ReqResBean rrb){
		String sql = Sqls.getSystemDeptsSql;
		Locks.deptwrLock.writeLock().lock();
		try{
			MyJdbc.INSTANCE.queryForList(sql, null, DeptBean.class,rrb);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Locks.deptwrLock.writeLock().unlock();
		}
	}
	
	public void getDeptTypes(ReqResBean rrb){
		String sql = Sqls.getSystemDepttypeSql;
		MyJdbc.INSTANCE.queryForList(sql, null, DeptTypeBean.class,rrb);
	}
	
	public void getDeptPropertys(ReqResBean rrb){
		String sql = Sqls.getSystemDeptPropertySql;
		MyJdbc.INSTANCE.queryForList(sql, null, DeptPropertyBean.class,rrb);
	}
	
	public String getAllRole(ReqResBean rrb){
		return DeptBean.getAllRole(rrb);
	}
	
	public int addDept(Map<String,Object> map,ReqResBean rrb,String ids[]){
		int z = 0;
		Locks.deptwrLock.writeLock().lock();
		String name = map.get("name")+"";
		try{
			String keys[] = new String[]{"id","name","depttype","deptproperty","systemname","pid","isend"};
			Object params[] = new Object[keys.length];
			int keyslen = keys.length;
			for(int i=0;i<keyslen;i++){
				params[i] = map.get(keys[i]);
			}
			z = MyJdbc.INSTANCE.update(Sqls.insertDeptSql, params,rrb);
			rrb.log(name+"部门添加成功", "1");
		}catch(Exception e){
			map.clear();
			rrb.log(name+"部门添加失败", "0");
			map.put("type", "error");
		}finally{
			Locks.deptwrLock.writeLock().unlock();
		}
		return z;
	}
	
	public String getDeptOneData(String id,ReqResBean rrb){
		return DeptBean.getMapFromIds(new String[]{id}, rrb);
	}
	
	public int updateDept(Map<String,Object> map,ReqResBean rrb){
		int z = 0;
		Locks.deptwrLock.writeLock().lock();
		String name = map.get("name")+"";
		try{
			String keys[] = new String[]{"name","depttype","deptproperty","isend","id","systemname"};
			Object params[] = new Object[keys.length];
			int keyslen = keys.length;
			for(int i=0;i<keyslen;i++){
				params[i] = map.get(keys[i])+"";
			}
			z = MyJdbc.INSTANCE.update(Sqls.updateDeptSql, params,rrb);
			rrb.log(name+"部门添加成功", "1");
		}catch(Exception e){
			map.clear();
			rrb.log(name+"部门添加失败", "0");
			map.put("type", "error");
		}finally{
			Locks.deptwrLock.writeLock().unlock();
		}
		return z;
	}
	
	public int delete(String idArray[],ReqResBean rrb){
		List<Object[]> params = new ArrayList<Object[]>();
		int idArrayLen = idArray.length;
		for(int i=0;i<idArrayLen;i++){
			Object[] os = new Object[]{idArray[i]};
			params.add(os);
		}
		int z = 0;
		Locks.deptwrLock.writeLock().lock();
		try{
			String sql = Sqls.deleteDeptSql;
			MyJdbc.INSTANCE.batchUpdate(sql, params, rrb);
			rrb.log("部门删除成功", "1");
		}catch(Exception e){
			rrb.log("部门删除成功", "0");
		}finally{
			Locks.deptwrLock.writeLock().unlock();
		}
		return z;
	}
	
	public int deptSetUser(String deptid,String userid[],ReqResBean rrb){
		try {
			String delSql = Sqls.deleteSystemUserDeptSql;
			String insertSql = Sqls.insertSystemUserDeptSql;
			Object params[] = new Object[]{deptid};
			int i = MyJdbc.INSTANCE.update(delSql, params, rrb);
			List<Object[]> insertParam = new ArrayList<Object[]>();
			int useridLen = userid.length;
			for(int z=0;z<useridLen;z++){
				Object[] os = new Object[2];
				os[0] = deptid;
				os[1] = userid[z];
				insertParam.add(os);
			}
			MyJdbc.INSTANCE.batchUpdate(insertSql, insertParam, rrb);
			rrb.log("部门对用户授权成功", "1");
			return i;
		} catch (Exception e) {
			rrb.log("部门对用户授权失败", "0");
			return 0;
		}
	}
	
	public String deptGetUsers(String deptid,ReqResBean rrb){
		try{
			Object[] params = new Object[]{deptid};
			String sql = Sqls.getUsersFromDept;
			List<UserDept> userDeptList = MyJdbc.INSTANCE.queryForList(sql, params, UserDept.class, rrb);
			return StaticBeans.gson.toJson(userDeptList);
		}catch(Exception e){
			return StaticBeans.gson.toJson(new ArrayList<UserDept>());
		}
	}
	
	public String userGetDeptId(String userid,ReqResBean rrb){
		try {
			String userGetDeptIdSql = Sqls.userGetDeptIdSql;
			Object[] os = MyJdbc.INSTANCE.queryForFields(userGetDeptIdSql, new Object[]{userid,"system"}, new String[]{"deptid"});
			if(os[0] == null){
				return "error";
			}else{
				return os[0]+"";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
	
}









