package com.wolf.main;

import java.io.InputStream;

import com.wolf.controller.LoginController;
import com.wolf.controller.Server1Controller;
import com.wolf.serconfig.Configs;
import com.wolf.server.KeyClient;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class LoginMain {

	public static void main(String[] args) {
		InputStream in = LoginMain.class.getResourceAsStream("/WebConfig.xml");
		regController();//运行注册器，注册器必须在配置读取之前执行
		OtherServer.mains(in);
		Configs.INSTANCE.dbconfig();//运行数据库配置
		RefController.INSTANCE.setDbcon(Configs.INSTANCE);//将数据库连接获取接口加入wio
		new Thread(new KeyClient()).start();
		OtherServer.execServer();
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new Server1Controller());
		RefController.INSTANCE.putoList(new LoginController());
	}
	
}
