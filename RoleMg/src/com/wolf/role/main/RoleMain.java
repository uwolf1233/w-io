package com.wolf.role.main;

import com.wolf.role.controller.RoleMgController;
import com.wolf.role.dao.RolesDao;
import com.wolf.serconfig.Configs;
import com.wolf.server.OtherServer;
import com.wolf.server.RefController;

public class RoleMain {

	public static void main(String[] args) {
		String path = RoleMain.class.getClass().getResource("/").getPath()+"/WebConfig.xml";
		regController();
		Configs.INSTANCE.dbconfig();
		RolesDao.INSTANCE.getRoles();
		OtherServer.mains(path);
	}
	
	public static void regController(){//注册
		RefController.INSTANCE.putoList(new RoleMgController());
	}
	
}
