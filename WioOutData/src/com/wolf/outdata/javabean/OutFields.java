package com.wolf.outdata.javabean;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OutFields {

	private String id;
	private String fieldName;
	private String fieldCname;
	private String fieldLen;
	private boolean cannull;
	private String nulldata;//默认
	
	public static ConcurrentMap<String,List<OutFields>> fieldSet = new ConcurrentHashMap<String,List<OutFields>>();
	
	public int saveAndUpdate(Connection con){
		if(id == null){
			//save
		}else{
			//update
		}
		return 1;
	}
	
	public String getId() {
		return id;
	}
	public OutFields setId(String id) {
		this.id = id;
		return this;
	}
	public String getFieldName() {
		return fieldName;
	}
	public OutFields setFieldName(String fieldName) {
		this.fieldName = fieldName;return this;
	}
	public String getFieldLen() {
		return fieldLen;
	}
	public OutFields setFieldLen(String fieldLen) {
		this.fieldLen = fieldLen;return this;
	}
	public boolean isCannull() {
		return cannull;
	}
	public OutFields setCannull(boolean cannull) {
		this.cannull = cannull;return this;
	}
	public String getNulldata() {
		return nulldata;
	}
	public OutFields setNulldata(String nulldata) {
		this.nulldata = nulldata;return this;
	}

	public String getFieldCname() {
		return fieldCname;
	}

	public OutFields setFieldCname(String fieldCname) {
		this.fieldCname = fieldCname;return this;
	}
	
	
}
