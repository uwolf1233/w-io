package com.wolf.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodMapping {

	String path() default "";//路径
	String lockNames() default "";//锁名称
	int secondnum() default 60;//锁时长
	long geWait() default 0;//获取锁等待时长
	boolean trans() default false;//是否启动事务
	int tranCommitMinute() default 10;//超时提交时长(秒)默认10秒
	int tranRollbackMinute() default 10;//超时回滚时长(秒)默认10秒
	int threadPoolNumMax() default 0;//路径负载配置，没有配置则使用全局负载
	boolean hasFile() default false;//路径是否包含文件
	boolean fileIn() default false;//文件是否到当前方法
	boolean isWebSocket() default false;//是否websocket
	String interfacetype() default "s";//s为内部接口，h为外部接口,w为websocket接口，sh为内外接口，作为接口分离，默认为内部
}
