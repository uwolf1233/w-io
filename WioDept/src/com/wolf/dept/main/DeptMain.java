package com.wolf.dept.main;

import java.io.InputStream;

import com.wolf.dept.controller.DeptController;
import com.wolf.dept.dao.DeptDao;
import com.wolf.dept.javaBean.DeptBean;
import com.wolf.serconfig.Configs;
import com.wolf.server.LogServer;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class DeptMain {

	public static void main(String[] args) {
		//String path = DeptMain.class.getClass().getResource("/").getPath()+"/WebConfig.xml";
		InputStream in = LogServer.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器
		OtherServer.mains(in);//运行
		Configs.INSTANCE.dbconfig();//运行数据库配置
		RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		DeptDao.INSTANCE.getDeptTypes(null);//先获取属性和类型
		DeptDao.INSTANCE.getDeptPropertys(null);
		DeptDao.INSTANCE.getDepts(null);
		DeptBean.setOtherData();//关联起来
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new DeptController());
	}
	
}
