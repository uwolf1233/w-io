package com.wolf.im.main;

import java.io.InputStream;

import com.wolf.controller.Test1Controller;
import com.wolf.server.LogServer;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class ImMain {

	public static void main(String[] args) {
		InputStream in = ImMain.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器
		OtherServer.mains(in);
		//Configs.INSTANCE.dbconfig();//运行数据库配置
		//RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		//PermiDao.INSTANCE.getPermis();
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new Test1Controller());
	}
	
}
