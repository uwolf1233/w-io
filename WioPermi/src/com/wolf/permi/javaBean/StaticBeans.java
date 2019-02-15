package com.wolf.permi.javaBean;

import com.google.gson.Gson;

public class StaticBeans {

	public static Gson gson = new Gson();
	
	public static String jsonStringHandle(String json){
		json = json.replace("\\", "");
		if(json.indexOf("\"") == 0){
			json = json.substring(1, json.length()-1);
		}
		return json;
	}
	
}
