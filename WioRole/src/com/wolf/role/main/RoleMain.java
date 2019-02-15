package com.wolf.role.main;

import java.io.InputStream;

import com.wolf.role.controller.RoleController;
import com.wolf.role.dao.RoleDao;
import com.wolf.serconfig.Configs;
import com.wolf.server.LogServer;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class RoleMain {

	public static void main(String[] args) {
		InputStream in = LogServer.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器
		OtherServer.mains(in);//运行
		Configs.INSTANCE.dbconfig();//运行数据库配置
		RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		RoleDao.INSTANCE.getRole(null);
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new RoleController());
	}
	
}
