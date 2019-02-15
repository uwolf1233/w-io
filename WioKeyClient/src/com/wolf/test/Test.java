package com.wolf.test;

import java.util.concurrent.BlockingQueue;

import com.wolf.result.KeyGetResults;
import com.wolf.server.KeyClient;

public class Test {

	public static void main(String[] args) {
		final KeyClient kc = new KeyClient();
		new Thread(kc).start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		kc.emcrypt("abcdf31412",new KeyGetResults() {

			@Override
			public void getBytes(byte[] bytes) {
				System.out.println("加密后的数据"+bytes);
				kc.dmcrypt(bytes, new KeyGetResults() {

					@Override
					public void getString(String str) {
						System.out.println("解密后的数据"+str);
					}
					
				});
			}
			
		});
//		String s = kc.dmcrypt(bytes);
//		System.out.println(s);
	}
	
}
