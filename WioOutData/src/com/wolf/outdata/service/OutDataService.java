package com.wolf.outdata.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wolf.outdata.javabean.OutFields;

/**
 * 数据导出
 *
 */
public class OutDataService {

	//{"key":"key","data":[{"id":"123","name":"abc"}]};
	
	public static String excelPath = "E:/测试/testExcel";
	
//	public static void main(String[] args) {
//		String jsonData = "{\"key\":\"key\",\"data\":[{\"id\":\"123\",\"name\":\"abc\",\"type\":\"1\"}]}";
//		OutDataService outDataService = new OutDataService();
//		List<Map<String,Object>> fieldLm = new ArrayList<Map<String,Object>>();
//		Map<String,Object> map = new HashMap<String,Object>();
//		map.put("fieldName", "id");
//		map.put("fieldcname", "id");
//		fieldLm.add(map);
//		map = new HashMap<String,Object>();
//		map.put("fieldName", "name");
//		map.put("fieldcname", "name");
//		fieldLm.add(map);
//		outDataService.setField("key", fieldLm);
//		outDataService.createWorker(jsonData);
//	}
	
	public void outData(){
		List<Map<String,Object>> datas = getDataFromDB();
		List<Map<String,Object>> fieldLm = getFieldFromDB();
		setField("key", fieldLm);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("key", "key");
		map.put("data", datas);
		createWorker(map);
	}
	
	private List<Map<String,Object>> getFieldFromDB(){
		
		return null;
	}
	
	private List<Map<String,Object>> getDataFromDB(){
		return null;
	}
	
	//public void createWorker(String jsonData){
	public void createWorker(Map<String,Object> map){
		//Map<String,Object> map = new Gson().fromJson(jsonData, new TypeToken<HashMap<String,Object>>(){}.getType());
		String key = map.get("key")+"";
		if(OutFields.fieldSet.containsKey(key)){
			Workbook wb = new SXSSFWorkbook(1000);
			Sheet sheet = wb.createSheet();
			List<OutFields> outFieldsList = OutFields.fieldSet.get(key);
			Row row = sheet.createRow(0);
			int outFieldsListLen = outFieldsList.size();
			for(int i=0;i<outFieldsListLen;i++){
				OutFields outFields = outFieldsList.get(i);
				String fieldCname = outFields.getFieldCname();
				Cell cell = row.createCell(i);
				cell.setCellValue(fieldCname);
			}
			List<Object> lists = (List<Object>)map.get("data");
			int listLen = lists.size();
			for(int j=0;j<listLen;j++){
				row = sheet.createRow(j+1);
				Map<String,Object> dataMap = (Map<String,Object>)lists.get(j);
				for(int i=0;i<outFieldsListLen;i++){
					OutFields outFields = outFieldsList.get(i);
					Cell cell = row.createCell(i);
					String fieldName = outFields.getFieldName();
					String data = dataMap.get(fieldName)+"";
					cell.setCellValue(data);
				}
			}
			String fileName = UUID.randomUUID().toString()+".xlsx";
			fileName = fileName.replace("-", "");
			String filePath = excelPath+"/"+fileName;
			File file = new File(filePath);
			FileOutputStream fos = null;
			try {
				if(file.createNewFile()){
					fos = new FileOutputStream(file);
					wb.write(fos);
					fos.flush();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if(fos!=null){
						fos.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			return;
		}
	}
	
	public void setField(String key,List<Map<String,Object>> fieldLm){
		if(OutFields.fieldSet.containsKey(key)){
			List<OutFields> outFieldsList = OutFields.fieldSet.get(key);
			for(Map<String,Object> map : fieldLm){
				for(OutFields outFields : outFieldsList){
					if(map.get("id")!=null && !map.get("id").equals("")){
						if(map.get("id").equals(outFields.getId())){
							outFields
							.setFieldName(map.get("fieldName")+"")
							.setFieldCname(map.get("fieldcname")+"")
							.setFieldLen(map.get("fieldLen")+"")
							.setCannull(Boolean.parseBoolean(map.get("cannull")+""))
							.setNulldata(map.get("nulldata")+"");
							break;
						}
					}
				}
			}
		}else{
			List<OutFields> OutFieldsList = new ArrayList<OutFields>();
			for(Map<String,Object> map : fieldLm){
				OutFieldsList.add(new OutFields()
					.setFieldName(map.get("fieldName")+"")
					.setFieldCname(map.get("fieldcname")+"")
					.setFieldLen(map.get("fieldLen")+"")
					.setCannull(Boolean.parseBoolean(map.get("cannull")+""))
					.setNulldata(map.get("nulldata")+"")
				);
			}
			OutFields.fieldSet.put(key, OutFieldsList);
		}
	}
	
}
