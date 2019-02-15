package com.wolf.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.wolf.server.ControllerProxy;
import com.wolf.server.HttpServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.DefaultFileRegion;

/**
 * 静态资源文件缓存
 * @author 
 *
 */
public class StaticFileCacheLinux {  

	private static ConcurrentMap<String, ByteBuf> staticFileMap = new ConcurrentHashMap<String,ByteBuf>();
	//private static UnpooledByteBufAllocator unpooledByteBufAllocator = new UnpooledByteBufAllocator(true);
	private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);
	
	public static void getStaticFile(){
		boolean islock;
		try {
			islock = lock.writeLock().tryLock(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			System.out.println("加载静态资源");
			if(staticFileMap.size() > 0){
				for(String s : staticFileMap.keySet()){
					//ReferenceCountUtil.release(staticFileMap.get(s));
					ByteBuf bf = staticFileMap.get(s);
					int refCnt = bf.refCnt();
					if(refCnt > 0){
						bf.release(refCnt);
					}
				}
			}
			String pFilePath = HttpServer.cpath+ControllerProxy.viewPath;
			File file = new File(pFilePath);
			if(file.exists() && file.isDirectory()){
				readStaticFile(pFilePath.replace("\\", "/"), file.listFiles());
			}else{
				System.out.println("error:路径:"+pFilePath+"找不到静态资源");
			}
			System.out.println("静态资源加载完毕");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(islock){
				lock.writeLock().unlock();
			}
		}
	}
	
	public static ByteBuf getFile(String filePath){
		boolean islock;
		try {
			islock = lock.readLock().tryLock(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			if(!staticFileMap.containsKey(filePath)){
				return null;
			}
			ByteBuf bf = staticFileMap.get(filePath).retain();
			return bf.duplicate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}finally{
			if(islock){
				lock.readLock().unlock();
			}
		}
	}
	
	@SuppressWarnings("static-access")
	private static void readStaticFile(String fPath,File[] files){//最初路径,路径
		if(files.length > 0){
			for(File file : files){
				if(file.isDirectory()){
					readStaticFile(fPath, file.listFiles());
				}else{
					byte[] bytes = readFile(file);
					if(bytes!=null && bytes.length>0){
						String abPath = file.getAbsolutePath();//获取绝对路径
						abPath = abPath.substring(abPath.indexOf(fPath)+fPath.length());
						if(abPath.endsWith(".html") || abPath.endsWith(".HTML")){
							abPath = abPath.substring(0, abPath.lastIndexOf("."));//替换掉后缀
						}
						abPath = abPath.replace("\\", "/");
//						ByteBuf bytebuf = unpooledByteBufAllocator.directBuffer();
//						bytebuf.writeBytes(bytes);
//						staticFileMap.put(abPath, bytebuf);//将数据放入堆外内存，提高传输效率
						
						staticFileMap.put(abPath, Unpooled.directBuffer().writeBytes(bytes));//将数据放入堆外内存，提高传输效率
					}
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	private static byte[] readFile(File file){
		FileInputStream fis = null;
		byte[] bytes = null;
		try{
			fis = new FileInputStream(file);
			bytes = new byte[fis.available()];
			int length = -1;
			while((length = fis.read(bytes))!=-1){
				fis.read(bytes, 0, length);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(fis != null){
					fis.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				return bytes;
			}
		}
	}
	
}
