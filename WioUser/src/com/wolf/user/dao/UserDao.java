package com.wolf.user.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.user.javaBean.StaticBeans;
import com.wolf.user.javaBean.UserBean;
import com.wolf.user.javaBean.UserRole;
import com.wolf.user.lock.Locks;
import com.wolf.user.sql.Sqls;

public class UserDao {

	private UserDao(){}
	
	public static UserDao INSTANCE = new UserDao();
	
	public String getUserToJson(String[] ids,ReqResBean rrb){
		return UserBean.getJsonFromIds(ids, rrb);
	}
	
	public void getUsers(ReqResBean rrb){
		String sql = Sqls.getSystemUsersSql;
		Locks.userwrLock.writeLock().lock();
		try{
			MyJdbc.INSTANCE.queryForList(sql, null, UserBean.class,rrb);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Locks.userwrLock.writeLock().unlock();
		}
	}
	
	public String getAllUser(ReqResBean rrb){
		return UserBean.getAllRole(rrb);
	}
	
	public void addUser(Map<String,Object> map,ReqResBean rrb,String ids[]){
		int z = 0;
		Locks.userwrLock.writeLock().lock();
		String name = map.get("name")+"";
		try{
			String hasSql = Sqls.hasUserSql;
			long count = MyJdbc.INSTANCE.queryCount(hasSql, new Object[]{name}, "c", rrb);
			if(count > 0){
				map.clear();
				map.put("type", "error");
				map.put("data", "该用户已注册");
				rrb.log(name+"用户添加失败,该用户已注册", "0");
				return;
			}
			String keys[] = new String[]{"id","name","systemname","pid","isend"};
			Object params[] = new Object[keys.length];
			int keyslen = keys.length;
			for(int i=0;i<keyslen;i++){
				params[i] = map.get(keys[i]);
			}
			z = MyJdbc.INSTANCE.update(Sqls.insertUserSql, params,rrb);
			if(z > 0){
				map.put("type", "success");
				rrb.log(name+"用户添加成功", "1");
			}else{
				map.put("type", "error");
				rrb.log(name+"用户添加失败", "0");
			}
		}catch(Exception e){
			map.clear();
			rrb.log(name+"用户添加失败", "0");
			map.put("type", "error");
			map.put("data", "用户添加失败");
		}finally{
			Locks.userwrLock.writeLock().unlock();
		}
	}
	
	public void refreshUsers(ReqResBean rrb){
		boolean flag = false;
		try {
			flag = Locks.userwrLock.readLock().tryLock(5000, TimeUnit.SECONDS);
			if(flag){
				String sql = Sqls.getSystemUsersSql;
				UserBean.clean();
				MyJdbc.INSTANCE.queryForList(sql, null, UserBean.class,rrb);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally{
			if(flag){
				Locks.userwrLock.readLock().unlock();
			}
		}
	}
	
	public String getUserOneData(String id,ReqResBean rrb){
		return UserBean.getMapFromIds(new String[]{id}, rrb);
	}
	
	public UserBean getMapFromName(String name,String systemname,ReqResBean rrb){
		return UserBean.getMapFromName(name,systemname, rrb);
	}
	
	public int updateUser(Map<String,Object> map,ReqResBean rrb){
		int z = 0;
		Locks.userwrLock.writeLock().lock();
		String name = map.get("name")+"";
		try{
			String keys[] = new String[]{"name","isend","id","systemname"};
			Object params[] = new Object[keys.length];
			int keyslen = keys.length;
			for(int i=0;i<keyslen;i++){
				params[i] = map.get(keys[i])+"";
			}
			z = MyJdbc.INSTANCE.update(Sqls.updateUserSql, params,rrb);
			rrb.log(name+"用户添加成功", "1");
		}catch(Exception e){
			map.clear();
			rrb.log(name+"用户添加失败", "0");
			map.put("type", "error");
		}finally{
			Locks.userwrLock.writeLock().unlock();
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
		Locks.userwrLock.writeLock().lock();
		try{
			String sql = Sqls.deleteUserSql;
			z = MyJdbc.INSTANCE.update(sql, params.toArray(), rrb);
			rrb.log("用户删除成功", "1");
		}catch(Exception e){
			rrb.log("用户删除成功", "0");
		}finally{
			Locks.userwrLock.writeLock().unlock();
		}
		return z;
	}
	
	public int userSetDept(String deptid[],String userid,ReqResBean rrb){
		try {
			String delSql = Sqls.deleteSystemDeptUserSql;
			String insertSql = Sqls.insertSystemDeptUserSql;
			Object params[] = new Object[]{userid};
			int i = MyJdbc.INSTANCE.update(delSql, params, rrb);
			List<Object[]> insertParam = new ArrayList<Object[]>();
			int deptidLen = deptid.length;
			for(int z=0;z<deptidLen;z++){
				Object[] os = new Object[2];
				os[0] = deptid[z];
				os[1] = userid;
				insertParam.add(os);
			}
			MyJdbc.INSTANCE.batchUpdate(insertSql, insertParam, rrb);
			rrb.log("用户对部门授权成功", "1");
			return i;
		} catch (Exception e) {
			rrb.log("用户对部门授权失败", "0");
			return 0;
		}
	}
	
	public int userSetRole(String roleid[],String userid,ReqResBean rrb){
		try {
			String delSql = Sqls.deleteSystemRoleUserSql;
			String insertSql = Sqls.insertSystemRoleUserSql;
			Object params[] = new Object[]{userid};
			int i = MyJdbc.INSTANCE.update(delSql, params, rrb);
			List<Object[]> insertParam = new ArrayList<Object[]>();
			int roleidLen = roleid.length;
			for(int z=0;z<roleidLen;z++){
				Object[] os = new Object[2];
				os[0] = roleid[z];
				os[1] = userid;
				insertParam.add(os);
			}
			MyJdbc.INSTANCE.batchUpdate(insertSql, insertParam, rrb);
			i = 1;
			rrb.log("用户对角色授权成功", "1");
			return i;
		} catch (Exception e) {
			rrb.log("用户对角色授权失败", "0");
			return 0;
		}
	}
	
	public String userGetRoles(String userid,ReqResBean rrb){
		try{
			Object[] params = new Object[]{userid};
			String sql = Sqls.getRolessFromUser;
			List<UserRole> userRoleList = MyJdbc.INSTANCE.queryForList(sql, params, UserRole.class, rrb);
			return StaticBeans.gson.toJson(userRoleList);
		}catch(Exception e){
			return StaticBeans.gson.toJson(new ArrayList<UserRole>());
		}
	}
	
}
