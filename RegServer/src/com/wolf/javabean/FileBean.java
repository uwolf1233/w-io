package com.wolf.javabean;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;
import com.wolf.javabean.SystemNet.Files;

public class FileBean {

	private byte[] filedata;
	private String fileName;
	private long readlong;
	private long realAllLong;
	private String hz;
	
	public byte[] getFiledata() {
		return filedata;
	}
	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getReadlong() {
		return readlong;
	}
	public void setReadlong(long readlong) {
		this.readlong = readlong;
	}
	public long getRealAllLong() {
		return realAllLong;
	}
	public void setRealAllLong(long realAllLong) {
		this.realAllLong = realAllLong;
	}
	public String getHz() {
		return hz;
	}
	public void setHz(String hz) {
		this.hz = hz;
	}
	
	public static List<FileBean> filesToFileBean(List<Files> filesList){
		List<FileBean> fileBeans = new ArrayList<FileBean>();
		for(Files files : filesList){
			FileBean fb = new FileBean();
			ByteString bs =  files.getFiledata();
			byte[] bytes = bs.toByteArray();
			if(bytes.length == 0){
				continue;
			}
			fb.setFiledata(bytes);
			fb.setFileName(files.getFileName());
			fb.setHz(files.getHz());
			fb.setReadlong(files.getReadlong());
			fb.setRealAllLong(files.getRealAllLong());
			fileBeans.add(fb);
		}
		return fileBeans;
	}
	
}
