package com.wolf.keys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.imageio.stream.FileImageInputStream;

import com.google.protobuf.ByteString;
import com.wolf.javabean.Key.Datas;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class StringHandle extends ChannelInboundHandlerAdapter{
	
	private static SecretKey ckey = null;
	
	private static String keyPath = "H:/key";//密钥保存路径
	
	private static String keyFileName = "/MiYao.txt";//密钥文件
	
	public static void init() throws Exception{
		KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
		keyGenerator.init(168);//168位
		SecretKey secretKey = keyGenerator.generateKey();//生成key的材料
		byte[] key = secretKey.getEncoded();
		
		DESedeKeySpec deKeySpec = new DESedeKeySpec(key);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
		ckey = factory.generateSecret(deKeySpec);//密钥
		if(!saveKey()){//证明没有过
			ckey = null;
			getKey();
		}
	}
	
	//将密钥保存到文件中
	public static boolean saveKey(){
		String keycode = HexBin.encode(ckey.getEncoded());
		File pathfile = new File(keyPath);
		if(!pathfile.exists()){
			throw new RuntimeException("找不到路径:"+keyPath);
		}
		File file = new File(keyPath+keyFileName);
		if(file.exists()){
			return false;
		}else{
			OutputStream outputStream = null;
			try {
				file.createNewFile();
				outputStream = new FileOutputStream(file);
				outputStream.write(keycode.getBytes());
				outputStream.flush();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("密钥文件生成失败");
			}finally{
				if(outputStream!=null){
					try {
						outputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					outputStream = null;
				}
			}
		}
	}
	
	//从文件中读取密钥
	public static void getKey(){
		File pathfile = new File(keyPath);
		if(!pathfile.exists()){
			throw new RuntimeException("找不到路径:"+keyPath);
		}
		File file = new File(keyPath+keyFileName);
		if(file.exists()){
			InputStream inputStream = null;
			InputStreamReader ireader = null;
			BufferedReader br = null;
			try{
				inputStream = new FileInputStream(file);
				ireader = new InputStreamReader(inputStream);
				br = new BufferedReader(ireader);
				String str = "";
				StringBuilder builder = new StringBuilder("");
				while((str=br.readLine())!=null){
					builder.append(str);
				}
				String keystr = builder.toString();
				byte[] bytes = HexBin.decode(keystr);
				DESedeKeySpec deKeySpec = new DESedeKeySpec(bytes);
				SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
				ckey = factory.generateSecret(deKeySpec);//密钥
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException("获取解密文件失败");
			}finally{
				if(inputStream!=null){
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inputStream = null;
				}
				if(ireader!=null){
					try {
						ireader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ireader = null;
				}
				if(br!=null){
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					br = null;
				}
			}
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if(msg instanceof Datas){
				Datas datas = (Datas)msg;
				Datas.Builder builder = Datas.newBuilder();
				String type = datas.getType();
				String id = datas.getId();
				if(type.equals("2")){//加密字符串
					try {
						String data = datas.getEmcrypt().getData();
						ByteString bs = emcrypt(data);
						builder.setType("2");
						builder.getEmcryptBuilder().setRdata(bs);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						builder.setType("-1");
					}
					builder.setId(id);
					keyWriter(ctx.channel(), builder);
					System.out.println("加密完成");
				}else if(type.equals("3")){//解密字符串
					try {
						ByteString bs = datas.getDmcrypt().getData();
						String s = dmcrypt(bs);
						builder.setType("3");
						builder.getDmcryptBuilder().setRdata(s);
						System.out.println("解密完成");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						builder.setType("-1");
					}
					builder.setId(id);
					keyWriter(ctx.channel(), builder);
				}
				else{
					//传递
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	private ByteString emcrypt(String data) throws Exception{//3DES加密
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");//算法类型/工作方式/填充方式
		cipher.init(Cipher.ENCRYPT_MODE,ckey);//加密模式
		byte[] bytes = cipher.doFinal(data.getBytes());
		ByteString bs = ByteString.copyFrom(bytes);
		return bs;
	}
	
	public String dmcrypt(ByteString bs) throws Exception{//3DES解密
		Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");//算法类型/工作方式/填充方式
		cipher.init(Cipher.DECRYPT_MODE, ckey);
		byte[] result = cipher.doFinal(bs.toByteArray());
		return new String(result);
	}
	
	private static int timeOut = 4;
	
	private boolean keyWriter(Channel channel,Datas.Builder builder){
		if(channel.isWritable()){//发送逻辑，为什么这么写，关键是预防堆外内存溢出
			channel.writeAndFlush(builder);
			return true;
		}else{
			try {
				int i = 0;
				while(!channel.isWritable()){
					Thread.sleep(4);
					if(i == timeOut*1000){
						break;
					}
					i++;
				}
				if(channel.isWritable()){
					channel.writeAndFlush(builder);
					return true;
				}else{
					return false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		}
	}
	
}
