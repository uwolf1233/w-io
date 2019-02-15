package com.wolf.outin.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.wolf.javabean.ReqResBean;
import com.wolf.jdbcs.MyJdbc;
import com.wolf.outin.dao.OutInDao;
import com.wolf.outin.javabean.OutInBean;
import com.wolf.outin.javabean.OutInDbsBean;
import com.wolf.outin.javabean.OutInFieldsBean;

public class OutInService {
	
	private OutInService(){}
	
	public static OutInService INSTANCE = new OutInService();
	
	public void createWorker(String name,ReqResBean rrb){
		Object[] os = OutInDao.INSTANCE.getSetData(name, rrb);
		if(os == null){
			throw new RuntimeException("error");
		}
		List<OutInFieldsBean> oifbList = (List<OutInFieldsBean>)os[1];
		OutInDbsBean odb = (OutInDbsBean)os[2];
		String tableName = odb.getTablename();
		StringBuilder fieldNames = new StringBuilder("");//组装sql查询字段
		
		Workbook wb = new SXSSFWorkbook(1000);
		Sheet sheet = wb.createSheet();
		Row row = sheet.createRow(0);
		
		int oifbListSize = oifbList.size();
		for(int i=0;i<oifbListSize;i++){
			OutInFieldsBean oifb = oifbList.get(i);
			fieldNames.append(i == 0 ? oifb.getFieldname() : (","+oifb.getFieldname()));
			String fieldCname = oifb.getFieldcname();
			Cell cell = row.createCell(i);
			cell.setCellValue(fieldCname);
		}
		String countSql = "select count(*) c from " + tableName;
		
		long count = MyJdbc.INSTANCE.queryCount(countSql, null, "c", rrb);//总行数
		long pageSize = 100;//每页最大100
		long totalPage = count % pageSize == 0 ? count/pageSize : (count/pageSize+1);//总页数
		
		int z = 0;
		for(int a=1;a<=totalPage;a++){
			long curPage = a;//当前页
			long pageindex = (curPage-1)*pageSize;//当前页首行
			
			//当前系统所有表用id作为唯一标识，可自行扩展
			String outSql = "select " + fieldNames.toString() + " from " + tableName + " order by id offset ? rows fetch next ? rows only ";
			List<Map<String,Object>> dataLm = MyJdbc.INSTANCE.queryForMapList(outSql, new Object[]{pageindex,pageSize}, rrb);
			int dataLmSize = dataLm.size();
			
			for(int i=0;i<dataLmSize;i++){
				Map<String,Object> map = dataLm.get(i);
				
				row = sheet.createRow(z+1);
				
				for(int j=0;j<oifbListSize;j++){//map放里面保证顺序
					OutInFieldsBean oifb = oifbList.get(j);
					String fieldname = oifb.getFieldname();
					
					Cell cell = row.createCell(j);
					
					for(String key : map.keySet()){
						if(fieldname.equals(key)){//如果为当前字段
							String str = map.get(key)+"";
							if(isNumeric(str)){
								double d = Double.parseDouble(map.get(key)+"");
								cell.setCellValue(d);//设置数据
							}else{
								cell.setCellValue(str);//设置数据
							}
						}
					}
				}
				z++;
			}
		}
		String fileName = UUID.randomUUID().toString().replace("-", "")+"-"+name+".xlsx";
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			wb.write(baos);
			baos.flush();
			wb = null;
			byte[] bytes = baos.toByteArray();
			rrb.outFile(bytes, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(baos!=null){
					baos.close();
					baos = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		System.out.println(isNumeric("1000.02"));
	}
	
	/**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    private static boolean isNumeric(String str){
    	//采用正则表达式的方式来判断一个字符串是否为数字，这种方式判断面比较全
        //可以判断正负、整数小数

        boolean isInt = Pattern.compile("^-?[1-9]\\d*$").matcher(str).find();
        boolean isDouble = Pattern.compile("^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)$").matcher(str).find();

        return isInt || isDouble;
    }
	
	public List<OutInFieldsBean> getFieldData(String tablename,ReqResBean rrb){
		return OutInDao.INSTANCE.getFieldData(tablename, rrb);
	}
	
	public int saveSet(OutInBean oib,OutInDbsBean oidb,List<OutInFieldsBean> oifbs,ReqResBean rrb){
		return OutInDao.INSTANCE.saveSet(oib, oidb, oifbs, rrb);
	}
	
	public List<OutInDbsBean> getAllOutInSet(ReqResBean rrb){
		return OutInDao.INSTANCE.getAllOutInSet(rrb);
	}
	
}











