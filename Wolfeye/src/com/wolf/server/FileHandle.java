package com.wolf.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.wolf.javabean.LogBean.LogDatas;

public class FileHandle {

	public static String slogfilePath = "G:/wioFiles/filehandle/logfile/slog";//日志文件路径
	public static String slogfileindex = "G:/wioFiles/filehandle/logfile/slogindex";//日志文件索引路径
	
	public static String elogfilePath = "G:/wioFiles/filehandle/logfile/elog";//日志文件路径
	public static String elogfileindex = "G:/wioFiles/filehandle/logfile/elogindex";//日志文件索引路径
	
	private FileHandle(){}
	
	public static FileHandle INSTANCE = new FileHandle();
	
	public static void create(){
		System.out.println("正在创建或连接日志文件....");
		File file1 = new File(slogfilePath);
		File file2 = new File(slogfileindex);
		File file3 = new File(elogfilePath);
		File file4 = new File(elogfileindex);
		try {
			if(!file1.exists()){
				file1.mkdir();
			}
			if(!file2.exists()){
				file2.mkdir();
			}
			if(!file3.exists()){
				file3.mkdir();
			}
			if(!file4.exists()){
				file4.mkdir();
			}
			System.out.println("创建或连接日志文件成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("创建或连接日志文件失败");
		}
	}
	
	public File createFile(String mainId,String type){
		try {
			File file = null;
			if(type.equals("1")){
				file = new File(slogfilePath+"/"+mainId+".txt");
			}else if(type.equals("0")){
				file = new File(elogfilePath+"/"+mainId+".txt");
			}
			if(!file.exists()){
				file.createNewFile();
			}
			return file;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//写日志
	public void writeFile(File file,LogDatas datas,String mainId,String type){
		if(file!=null && file.exists()){
			String parentId = datas.getLogs().getParentId();
			String id = datas.getLogs().getId();
			String time = datas.getLogs().getTime();
			String message = datas.getLogs().getMessage();
			String path = datas.getLogs().getPath();
			String serverName = datas.getLogs().getServerName();
			String ip = datas.getLogs().getId();
			FileWriter fw = null;
			try{
				fw = new FileWriter(file,true);
				StringBuilder builder = new StringBuilder()
				.append("-start-"+"\r\n")
				.append("mainId:"+mainId+"\r\n")
				.append("parentId:"+parentId+"\r\n")
				.append("id:"+id+"\r\n")
				.append("time:"+time+"\r\n")
				.append("message:"+message+"\r\n")
				.append("path:"+path+"\r\n")
				.append("serverName:"+serverName+"\r\n")
				.append("ip:"+ip+"\r\n")
				.append("-end-"+"\r\n")
				.append("\r\n");
				createIndex(type.equals("0") ? elogfileindex : slogfileindex, id, mainId, time, path, ip,builder.toString());
				fw.write(builder.toString());
				fw.flush();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try {
					if(fw!=null){
						fw.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public List<String> getLogToString(String filePath){
		File file = new File(filePath);
		if(!file.exists()){
			return null;
		}
		List<String> list = new ArrayList<String>();
		StringBuilder builder = null;
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s = "";
			boolean isend = true;
			while((s = br.readLine())!=null){
				if(s.equals("-start-")){//如果是开始
					builder = new StringBuilder();
					isend = false;
					continue;
				}else if(s.equals("-end-")){//如果是结束
					list.add(builder.toString());
					builder = null;
					isend = true;
					continue;
				}
				if(!isend){
					builder.append(s);//如果包含在里面，则写入
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fr!=null){
					fr.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(br!=null){
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public List<String> getLogToHtml(String filePath){
		File file = new File(filePath);
		if(!file.exists()){
			return null;
		}
		List<String> list = new ArrayList<String>();
		StringBuilder builder = null;
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s = "";
			boolean isend = true;
			while((s = br.readLine())!=null){
				if(s.equals("-start-")){//如果是开始
					builder = new StringBuilder();
					isend = false;
					continue;
				}else if(s.equals("-end-")){//如果是结束
					builder.append("<br></br>");
					list.add(builder.toString());
					builder = null;
					isend = true;
					continue;
				}
				if(!isend){
					builder.append(s+"<br></br>");//如果包含在里面，则写入
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fr!=null){
					fr.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(br!=null){
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	private ReentrantReadWriteLock wrLock = new ReentrantReadWriteLock(false);
	
	//生成索引
	public void createIndex(String indexpath,String id,String mainId,String time,String path,String ip,String message){
		IKAnalyzer analyzer = null;
		analyzer = new IKAnalyzer();
		Directory directory = null;
		IndexWriter indexWriter = null;
		wrLock.writeLock().lock();
		try {
			directory =	FSDirectory.open(Paths.get(indexpath));
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, config);
			Document document = new Document();
			document.add(new TextField("id",id,Store.YES));
			document.add(new TextField("mainId",mainId,Store.YES));
			document.add(new TextField("time",time,Store.YES));
			document.add(new TextField("path",path,Store.YES));
			document.add(new TextField("ip",ip,Store.YES));
			document.add(new TextField("message",message,Store.YES));
			indexWriter.addDocument(document);
			indexWriter.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(indexWriter!=null){
					indexWriter.close();
					indexWriter = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wrLock.writeLock().unlock();
		}
	}
	
	public List<List<String>> readFile(String filePath){
		List<List<String>> listlist = new ArrayList<List<String>>();
		try {
			File file = new File(filePath);
			BufferedReader bf= new BufferedReader(new FileReader(file));
			String lineTxt = null;
			List<String> list = null;
			while((lineTxt = bf.readLine()) != null){
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			return listlist;
		}
	}
	
	//递归查找
	@SuppressWarnings("finally")
	public Object searchIndex(String inputData,String type) {
		String indexPath = type.equals("0") ? elogfileindex : slogfileindex;
		List<List<String>> listlist = new ArrayList<List<String>>();
		String[] inputDatas = inputData.split(" ");
		int inputDatasLen = inputDatas.length;
		String[] fields = new String[inputDatasLen];
		Occur[] occ = new Occur[inputDatasLen];
		for(int i=0;i<inputDatasLen;i++){
			fields[i] = "content";
			occ[i] = Occur.MUST;
		}
		Directory directory = null;
		IKAnalyzer analyzer = null;
		DirectoryReader ireader = null;
		IndexSearcher isearcher = null;
		try {
			directory = FSDirectory.open(Paths.get(indexPath));
			analyzer = new IKAnalyzer();
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			Query query = MultiFieldQueryParser.parse(inputDatas,fields,occ,analyzer);
			TopDocs hits = isearcher.search(query, 10);
			List<String> list = null;
			for(ScoreDoc hitDocs : hits.scoreDocs){
				Document hitDoc = isearcher.doc(hitDocs.doc);
				String content = hitDoc.get("message");
				String contents[] = content.split("\r\n");
				for(String line : contents){
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				if(directory!=null){
					directory.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(analyzer!=null){
				analyzer.close();
			}
			try {
				if(ireader!=null){
					ireader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return listlist;
		}
	}
	
//	if(line.contains("普通日志")||line.contains("异常日志")){
//		listlist.add(list);
//		list = new ArrayList<String>();
//		if(line.contains("普通日志")){
//			list.add("普通日志");
//			list.add(line);
//		}else if(line.contains("异常日志")){
//			list.add("异常日志");
//			list.add(line);
//		}
//	}else{
//		list.add(line);
//	}
	
}
