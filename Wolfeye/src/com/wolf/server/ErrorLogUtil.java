package com.wolf.server;

import java.io.File;

import com.wolf.interfaces.LogUtil;
import com.wolf.javabean.LogBean.LogDatas;

//异常的
public class ErrorLogUtil implements LogUtil{

	private ErrorLogUtil(){}
	
	public static ErrorLogUtil INSTANCE = new ErrorLogUtil();
	
	@Override
	public void write(LogDatas datas,String type) {
		// TODO Auto-generated method stub
		String mainId = datas.getLogs().getMainId();
		File file = FileHandle.INSTANCE.createFile(mainId, type);
		FileHandle.INSTANCE.writeFile(file, datas, mainId,type);
	}
	
}
