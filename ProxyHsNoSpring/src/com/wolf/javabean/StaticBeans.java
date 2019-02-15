package com.wolf.javabean;

import com.google.gson.Gson;

public class StaticBeans {

	public static Gson gson = new Gson();
	
	public static String jsonStringHandle(String json){
		json = json.replace("\\", "").replace("\"[", "[").replace("]\"", "]");
		if(json.indexOf("\"") == 0){
			json = json.substring(1, json.length()-1);
		}
		return json;
	}
	
}
