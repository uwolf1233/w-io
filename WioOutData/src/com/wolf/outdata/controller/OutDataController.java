package com.wolf.outdata.controller;

import java.util.Map;

import com.wolf.javabean.ReqResBean;
import com.wolf.server.MethodMapping;

public class OutDataController {

	@MethodMapping(path="/test",interfacetype="h",threadPoolNumMax=2)
	public String test1(ReqResBean rrb,Map<String,String> sessionMap){
		return "/Test/Test.html";
	}
	
	
	
}
