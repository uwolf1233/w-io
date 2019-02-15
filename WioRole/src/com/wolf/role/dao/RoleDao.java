package com.wolf.role.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.role.javaBean.RoleBean;
import com.wolf.role.javaBean.RolePermi;
import com.wolf.role.javaBean.StaticBeans;
import com.wolf.role.lock.Locks;
import com.wolf.role.sql.Sqls;

public class RoleDao {

	private RoleDao(){}
	
	public static RoleDao INSTANCE = new RoleDao();
	
	public String getRoleToJson(String[] ids,ReqResBean rrb){
		return RoleBean.getJsonFromIds(ids, rrb);
	}
	
	public void getRole(ReqResBean rrb){
		String sql = Sqls.getSystemRolesSql;
		Locks.rolewrLock.writeLock().lock();
		try{
			MyJdbc.INSTANCE.queryForList(sql, null, RoleBean.class,rrb);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Locks.rolewrLock.writeLock().unlock();
		}
	}
	
	public String getAllRole(ReqResBean rrb){
		return RoleBean.getAllRole(rrb);
	}
	
	public int addRole(Map<String,Object> map,ReqResBean rrb,String ids[]){
		int z = 0;
		Locks.rolewrLock.writeLock().lock();
		String name = map.get("name")+"";
		try{
			String keys[] = new String[]{"id","name","systemname","pid","isend"};
			Object params[] = new Object[keys.length];
			int keyslen = keys.length;
			for(int i=0;i<keyslen;i++){
				params[i] = map.get(keys[i]);
			}
			z = MyJdbc.INSTANCE.update(Sqls.insertRoleSql, params,rrb);
			rrb.log(name+"角色添加成功", "1");
		}catch(Exception e){
			map.clear();
			rrb.log(name+"角色添加失败", "0");
			map.put("type", "error");
		}finally{
			Locks.rolewrLock.writeLock().unlock();
		}
		return z;
	}
	
	public void refreshRoles(ReqResBean rrb){
		boolean flag = false;
		try {
			flag = Locks.rolewrLock.readLock().tryLock(5000, TimeUnit.SECONDS);
			if(flag){
				String sql = Sqls.getSystemRolesSql;
				RoleBean.clean();
				MyJdbc.INSTANCE.queryForList(sql, null, RoleBean.class,rrb);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally{
			if(flag){
				Locks.rolewrLock.readLock().unlock();
			}
		}
	}
	
	public String getRoleOneData(String id,ReqResBean rrb){
		return RoleBean.getMapFromIds(new String[]{id}, rrb);
	}
	
	public int updateRole(Map<String,Object> map,ReqResBean rrb){
		int z = 0;
		Locks.rolewrLock.writeLock().lock();
		String name = map.get("name")+"";
		try{
			String keys[] = new String[]{"name","isend","id","systemname"};
			Object params[] = new Object[keys.length];
			int keyslen = keys.length;
			for(int i=0;i<keyslen;i++){
				params[i] = map.get(keys[i])+"";
			}
			z = MyJdbc.INSTANCE.update(Sqls.updateRoleSql, params,rrb);
			rrb.log(name+"角色添加成功", "1");
		}catch(Exception e){
			map.clear();
			rrb.log(name+"角色添加失败", "0");
			map.put("type", "error");
		}finally{
			Locks.rolewrLock.writeLock().unlock();
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
		Locks.rolewrLock.writeLock().lock();
		try{
			String sql = Sqls.deleteRoleSql;
			MyJdbc.INSTANCE.batchUpdate(sql, params, rrb);
			z = 1;
			rrb.log("用户删除成功", "1");
		}catch(Exception e){
			z = 0;
			rrb.log("用户删除成功", "0");
		}finally{
			Locks.rolewrLock.writeLock().unlock();
		}
		return z;
	}
	
	public int roleSetUser(String userid[],String roleid,ReqResBean rrb){
		try {
			String delSql = Sqls.deleteSystemRoleUserSql;
			String insertSql = Sqls.insertSystemRoleUserSql;
			Object params[] = new Object[]{roleid};
			int i = MyJdbc.INSTANCE.update(delSql, params, rrb);
			List<Object[]> insertParam = new ArrayList<Object[]>();
			int useridLen = userid.length;
			for(int z=0;z<useridLen;z++){
				Object[] os = new Object[2];
				os[1] = userid[z];
				os[0] = roleid;
				insertParam.add(os);
			}
			MyJdbc.INSTANCE.batchUpdate(insertSql, insertParam, rrb);
			i = 1;
			rrb.log("角色对用户授权成功", "1");
			return i;
		} catch (Exception e) {
			rrb.log("角色对用户授权失败", "0");
			return 0;
		}
	}
	
	public int roleSetPermi(String permiid[],String roleid,ReqResBean rrb){
		try {
			String delSql = Sqls.deleteSystemRolePermiSql;
			String insertSql = Sqls.insertSystemRolePermiSql;
			Object params[] = new Object[]{roleid};
			int i = MyJdbc.INSTANCE.update(delSql, params, rrb);
			List<Object[]> insertParam = new ArrayList<Object[]>();
			int permiidLen = permiid.length;
			for(int z=0;z<permiidLen;z++){
				Object[] os = new Object[2];
				os[0] = permiid[z];
				os[1] = roleid;
				insertParam.add(os);
			}
			MyJdbc.INSTANCE.batchUpdate(insertSql, insertParam, rrb);
			i = 1;
			rrb.log("角色对用户授权成功", "1");
			return i;
		} catch (Exception e) {
			rrb.log("角色对用户授权失败", "0");
			return 0;
		}
	}
	
	public String roleGetPermis(String roleid,ReqResBean rrb){
		try{
			Object[] params = new Object[]{roleid};
			String sql = Sqls.getPermisssFromRole;
			List<RolePermi> userRoleList = MyJdbc.INSTANCE.queryForList(sql, params, RolePermi.class, rrb);
			return StaticBeans.gson.toJson(userRoleList);
		}catch(Exception e){
			return StaticBeans.gson.toJson(new ArrayList<RolePermi>());
		}
	}
	
	public String getDataFromUserid(String userid,String systemname,ReqResBean rrb){
		String getDataFromUseridSql = Sqls.getDataFromUseridSql;
		try {
			StringBuilder builder = new StringBuilder("");
			List<Object[]> oslist = MyJdbc.INSTANCE.queryForFieldsList(getDataFromUseridSql, new Object[]{userid,systemname}, new String[]{"roleid"});
			int oslistLen = oslist.size();
			for(int i=0;i<oslistLen;i++){
				builder.append(i == 0 ? (oslist.get(i)[0]+"") : (","+oslist.get(i)[0]));
			}
			return builder.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
		
	}
	
}









