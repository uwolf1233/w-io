package com.wolf.outin.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.outin.javabean.OutInBean;
import com.wolf.outin.javabean.OutInDbsBean;
import com.wolf.outin.javabean.OutInFieldsBean;
import com.wolf.outin.sql.Sqls;

public class OutInDao {

	private OutInDao(){}
	
	public static OutInDao INSTANCE = new OutInDao();
	
	public int saveOutInSet(OutInBean outInBean,List<OutInFieldsBean> outInFieldsBeanList,OutInDbsBean odb,ReqResBean rrb){
		try {
			int i = MyJdbc.INSTANCE.javaBeanSaveEx(outInBean, "outin", rrb);
			if(i > 0){
				i = MyJdbc.INSTANCE.javaBeanListSaveEx(outInFieldsBeanList, "outinfields", rrb);
				if(i > 0){
					i = MyJdbc.INSTANCE.javaBeanSaveEx(odb, "outindbs", rrb);
				}
			}
			return i;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public Object[] getSetData(String name,ReqResBean rrb){
		try {
			String outinSql = Sqls.outinSql;
			String outinfieldsSql = Sqls.outinfieldsSql;
			String outindbsSql = Sqls.outindbsSql;
			OutInBean oib = MyJdbc.INSTANCE.queryForObj(outinSql, new Object[]{name}, OutInBean.class, rrb);
			List<OutInFieldsBean> oifbList = MyJdbc.INSTANCE.queryForList(outinfieldsSql, new Object[]{oib.getOutinid()}, OutInFieldsBean.class, rrb);
			OutInDbsBean odb = MyJdbc.INSTANCE.queryForObj(outindbsSql, new Object[]{oib.getOutinid()}, OutInDbsBean.class, rrb);
			Object[] os = new Object[]{oib,oifbList,odb};
			return os;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public int[] saveData(List<OutInFieldsBean> list,List<Object[]> params,String tablename,ReqResBean rrb){
		int z = 0;
		Object[] title = null;
		for(Object[] os : params){
			if(z == 0){//表头
				title = params.get(z);
				z++;
				continue;
			}
			int i = 0;
			for(Object o : os){
				OutInFieldsBean oifb = list.get(i);
				if(o == null){
					if(oifb.getNulldata()!=null){
						Object co = oifb.getNulldata();
						if(co.equals("#UUID")){//默认值类型可以自己扩展,最好由java去做处理
							os[i] = UUID.randomUUID().toString().replace("-", "");
						}
						else{
							os[i] = co;
						}
					}else if(oifb.getCannull().equals("1")){
						throw new RuntimeException("数据异常，"+oifb.getFieldcname()+"不允许为空");
					}else{
						o = "";//此处可以知道，非字符串类型最好设置默认值
					}
				}else{
					String fieldlen = oifb.getFieldlen();
					if(fieldlen!=null && (o+"").length()>Integer.parseInt(fieldlen)){
						throw new RuntimeException("数据异常，"+oifb.getFieldcname()+"数据超长");
					}
				}
				i++;
			}
		}
		List<Object[]> ol = new ArrayList<Object[]>();
		int n = 0;
		for(Object[] os : params){
			if(n == 0){//表头
				n++;
				continue;
			}
			List<Object> oll = new ArrayList<Object>();
			for(Object o : os){
				if(o == null){
					continue;
				}
				oll.add(o);
			}
			ol.add(oll.toArray());
		}
		StringBuilder whBuilder = new StringBuilder("");
		StringBuilder fieldBuilder = new StringBuilder("");
		int j = 0;
		for(Object o : title){
			for(OutInFieldsBean oifb : list){
				if(!oifb.getFieldcname().equals(o)){
					continue;
				}
				fieldBuilder.append(j == 0 ? oifb.getFieldname() : (","+oifb.getFieldname()));
				whBuilder.append(j == 0 ? "?" : (",?"));
				j++;
				break;
			}
		}
		String sql = "insert into " + tablename + "("+fieldBuilder.toString()+") values("+whBuilder.toString()+")";
		try {
			return MyJdbc.INSTANCE.batchUpdate(sql, ol, rrb);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("数据异常");
		}
	}
	
	public List<OutInFieldsBean> getFieldData(String tablename,ReqResBean rrb){
		String getDBtableSql = Sqls.getDBtableSql;
		List<OutInFieldsBean> ofb = MyJdbc.INSTANCE.queryForListObj(getDBtableSql, new Object[]{tablename}, 
				OutInFieldsBean.class, rrb);
		return ofb;
	}
	
	public int saveSet(OutInBean oib,OutInDbsBean oidb,List<OutInFieldsBean> oifbs,ReqResBean rrb){
		try {
			int i = MyJdbc.INSTANCE.javaBeanSaveEx(oib, "outin", rrb);
			if(i>0){
				i = MyJdbc.INSTANCE.javaBeanSaveEx(oidb, "outindbs", rrb);
				if(i > 0){
					i = MyJdbc.INSTANCE.javaBeanListSaveEx(oifbs, "outinfields", rrb);
				}
			}
			if(i>0){
				return 1;
			}else{
				return 0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public List<OutInDbsBean> getAllOutInSet(ReqResBean rrb){
		//OutInDbsBean oidb = 
		String getAllOutInSetSql = Sqls.getAllOutInSetSql;
		List<OutInDbsBean> oidbList = MyJdbc.INSTANCE.queryForListObj(getAllOutInSetSql, null, OutInDbsBean.class, rrb);
		return oidbList;
	}
	
	public Object[] getSet(String name,ReqResBean rrb){
		Object[] os = new Object[3];
		String outinFromNameSql = Sqls.outinFromNameSql;
		OutInBean oib = MyJdbc.INSTANCE.queryForObj(outinFromNameSql, new Object[]{name}, OutInBean.class, rrb);
		if(oib!=null){
			os[0] = oib;
			String outindbsSql = Sqls.outindbsSql;
			OutInDbsBean oidb = MyJdbc.INSTANCE.queryForObj(outindbsSql, new Object[]{oib.getOutinid()}, OutInDbsBean.class, rrb);
			if(oidb!=null){
				os[1] = oidb;
				String outinfieldsSql = Sqls.outinfieldsSql;
				List<OutInFieldsBean> oifblist = MyJdbc.INSTANCE.queryForListObj(outinfieldsSql, 
						new Object[]{oib.getOutinid()}, OutInFieldsBean.class, rrb);
				if(oifblist!=null){
					os[2] = oifblist;
				}
			}
		}
		return os;
	}
	
}











