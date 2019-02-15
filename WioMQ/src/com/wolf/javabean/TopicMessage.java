package com.wolf.javabean;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.ByteString;

public class TopicMessage {
	
	private String id;
	private ByteString byteString;
	private int status;
	private Publisher publisher;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ByteString getByteString() {
		return byteString;
	}
	public void setByteString(ByteString byteString) {
		this.byteString = byteString;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Publisher getPublisher() {
		return publisher;
	}
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
}
