package com.wolf.outin.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wolf.javabean.FileBean;
import com.wolf.javabean.ReqResBean;
import com.wolf.javabean.SessionLine;
import com.wolf.outin.dao.OutInDao;
import com.wolf.outin.javabean.OutInBean;
import com.wolf.outin.javabean.OutInDbsBean;
import com.wolf.outin.javabean.OutInFieldsBean;
import com.wolf.outin.javabean.StaticBeans;
import com.wolf.outin.service.BigExcelReader;
import com.wolf.outin.service.OutInService;
import com.wolf.server.MethodMapping;

public class OutInController {

	@MethodMapping(path="/systemShow",interfacetype="h",threadPoolNumMax=2)
	public String test1(ReqResBean rrb,SessionLine sessionLine){
		return "/SystemShow/SystemShows";
	}
	
	@MethodMapping(path="/outin_saveOutInSet",interfacetype="h",threadPoolNumMax=5,trans=true)
	public void saveOutInSet(ReqResBean rrb,SessionLine sessionLine){
		String jsonData = rrb.getRequestMap().get("jsonData");
		Map<String,Object> map = StaticBeans.gson.fromJson(jsonData, new TypeToken<Map<String,Object>>(){}.getType());
		OutInBean oib = new OutInBean();
		oib.setName(map.get("name")+"");
		oib.setTypes(map.get("types")+"");
		oib.setOutinid(UUID.randomUUID().toString().replace("-", ""));
		
		OutInDbsBean odb = new OutInDbsBean();
		odb.setOutinid(oib.getOutinid());
		odb.setDatabases(map.get("databases")+"");
		odb.setTablename(map.get("tablename")+"");
		
		List<Map<String,Object>> list = (List<Map<String,Object>>)map.get("outinfields");
		int listSize = list.size();
		List<OutInFieldsBean> oifblist = new ArrayList<OutInFieldsBean>();
		for(int i=0;i<listSize;i++){
			Map<String,Object> omap = list.get(i);
			OutInFieldsBean oifb = new OutInFieldsBean();
			oifb.setId(UUID.randomUUID().toString().replace("-", ""));
			oifb.setOutinid(oib.getOutinid());
			oifb.setFieldname(omap.get("fieldname")+"");
			oifb.setFieldcname(omap.get("fieldcname")+"");
			oifb.setFieldlen(omap.get("fieldlen")+"");
			oifb.setCannull(omap.get("cannull")+"");
			oifb.setNulldata(omap.get("nulldata")+"");
			oifblist.add(oifb);
		}
		int i = OutInDao.INSTANCE.saveOutInSet(oib, oifblist,odb ,rrb);
		if(i > 0){
			rrb.thransThread(1);
			rrb.setResponseKV("type", "success");
		}else{
			rrb.thransThread(0);
			rrb.setResponseKV("type", "error");
		}
	}
	
	@MethodMapping(path="/outin_outExcel",interfacetype="h",threadPoolNumMax=5)
	public void outExcel(ReqResBean rrb,SessionLine sessionLine){
		String name = rrb.getRequestMap().get("name");
		if(name == null || name.trim().equals("")){
			rrb.setResponseKV("type", "error");
		}else{
			OutInService.INSTANCE.createWorker(name, rrb);
		}
	}
	
	@MethodMapping(path="/outin_inExcel",interfacetype="h",threadPoolNumMax=5,hasFile=true,fileIn=true)
	public void inExcel(final ReqResBean rrb,SessionLine sessionLine){
		//String tempPath = "E:/其他开发/文件上传测试";//临时存储目录，可自己定义
		List<FileBean> fileBeanList = rrb.getFileBean();
		int fileBeanListSize = fileBeanList.size();
		String name = rrb.getRequestMap().get("name");
		Object[] os = OutInDao.INSTANCE.getSetData(name, rrb);
		if(os == null){
			rrb.setResponseKV("type", "导入失败");
			return;
		}
		final List<OutInFieldsBean> oifbList = (List<OutInFieldsBean>)os[1];
		OutInDbsBean odb = (OutInDbsBean)os[2];
		final String tablename = odb.getTablename();
		
		for(int i=0;i<fileBeanListSize;i++){
			FileBean fb = fileBeanList.get(i);
			//String fileNames = fb.getFileName()+"."+fb.getHz();
			byte[] bytes = fb.getFiledata();
			InputStream in = null;
			final List<Object[]> olist = new ArrayList<Object[]>();
			try{
				in = new ByteArrayInputStream(bytes);
				BigExcelReader reader = new BigExcelReader(in) {//采用驱动模式导入，支持大数据量
					
					@Override
					protected void outputRow(String[] datas, int[] rowTypes,
							int rowIndex) {
						// TODO Auto-generated method stub
						// 此处输出每一行的数据
						olist.add(datas);
						if(olist.size() > 100){//每100条做一次插入
							OutInDao.INSTANCE.saveData(oifbList, olist, tablename, rrb);
							olist.clear();
						}
						//System.out.println(Arrays.toString(datas));
					}
					
				};
				reader.parse();
				if(olist.size()>0){
					OutInDao.INSTANCE.saveData(oifbList, olist, tablename, rrb);
					olist.clear();
				}
				rrb.setResponseKV("type", fb.getFileName()+":导入完成，请检查数据正确性");
			}catch(Exception e){
				e.printStackTrace();
				rrb.setResponseKV("type", fb.getFileName()+":"+e.getMessage());
			}finally{
				fileBeanList.clear();
				try {
					if(in != null){
						in.close();
						in = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@MethodMapping(path="/outin_getFieldData",interfacetype="h",threadPoolNumMax=2)
	public void getFieldData(ReqResBean rrb,SessionLine sessionLine){
		String tablename = rrb.getRequestMap().get("tablename");
		List<OutInFieldsBean> ofb = OutInService.INSTANCE.getFieldData(tablename, rrb);
		rrb.setResponseData(StaticBeans.gson.toJson(ofb));
	} 
	
	@MethodMapping(path="/outin_saveSet",interfacetype="h",threadPoolNumMax=2,trans=true)
	public void saveSet(ReqResBean rrb,SessionLine sessionLine){
		try {
			String jsonData = rrb.getRequestMap().get("jsonData");
			Map<String,Object> map = StaticBeans.gson.fromJson(jsonData, new TypeToken<Map<String,Object>>(){}.getType());
			List<Map<String,Object>> outInFieldsLm = (List<Map<String,Object>>)map.get("OutInFields");
			String tablename = map.get("tablename")+"";
			String tablecname = map.get("tablecname")+"";
			String types = map.get("types")+"";
			String name = map.get("name")+"";
			OutInBean oib = new OutInBean();
			oib.setOutinid(UUID.randomUUID().toString().replace("-", ""));
			oib.setTypes(types);
			oib.setName(name);
			OutInDbsBean oidb = new OutInDbsBean();
			oidb.setOutinid(oib.getOutinid());
			oidb.setTablename(tablename);
			oidb.setTablecname(tablecname);
			List<OutInFieldsBean> oifbs = new ArrayList<OutInFieldsBean>();
			for(Map<String,Object> maps : outInFieldsLm){
				OutInFieldsBean oifb = new OutInFieldsBean();
				oifb.setId(UUID.randomUUID().toString().replace("-", ""));
				oifb.setOutinid(oib.getOutinid());
				oifb.setFieldname(maps.get("fieldname")+"");
				oifb.setFieldcname(maps.get("fieldcname")+"");
				oifb.setFieldlen(maps.get("fieldlen")+"");
				oifb.setCannull(maps.get("cannull")+"");
				oifb.setNulldata(maps.get("nulldata")+"");
				oifbs.add(oifb);
			}
			if(OutInService.INSTANCE.saveSet(oib, oidb, oifbs, rrb) > 0){
				rrb.setResponseKV("type", "success");
				rrb.setResponseKV("data", "操作成功");
				rrb.thransThread(1);
			}else{
				rrb.setResponseKV("type", "error");
				rrb.setResponseKV("data", "操作失败");
				rrb.thransThread(0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rrb.thransThread(0);
		}
	}
	
	@MethodMapping(path="/outin_getAllOutInSet",interfacetype="h",threadPoolNumMax=2)
	public void getAllOutInSet(ReqResBean rrb,SessionLine sessionLine){
		List<OutInDbsBean> list = OutInService.INSTANCE.getAllOutInSet(rrb);
		rrb.setResponseKV("data", StaticBeans.gson.toJson(list));
	}
	
//	@MethodMapping(path="/outin_excelIn",interfacetype="h",threadPoolNumMax=3,trans=true)
//	public void excelIn(ReqResBean rrb,SessionLine sessionLine){
//		String tablename = rrb.getRequestMap().get("tablename");
//		List<FileBean> fblist = rrb.getFileBean();
//		
//	}
	
}











