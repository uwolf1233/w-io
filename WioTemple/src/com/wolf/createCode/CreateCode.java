package com.wolf.createCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateCode {

	public static void main(String[] args) {
		try {
			new CreateCode().runs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";

	  String dbURL="jdbc:sqlserver://192.168.56.80:1433;DatabaseName=owerp";

	  String userName="sa";

	  String userPwd="123";
	
	public void runs() throws Exception{
		
		
		String modelpath = "H:/MeProject1/WioTemple/src/FileTemp";//目前写死
		
		String tempjspath = modelpath+"/modejs.txt";
		String tempControllerpath = modelpath+"/modelController.txt";
		String tempDaopath = modelpath+"/modelDao.txt";
		String tempServerpath = modelpath+"/modelServer.txt";
		String tempSqlpath = modelpath+"/modeSqls.txt";
		String tempJavaBeanpath = modelpath+"/modeljavabean.txt";
		String temphtmlpath = modelpath+"/owhtml.txt";
		
		String createendpath = "H:/MeProject1/WioTemple/src/createend";//代码生成到的文件夹
		String jspath = createendpath+"/js";
		String controllerpath = createendpath+"/controller";
		String daopath = createendpath+"/dao";
		String serverpath = createendpath+"/server";
		String sqlspath = createendpath+"/sqls";
		String javabeanpath = createendpath+"/javabean";
		String htmlpath = createendpath+"/html.txt";
		
		
		String tablenames[] = new String[]{"ow_procurementcosthi",
				"ow_wholesale","ow_wholesalehi","ow_salepriceset",
				"ow_salepricesethi","ow_wholesalesalepriceset",
				"ow_wholesalesalepricesethi","ow_immovableset",
				"ow_procurementcostcontrol",
				"ow_procurementcostcontrolhi",
				"ow_profit","ow_profithi"};//表名
		String modelcnames[] = new String[]{"商品单品采购成本设置历史",
				"商品批发成本设置","商品批发成本设置历史","商品单品销售设置",
				"商品单品销售设置历史","商品单品批发销售设置","商品单品批发销售设置历史",
				"不动产设置","商品采购成本控制","商品采购成本控制历史",
				"商品利润控制","商品利润控制历史"};//模块中文名
		String modelnames[] = new String[]{"procurementcosthi",
				"wholesale","wholesalehi","salepriceset",
				"salepricesethi","wholesalesalepriceset",
				"wholesalesalepricesethi","immovableset",
				"procurementcostcontrol","procurementcostcontrolhi",
				"profit","profithi"};//模块英文名
		String sysname = "ow";//系统前缀
		String systype = "erp";//系统类型
		String inputsearchcs[] = new String[]{"根据供应商编号,商品条码以及销售方式编号搜索",
				"根据供应商编号,商品条码以及销售方式编号搜索",
				"根据供应商编号,商品条码以及销售方式编号搜索",
				"根据供应商编号,商品条码以及销售方式编号搜索",
				"根据供应商编号,商品条码以及销售方式编号搜索",
				"根据供应商编号,商品条码以及销售方式编号搜索",
				"根据供应商编号,商品条码以及销售方式编号搜索",
				"商品条码搜索",
				"根据供应商编号和商品条码搜索",
				"根据供应商编号和商品条码搜索",
				"根据供应商编号和商品条码搜索",
				"根据供应商编号和商品条码搜索"};//模糊查询搜索中文名 
		String searchfields[] = new String[]{"suppliercode,goodsbarcode,saletypecode",
				"suppliercode,goodsbarcode,saletypecode",
				"suppliercode,goodsbarcode,saletypecode",
				"suppliercode,goodsbarcode,saletypecode",
				"suppliercode,goodsbarcode,saletypecode",
				"suppliercode,goodsbarcode,saletypecode",
				"suppliercode,goodsbarcode,saletypecode",
				"goodsbarcode",
				"suppliercode,goodsbarcode",
				"suppliercode,goodsbarcode",
				"suppliercode,goodsbarcode",
				"suppliercode,goodsbarcode"};//模糊查询字段
		String importpath = "com.wolf.am";
		
		File pathfile = new File(modelpath);
		if(!pathfile.exists()){
			return;
		}
		File createpathfile = new File(createendpath);
		if(!createpathfile.exists()){
			return;
		}
		int i = 0;
		for(String tablename : tablenames){
			String getDBtableSql = "select b.name fieldname,c.name fieldtype from sysobjects a,syscolumns b,systypes c where a.id=b.id "
					+"and a.name='"+tablename+"' and a.xtype='U' and b.xtype=c.xtype";
			String modelcname = modelcnames[i];
			String modelname = modelnames[i];
			String modeloname = modelname.substring(0, 1).toUpperCase() + modelname.substring(1);
			String inputsearchc = inputsearchcs[i];
			String java_searchfield = searchfields[i];
			StringBuilder java_searchfieldBuilder = new StringBuilder("");
			String[] searchfieldArray = java_searchfield.split(",");
			String js_searchfield = "{";
			int z = 0;
			for(String s : searchfieldArray){
				js_searchfield+="'"+s+"':"+modelname+"inputsearch,";
				java_searchfieldBuilder.append(z==0?("\""+s+"\""):(",\""+s+"\""));
				z++;
			}
			java_searchfield = java_searchfieldBuilder.toString();
			js_searchfield+="'type':'inputsearch'}";
			StringBuilder js_colsBuilder = new StringBuilder("");
			StringBuilder addAndUpdateJson_fieldBuilder = new StringBuilder("");
			StringBuilder mapToBeanBuilder = new StringBuilder("");
			StringBuilder javabeanprivateBuilder = new StringBuilder("");
			StringBuilder javabeangetsetBuilder = new StringBuilder("");
			
			List<Map<String,String>> dataLm = getTableData(getDBtableSql);
			int a = 0;
			for(Map<String,String> map : dataLm){
				String fieldname = map.get("fieldname")+"";
				js_colsBuilder.append("{field:'"+fieldname+"', width:100, title: '"+fieldname+"'}");
				js_colsBuilder.append(a!=(dataLm.size()-1) ? ",\r\n" : "\r\n");
				addAndUpdateJson_fieldBuilder.append("+'<div class=\"layui-form-item\"><label class=\"layui-form-label\"></label>'\r\n")
				.append("+'<div class=\"layui-input-block\">'\r\n")
				.append("+'<input type=\"text\" name=\""+fieldname+"\" placeholder=\"\" class=\"layui-input\">'\r\n")
				.append("+'</div></div>'\r\n");
				String fieldoname = fieldname.substring(0, 1).toUpperCase() + fieldname.substring(1);
				mapToBeanBuilder.append(".set"+fieldoname+"(map.get(\""+fieldname+"\"))");
				javabeanprivateBuilder.append("\tprivate String "+fieldname+";\r\n");//可以根据fieldtype分别，目前只有String，可自己扩展
				javabeangetsetBuilder.append("\tpublic String get"+fieldoname+"() {\r\n")
					.append("\t\treturn "+fieldname+";\r\n")
					.append("\t}\r\n")
					.append("\tpublic "+modeloname+" set"+fieldoname+"(String "+fieldname+") {\r\n")
					.append("\t\tthis."+fieldname+" = "+fieldname+";return this;\r\n")
					.append("\t}\r\n");
				a++;
			}
			String js_cols = js_colsBuilder.toString();
			String addAndUpdateJson_field = addAndUpdateJson_fieldBuilder.toString();
			String mapToBean = mapToBeanBuilder.append(";").toString();
			//------------js--------------
			File spathfile = new File(tempjspath);
			File createPath = new File(jspath);
			FileReader fr = new FileReader(spathfile);
			BufferedReader br = new BufferedReader(fr);
			FileWriter fw = new FileWriter(createPath+"/"+modelname+".js");
			filecopy(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,javabeanprivateBuilder.toString(),
					javabeangetsetBuilder.toString());
			fw.close();
			br.close();
			fr.close();
			//------------js--------------
			//------------cotroller--------------
			createPath = new File(controllerpath);
			spathfile = new File(tempControllerpath);
			fr = new FileReader(spathfile);
			br = new BufferedReader(fr);
			fw = new FileWriter(createPath+"/"+modeloname+"Controller.java");
			filecopy(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,javabeanprivateBuilder.toString(),
					javabeangetsetBuilder.toString());
			fw.close();
			br.close();
			fr.close();
			//------------cotroller--------------
			//------------server--------------
			createPath = new File(serverpath);
			spathfile = new File(tempServerpath);
			fr = new FileReader(spathfile);
			br = new BufferedReader(fr);
			fw = new FileWriter(createPath+"/"+modeloname+"Server.java");
			filecopy(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,javabeanprivateBuilder.toString(),
					javabeangetsetBuilder.toString());
			fw.close();
			br.close();
			fr.close();
			//------------server--------------
			//------------dao--------------
			createPath = new File(daopath);
			spathfile = new File(tempDaopath);
			fr = new FileReader(spathfile);
			br = new BufferedReader(fr);
			fw = new FileWriter(createPath+"/"+modeloname+"Dao.java");
			filecopy(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,javabeanprivateBuilder.toString(),
					javabeangetsetBuilder.toString());
			fw.close();
			br.close();
			fr.close();
			//------------dao--------------
			//------------javabean--------------
			createPath = new File(javabeanpath);
			spathfile = new File(tempJavaBeanpath);
			fr = new FileReader(spathfile);
			br = new BufferedReader(fr);
			fw = new FileWriter(createPath+"/"+modeloname+".java");
			filecopy(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,javabeanprivateBuilder.toString(),
					javabeangetsetBuilder.toString());
			fw.close();
			br.close();
			fr.close();
			//------------javabean--------------
			//------------sqls--------------
			createPath = new File(sqlspath);
			spathfile = new File(tempSqlpath);
			fr = new FileReader(spathfile);
			br = new BufferedReader(fr);
			fw = new FileWriter(createPath+"/"+modeloname+"Sqls.java");
			filecopy(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,javabeanprivateBuilder.toString(),
					javabeangetsetBuilder.toString());
			fw.close();
			br.close();
			fr.close();
			//------------sqls--------------
			//------------html--------------
			createPath = new File(htmlpath);
			spathfile = new File(temphtmlpath);
			fr = new FileReader(spathfile);
			br = new BufferedReader(fr);
			fw = new FileWriter(createPath,true);
			fileappend(br, fw, sysname, systype, modelname, modeloname, addAndUpdateJson_field,inputsearchc,
					js_cols,js_searchfield,importpath,mapToBean,java_searchfield,modelcname);
			fw.close();
			br.close();
			fr.close();
			//------------html--------------
			i++;
		}
	}
	
	private void filecopy(BufferedReader br,FileWriter fw,String sysname,String systype,
			String modelname,String modeloname,String addAndUpdateJson_field,String inputsearchc,
			String js_cols,String js_searchfield,String importpath,String mapToBeanField,
			String java_searchfield,String javabeanprivate,String javabeangetset) throws Exception{
		String jsline = "";
		while((jsline = br.readLine())!=null){
			jsline = jsline.replace("*{sysname}*", sysname).replace("*{systype}*", systype)
					.replace("*{modelname}*", modelname).replace("*{modeloname}*", modeloname)
					.replace("*{addAndUpdateJson_field}*", addAndUpdateJson_field)
					.replace("*{inputsearchc}*", inputsearchc).replace("*{js_cols}*", js_cols)
					.replace("*{js_searchfield}*", js_searchfield)
					.replace("*{importpath}*", importpath).replace("*{mapToBeanField}*", mapToBeanField)
					.replace("*{java_searchfield}*", java_searchfield)
					.replace("*{javabeanprivate}*", javabeanprivate)
					.replace("*{javabeangetset}*",javabeangetset);
			fw.write(jsline);
			fw.write("\r\n");
		}
	}
	
	private void fileappend(BufferedReader br,FileWriter fw,String sysname,String systype,
			String modelname,String modeloname,String addAndUpdateJson_field,String inputsearchc,
			String js_cols,String js_searchfield,String importpath,String mapToBeanField,
			String java_searchfield,String modelcname) throws Exception{
		fw.write("\r\n");
		fw.write("\r\n");
		String jsline = "";
		while((jsline = br.readLine())!=null){
			jsline = jsline.replace("*{sysname}*", sysname).replace("*{systype}*", systype)
					.replace("*{modelname}*", modelname).replace("*{modeloname}*", modeloname)
					.replace("*{addAndUpdateJson_field}*", addAndUpdateJson_field)
					.replace("*{inputsearchc}*", inputsearchc).replace("*{js_cols}*", js_cols)
					.replace("*{js_searchfield}*", js_searchfield)
					.replace("*{importpath}*", importpath).replace("*{mapToBeanField}*", mapToBeanField)
					.replace("*{java_searchfield}*", java_searchfield)
					.replace("*{modelcname}*", modelcname);
			fw.write(jsline);
			fw.write("\r\n");
		}
	}
	
	private List<Map<String,String>> getTableData(String sql) throws Exception{
		Class.forName(driverName);
		Connection con = DriverManager.getConnection(dbURL,userName,userPwd);
		PreparedStatement ps = con.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<Map<String,String>> dataLm = new ArrayList<Map<String,String>>();
		while(rs.next()){
			Map<String,String> map = new HashMap<String,String>();
			map.put("fieldname", rs.getString("fieldname"));
			map.put("fieldtype", rs.getString("fieldtype"));
			dataLm.add(map);
		}
		rs.close();
		ps.close();
		con.close();
		System.out.println(dataLm.toString());
		return dataLm;
	}
	
}
