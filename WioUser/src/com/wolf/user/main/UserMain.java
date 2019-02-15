package com.wolf.user.main;

import java.io.InputStream;

import com.wolf.serconfig.Configs;
import com.wolf.server.LogServer;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;
import com.wolf.user.controller.UserController;
import com.wolf.user.dao.UserDao;

public class UserMain {

	public static void main(String[] args) {
		InputStream in = LogServer.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器，注册器必须在配置读取之前执行
		OtherServer.mains(in);//运行
		Configs.INSTANCE.dbconfig();//运行数据库配置
		RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		UserDao.INSTANCE.getUsers(null);
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new UserController());
	}
	
}
