package com.wolf.javabean;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.ByteString;

public class QueueMessage {

	private String id;
	private ByteString byteString;
	private int status;
	private Producer producer;
	
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
	public Producer getProducer() {
		return producer;
	}
	public void setProducer(Producer producer) {
		this.producer = producer;
	}
	
}
