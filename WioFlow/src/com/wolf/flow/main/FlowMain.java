package com.wolf.flow.main;

import java.io.InputStream;

import com.wolf.dao.FlowDao;
import com.wolf.flow.controller.FlowController;
import com.wolf.serconfig.Configs;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class FlowMain {

	public static void main(String[] args) {
		//String path = DeptMain.class.getClass().getResource("/").getPath()+"/WebConfig.xml";
		InputStream in = FlowMain.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器
		OtherServer.mains(in);//运行
		Configs.INSTANCE.dbconfig();//运行数据库配置
		RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		FlowDao.INSTANCE.initFlow();
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new FlowController());
	}
	
}
