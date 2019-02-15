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
import java.util.List;

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
				setPs(ps, params);
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
							cfield.set(t, rs.getObject(i));
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
	
	private void setPs(PreparedStatement ps,Object[] params) throws Exception{
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
			}
			i++;
		}
	}
	
}
