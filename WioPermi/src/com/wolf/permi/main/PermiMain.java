package com.wolf.permi.main;

import java.io.InputStream;

import com.wolf.permi.controller.PermiController;
import com.wolf.permi.dao.PermiDao;
import com.wolf.serconfig.Configs;
import com.wolf.server.LogServer;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class PermiMain {

	public static void main(String[] args) {
		InputStream in = PermiMain.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器
		OtherServer.mains(in);
		Configs.INSTANCE.dbconfig();//运行数据库配置
		RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		//PermiDao.INSTANCE.getPermis();
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new PermiController());
	}
	
}
