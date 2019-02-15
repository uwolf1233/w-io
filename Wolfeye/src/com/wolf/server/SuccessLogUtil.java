package com.wolf.server;

import java.io.File;

import com.wolf.interfaces.LogUtil;
import com.wolf.javabean.LogBean.LogDatas;

//正常的
public class SuccessLogUtil implements LogUtil{

	private SuccessLogUtil(){}
	
	public static SuccessLogUtil INSTANCE = new SuccessLogUtil();
	
	@Override
	public void write(LogDatas datas,String type) {
		// TODO Auto-generated method stub
		String mainId = datas.getLogs().getMainId();
		File file = FileHandle.INSTANCE.createFile(mainId, type);
		FileHandle.INSTANCE.writeFile(file, datas, mainId,type);
	}
	
}
