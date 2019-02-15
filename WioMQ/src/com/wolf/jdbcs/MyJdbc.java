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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.wolf.serconfig.Configs;

public class MyJdbc {
	
	private MyJdbc(){}
	
	public static MyJdbc INSTANCE = new MyJdbc();
	
	public <T> List<T> queryForList(String sql,Object[] params,Class clazz){
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
						if(cfield.getName().equals(field)){
							cfield.setAccessible(true);
							//cfield.set(t, rs.getObject(field));
							setField(cfield, rs, field,t);
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
	
	private <T> void setField(Field cfield,ResultSet rs,String fieldname,T t) throws Exception{
		String fieldType = cfield.getType().getName();
		Object o = rs.getObject(fieldname);
		if(fieldType.equals("java.lang.Long")){
			if(o != null && (o+"").contains("-") && (o+"").contains(":")){//被认为是时间格式
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.util.Date date = format.parse(o+"");
				o = date.getTime();
			}
			cfield.set(t, o == null ? "0" : Long.parseLong(o+""));
		}else if(fieldType.equals("java.lang.String")){
			cfield.set(t, rs.getObject(fieldname)+"");
		}else if(fieldType.equals("com.google.protobuf.ByteString")){
			byte[] bytes = (byte[])o;
			ByteString bs = ByteString.copyFrom(bytes);
			cfield.set(t, bs);
		}else{
			cfield.set(t, rs.getObject(fieldname));
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
	
	public List<Object[]> queryListForFields(String sql,Object[] params,String[] resultkey){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Object[]> listos = new ArrayList<Object[]>();
		Connection con = null;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			rs = ps.executeQuery();
			while(rs.next()){
				Object[] os = new Object[resultkey.length];
				int i = 0;
				for(String s : resultkey){
					Object data = rs.getObject(s);
					os[i] = data;
					i++;
				}
				listos.add(os);
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
		return listos;
	}
	
	public List<Map<String,Object>> queryForListMap(String sql,Object[] params){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Map<String,Object>> list = null;
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
			list = new ArrayList<Map<String,Object>>();
			while(rs.next()){
				Map<String,Object> map = new HashMap<String,Object>();
				list.add(map);
				for(int i=1;i<=rc;i++){
					String field = rsmd.getColumnLabel(i);
					Object o = rs.getObject(field);
					map.put(field, o);
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
	
	public int update(String sql,Object[] params){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		Connection con = null;
		PreparedStatement ps = null;
		int num = 0;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				setPs(ps, params,false);
			}
			num = ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(ps != null){
					ps.close();
					ps = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("数据库操作异常");
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
		return num;
	}
	
	public void batchUpdate(String sql,List<Object[]> params){
		if(sql == null || sql.equals("")){
			throw new RuntimeException("sql is empty");
		}
		Connection con = null;
		PreparedStatement ps = null;
		int num = 0;
		try{
			con = Configs.INSTANCE.ds.getConnection(); 
			ps = con.prepareStatement(sql);
			if(params!=null){
				for(Object[] os : params){
					setPs(ps, os,true);
				}
			}
			ps.executeBatch();
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("数据库操作异常");
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
