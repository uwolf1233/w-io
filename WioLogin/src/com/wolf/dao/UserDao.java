package com.wolf.dao;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.javabean.WioThreadPool;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.result.GetResult;
import com.wolf.result.GetResults;
import com.wolf.result.KeyGetResult;
import com.wolf.result.KeyGetResults;
import com.wolf.serconfig.Configs;
import com.wolf.server.KeyClient;
import com.wolf.sqls.Sqls;

public class UserDao {
	
	private UserDao(){
	}
	
	public static UserDao INSTANCE = new UserDao();
	
	public String getUserId(String username,ReqResBean rrb){
		try {
			String getUserIdFromUserNameSql = Sqls.getUserIdFromUserNameSql;
			String userid = MyJdbc.INSTANCE.queryForFields(getUserIdFromUserNameSql, new Object[]{username}, new String[]{"id"})[0]+"";
			return userid;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	//异步模式
//	public int boolUser(final String pwd,final String username,final SessionLine sessionLine,
//			final ReqResBean rrb) {
//		//long startTime = System.currentTimeMillis();
//		final BlockingQueue<Integer> waitloginqueue = new SynchronousQueue<Integer>();//验证是否登录过
//		final BlockingQueue<Boolean> waitpwdqueue = new SynchronousQueue<Boolean>();//验证密码是否正确
//		final BlockingQueue<Boolean> waitdbqueue = new SynchronousQueue<Boolean>();//数据库操作是否有问题
//		String getUserPwdSql = Sqls.getUserPwdSql;
//		final Object uid[] = MyJdbc.INSTANCE.queryForFields(getUserPwdSql, new Object[]{username}, new String[]{"id","pwd","status","logusersid","sessionid"});//获取用户id
//		if(uid[0]!=null && !uid[0].equals("")){
//			final String userid = uid[0]+"";
//			String status = uid[2]+"";
//			if(status.equals("1")){ //如果已经登录过，判断session是否还有效
//				if(userid.trim().equals("")){
//					throw new RuntimeException("登录异常,当前用户未注册");
//				}
//				if(uid[3] == null){//登录记录表没有数据
//					//执行登录
//					waitloginqueue.offer(1);
//				}else{//登录记录表有数据
//					if(uid[4] == null && uid[3]!=null){//如果有数据但没有session记录
//						//执行登录
//						waitloginqueue.offer(1);
//					}else if(uid[4] != null && uid[3]!=null){//如果有数据而且有session记录
//						String sessionid = uid[4]+"";
//						sessionLine.setSessionId(sessionid);
//						sessionLine.hasSession(new GetResults() {
//
//							@Override
//							public Object getObjectData(Object o) {
//								try {
//									if(o == null || o.equals("sessionNull")){//当前session已经失效
//										//执行登录
//										waitloginqueue.put(1);
//									}else{//已经登录过了
//										//不需要执行登录
//										waitloginqueue.put(2);
//									}
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//								return null;
//							}
//						});
//					}
//				}
//			}else{
//				try {
//					waitloginqueue.put(1);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			byte[] bytes = (byte[])uid[1];
//			KeyClient.dmcrypt(bytes,new KeyGetResults() {
//
//				@Override
//				public void getString(String str) {
//					try {
//						if(!str.equals(pwd)){//判断密码是否正确
//							waitpwdqueue.put(false);
//						}else{
//							waitpwdqueue.put(true);
//						}
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				
//			});
//			WioThreadPool.pool.execute(new Login_DB(userid, username, sessionLine, uid, new GetResults() {
//
//				@Override
//				public boolean isBooleanData(boolean booleanData) {
//					waitdbqueue.put(booleanData);
//					return true;
//				}
//				
//			}, rrb));
//		}else{
//			waitloginqueue.offer(-2);
//		}
//		int r = -1;
//		try {//这里不要用判断的方式取队列数据，否则容易导致数据库连接先关闭的问题，这里采用一定时间的最终同步，如果要做长时间，选用take
//			int waitlogin = waitloginqueue.poll(3, TimeUnit.SECONDS);
//			Boolean waitpwd = waitpwdqueue.poll(3, TimeUnit.SECONDS);
//			Boolean waitdb = waitdbqueue.poll(3, TimeUnit.SECONDS);
//			if(waitlogin == 1 && waitpwd!=null && waitpwd && waitdb!=null && waitdb){//采用最终同步
//				r = 1;
//			}else if(waitlogin == 2){
//				r = 2;
//			}else if(waitlogin == -2){
//				r = -1;
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			r = -1;
//		}
//		//long endTime = System.currentTimeMillis();
//		//System.out.println("1时长---"+(endTime-startTime));
//		return r;
//	}
//	
//	class Login_DB implements Runnable{
//
//		private String userid;
//		private String username;
//		private SessionLine sessionLine;
//		private Object uid[];
//		private ReqResBean rrb;
//		private GetResult getResult;
//		
//		public Login_DB(String userid,String username,SessionLine sessionLine,Object uid[],GetResult getResult,ReqResBean rrb){
//			this.userid = userid;
//			this.username = username;
//			this.sessionLine = sessionLine;
//			this.uid = uid;
//			this.rrb = rrb;
//			this.getResult = getResult;
//		}
//		
//		
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			Map<String,Object> usermap = new HashMap<String, Object>();
//			usermap.put("userid", userid);
//			usermap.put("username", username);
//			createSession(sessionLine, usermap);//获取创建session
//			String updateUsersStatusSql = Sqls.updateUsersStatusSql;
//			int m = MyJdbc.INSTANCE.update(updateUsersStatusSql, new Object[]{"1",userid}, rrb);
//			if(m > 0){
//				int a = 0;
//				if(uid[3]!=null){
//					String updateSessionSql = Sqls.updateSessionSql;
//					a = MyJdbc.INSTANCE.update(updateSessionSql, new Object[]{sessionLine.getSessionId(),userid}, rrb);//记入登录记录
//				}else{
//					String insertSessionSql = Sqls.insertSessionSql;
//					a = MyJdbc.INSTANCE.update(insertSessionSql, new Object[]{userid,sessionLine.getSessionId()} , rrb);//记入登录记录
//				}
//				if(a == 0){
//					getResult.isBooleanData(false);
//				}else{
//					getResult.isBooleanData(true);//登录成功
//				}
//			}else{
//				getResult.isBooleanData(false);
//			}
//		}
//		
//	}
	
	//同步模式(异步模式有未知bug，同步模式经postman 500并发压测比较稳定，响应时间20ms-80ms之间，大部分在40ms左右)
	public int boolUser(String pwd,String username,SessionLine sessionLine,ReqResBean rrb) {//-1为登录失败,0为密码错误,1为登录成功，凡是登录过的，非当前session重新登录
		long starttime = System.currentTimeMillis();
		int z = -1;
		boolean isUpdateUserSession = false;//判断登录记录是需要新增还是修改,false是新增，true是修改
		try {
			//String usercountSql = Sqls.usercountSql;
			//long count = MyJdbc.INSTANCE.queryCount(usercountSql, new Object[]{username}, "c", rrb);//先判断用户是否存在
			String getUserPwdSql = Sqls.getUserPwdSql;
			Object uid[] = MyJdbc.INSTANCE.queryForFields(getUserPwdSql, new Object[]{username}, new String[]{"id","pwd","status","logusersid","sessionid"});//获取用户id
			if(uid[0]!=null && !uid[0].equals("")){
				String userid = uid[0]+"";
				String status = uid[2]+"";
				boolean canLogin = false;
				if(status.equals("1")){//如果已经登录过，判断session是否还有效
					if(userid.trim().equals("")){
						throw new RuntimeException("登录异常,当前用户未注册");
					}
					if(uid[3] == null){//登录记录表没有数据
						isUpdateUserSession = false;
						canLogin = true;
					}else{//登录记录表有数据
						if(uid[4] == null && uid[3]!=null){//如果有数据但没有session记录
							isUpdateUserSession = true;
							canLogin = true;
						}else if(uid[4] != null && uid[3]!=null){
							String sessionid = uid[4]+"";
							sessionLine.setSessionId(sessionid);
							Object o = sessionLine.hasSessionRun(null);
							if(o == null || o.equals("sessionNull")){//当前session已经失效
								isUpdateUserSession = true;
								canLogin = true;
							}else{
								z = 2;//已经登录过了
								canLogin = false;
								sessionLine.setSessionId(sessionid);
								isUpdateUserSession = false;
							}
						}
					}
				}else{
					canLogin = true;
					isUpdateUserSession = false;
				}
				if(canLogin){
					byte[] bytes = (byte[])uid[1];
					String s = KeyClient.dmcryptRun(bytes,null);
					if(!s.equals(pwd)){//判断密码是否正确
						z = 0; 
					}else{
						Map<String,Object> usermap = new HashMap<String, Object>();
						usermap.put("userid", userid);
						usermap.put("username", username);
						createSession(sessionLine, usermap);//获取创建session
						String updateUsersStatusSql = Sqls.updateUsersStatusSql;
						int m = MyJdbc.INSTANCE.update(updateUsersStatusSql, new Object[]{"1",userid}, rrb);
						if(m > 0){
							int a = 0;
							if(isUpdateUserSession){
								String updateSessionSql = Sqls.updateSessionSql;
								a = MyJdbc.INSTANCE.update(updateSessionSql, new Object[]{sessionLine.getSessionId(),userid}, rrb);//记入登录记录
							}else{
								String insertSessionSql = Sqls.insertSessionSql;
								a = MyJdbc.INSTANCE.update(insertSessionSql, new Object[]{userid,sessionLine.getSessionId()} , rrb);//记入登录记录
							}
							
							if(a == 0){
								z = -1;
							}else{
								z = 1;//登录成功
							}
						}else{
							z = -1;
						}
					}
				}
			}else{
				z = -1;
			}
			long endtime = System.currentTimeMillis();
			System.out.println("3执行时间---"+(endtime-starttime));
			return z;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("登录异常");
		}
	}
	
	public void createSession(SessionLine sessionLine,Map<String,Object> map){
		sessionLine.create(map);
		if(sessionLine.isOvertime()){
			throw new RuntimeException("登录超时");
		}
	}
	
	public void saveSessionId(String sessionid,String userid,ReqResBean rrb){
		try {
			String insertSessionSql = Sqls.insertSessionSql;
			MyJdbc.INSTANCE.update(insertSessionSql, new Object[]{userid,sessionid}, rrb);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void saveUser(String userid,String username,String pwd,String systemname,ReqResBean rrb){
		if(username == null || username.trim().equals("")){
			throw new RuntimeException("用户名不能为空");
		}
		if(pwd == null || pwd.trim().equals("")){
			throw new RuntimeException("密码不能为空");
		}
		if(pwd.length() < 6){
			throw new RuntimeException("密码太短");
		}
		String saveUserSql = Sqls.saveUserSql;
		String id = userid;
		try {
			byte[] pwdbytes = KeyClient.emcryptRun(pwd,null);
			if(pwdbytes!=null){
				MyJdbc.INSTANCE.update(saveUserSql, new Object[]{id,username,pwdbytes,"0"}, rrb);
			}else{
				throw new RuntimeException("注册失败");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("注册失败");
		}
	}
	
	public int outUser(String username,ReqResBean rrb){//本地事务
		Connection con = null;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			con.setAutoCommit(false);
			String getUserIdFromUserNameSql = Sqls.getUserIdFromUserNameSql;
			String userid = MyJdbc.INSTANCE.queryForFields(getUserIdFromUserNameSql, new Object[]{username}, new String[]{"id"})[0]+"";//获取用户id
			String updateUsersNameStatusSql = Sqls.updateUsersNameStatusSql;
			int i = MyJdbc.INSTANCE.updateCurrentTran(updateUsersNameStatusSql, new Object[]{"0",username}, con);
			String removeSessionSql = Sqls.removeSessionSql;
			MyJdbc.INSTANCE.updateCurrentTran(removeSessionSql, new Object[]{userid}, con);
			con.commit();
			return i;
		}catch(Exception e){
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				return 0;
			}
			return 0;
		}finally{
			try {
				if(con != null){
					con.close();
					con = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}





