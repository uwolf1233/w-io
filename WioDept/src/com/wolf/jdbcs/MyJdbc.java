package com.wolf.jdbcs;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wolf.dept.javaBean.IdMoneyToMoney;
import com.wolf.javabean.ReqResBean;
import com.wolf.serconfig.Configs;

public class MyJdbc {
	
	private MyJdbc(){}
	
	public static MyJdbc INSTANCE = new MyJdbc();
	
	public long queryCount(String sql,Object[] params,String countname,ReqResBean rrb){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		long count = 0;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			rs = ps.executeQuery();
			rs.next();
			count = rs.getBigDecimal(countname).longValue();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(con!=null){
					con.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(ps != null){
					ps.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return count;
	}
	
	public <T> List<T> queryForList(String sql,Object[] params,Class clazz,ReqResBean rrb){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		if(clazz == null){
			throw new RuntimeException("clazz is null");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<T> list = null;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int rc = rsmd.getColumnCount();
			//Class clazz = t.getClass();
			Field[] fields = clazz.getDeclaredFields();
			list = new ArrayList<T>();
			while(rs.next()){
				T t = (T)clazz.newInstance();
				list.add(t);
				for(int i=1;i<=rc;i++){
					String field = rsmd.getColumnLabel(i);
					for(Field cfield : fields){
						if(cfield.getName().toLowerCase().equals(field.toLowerCase())){
							cfield.setAccessible(true);
							cfield.set(t, rs.getObject(field));
							break;
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		return list;
	}
	
	public <T> T queryForObj(String sql,Object[] params,Class clazz,ReqResBean rrb){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		if(clazz == null){
			throw new RuntimeException("clazz is null");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int rc = rsmd.getColumnCount();
			//Class clazz = t.getClass();
			Field[] fields = clazz.getDeclaredFields();
			if(rs.next()){
				T t = (T)clazz.newInstance();
				for(int i=1;i<=rc;i++){
					String field = rsmd.getColumnLabel(i);
					for(Field cfield : fields){
						if(cfield.getName().equals(field)){
							cfield.setAccessible(true);
							cfield.set(t, rs.getObject(field));
							break;
						}
					}
				}
				return t;
			}else{
				return null;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("查询异常");
		}finally{
			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	public void initIdMoneyToMoney(String sql,Object[] params,Class clazz,String key,String value,String moneyidsKey,ReqResBean rrb){//初始化中的多对多,id对应
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		if(clazz == null){
			throw new RuntimeException("clazz is null");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			rs = ps.executeQuery();
			Field[] fields = clazz.getDeclaredFields();
			Map<String,Object[]> map = new HashMap<String,Object[]>();
			ResultSetMetaData rsmd = rs.getMetaData();
			int rc = rsmd.getColumnCount();
			while(rs.next()){
				String keyData = rs.getString(key);//本系统id内置为String
				Object o = null;
				List<String> cidskey = null;
				if(map.containsKey(keyData)){
					cidskey = (List<String>)map.get(keyData)[1];
				}else{
					o = clazz.newInstance();
					cidskey = new ArrayList<String>();
					Object[] os = new Object[2];
					os[0] = o;
					os[1] = cidskey;
					map.put(keyData, os);
				}
				cidskey.add(rs.getString(moneyidsKey));
			}
			for(String mapKey : map.keySet()){
				Object[] os = map.get(mapKey);
				Object o = os[0];
				List<String> ids = (List<String>)os[1];
				IdMoneyToMoney idMTM = (IdMoneyToMoney)o;
				idMTM.setId(mapKey);
				idMTM.setIds(ids.toArray(new String[ids.size()]));
				idMTM.findObj();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	private void setPs(PreparedStatement ps,Object[] params,boolean isBatch) throws Exception{
		int i = 1;
		for(Object o : params){
			if(o instanceof String){
				ps.setString(i, o.toString());
			}else if(o instanceof Long){
				ps.setLong(i, (Long)o);
			}else if(o instanceof Integer){
				ps.setInt(i, (Integer)o);
			}else if(o instanceof Short){
				ps.setShort(i, (Short)o);
			}else if(o instanceof BigDecimal){
				ps.setBigDecimal(i, (BigDecimal)o);
			}else if(o instanceof Date){
				ps.setDate(i, (Date)o);
			}else if(o instanceof Time){
				ps.setTime(i, (Time)o);
			}else if(o instanceof Timestamp){
				ps.setTimestamp(i, (Timestamp)o);
			}else if(o instanceof byte[]){
				ps.setBytes(i, (byte[])o);
			}
			i++;
		}
		if(isBatch){
			ps.addBatch();
		}
	}
	
	public int update(String sql,Object[] params,ReqResBean rrb){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		Connection con = rrb == null ? null : rrb.getCon();
		PreparedStatement ps = null;
		int num = 0;
		try{
			if(con==null){//表示没有事务
				con = Configs.INSTANCE.ds.getConnection(); 
			}
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			num = ps.executeUpdate();
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rrb.getCon() == null){//表示没有事务
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
		return num;
	}
	
	/**
	 * 本地事务，分布式事务千万不要用这种
	 * @param sql
	 * @param params
	 * @return
	 */
	public int updateCurrentTran(String sql,Object[] params,Connection con){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		PreparedStatement ps = null;
		int num = 0;
		try{
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			num = ps.executeUpdate();
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return num;
	}
	
	public int[] batchUpdate(String sql,List<Object[]> params,ReqResBean rrb){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		Connection con = rrb == null ? null : rrb.getCon();
		PreparedStatement ps = null;
		int num = 0;
		try{
			if(con==null){//表示没有事务
				con = Configs.INSTANCE.ds.getConnection(); 
			}
			ps = con.prepareStatement(sql);
			if(params!=null){
				for(Object[] os : params){
					setPs(ps, os,true);
				}
			}
			return ps.executeBatch();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(rrb.getCon() == null){//表示没有事务
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
	
	public int javaBeanSaveEx(Object javabean,String tableName,ReqResBean rrb){
		Connection con = rrb == null ? null : rrb.getCon();
		PreparedStatement ps = null;
		try {
			if(javabean == null || tableName == null || tableName.trim().equals("")){
				return 0;
			}
			String wh = "";
			String fieldnames = "";
			Class clazz = javabean.getClass();
			Field[] fields = clazz.getDeclaredFields();
			Object[] os = new Object[fields.length];
			int fieldsLen = fields.length;
			for(int i=0;i<fieldsLen;i++){
				String fileName = fields[i].getName();
				wh += i == 0 ? "?" : ",?";
				fieldnames += i == 0 ? fileName : (","+fileName);
				fields[i].setAccessible(true);
				os[i] = fields[i].get(javabean);
			}
			String sql = "insert into " + tableName + "(" + fieldnames + ") values(" + wh + ")";
			int num = 0;
			if(con==null){//表示没有事务
				con = Configs.INSTANCE.ds.getConnection(); 
			}
			ps = con.prepareStatement(sql);
			setPs(ps, os,false);
			num = ps.executeUpdate();
			return num;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("");
		}finally{
			if(rrb.getCon() == null){//表示没有事务
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
			try {
				if(ps != null){
					ps.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public <T> int javaBeanListSaveEx(List<T> javabeans,String tableName,ReqResBean rrb) throws Exception {
		Connection con = rrb == null ? null : rrb.getCon();
		PreparedStatement ps = null;
		try {
			if(con==null){//表示没有事务
				con = Configs.INSTANCE.ds.getConnection(); 
			}
			System.out.println(con.getAutoCommit());
			String fieldnames = "";
			for(Object javabean : javabeans){
				String wh = "";
				if(javabean == null || tableName == null || tableName.trim().equals("")){
					continue;
				}
				Class clazz = javabean.getClass();
				Field[] fields = clazz.getDeclaredFields();
				Object[] fieldO = new Object[fields.length];
				int fieldsLen = fields.length;
				for(int i=0;i<fieldsLen;i++){
					String fileName = fields[i].getName();

					wh += i == 0 ? "?" : ",?";
					fieldnames += i == 0 ? fileName : (","+fileName);
					
					fields[i].setAccessible(true);
					fieldO[i] = fields[i].get(javabean)+"";
				}
				fieldnames = fieldnames.toLowerCase();
				if(ps == null){
					String sql = "insert into " + tableName + "(" + fieldnames + ") values(" + wh + ")";
					ps = con.prepareStatement(sql);
				}
				setPs(ps, fieldO,true);
			}

			int[] z = ps.executeBatch();
			if(Arrays.asList(z).contains(0)){
				return 0;
			}else{
				return 1;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}finally{
			if(rrb.getCon() == null){//表示没有事务
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
			try {
				if(ps != null){
					ps.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Object[] queryForFields(String sql,Object[] params,String[] resultkey){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		Object[] os = new Object[resultkey.length];
		Connection con = null;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			rs = ps.executeQuery();
			if(rs.next()){
				int i = 0;
				for(String s : resultkey){
					Object data = rs.getObject(s);
					os[i] = data;
					i++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally{
			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		return os;
	}
	
}
