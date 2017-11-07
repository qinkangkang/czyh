package com.czyh.czyhweb.log;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Log {

	/**
	 * 操作模块编号 登录登出(1) 我的信息(2) 首页(3) 无线管理(4) 活动管理(5) 订单管理(6) 客户管理(7) 促销管理(8)
	 * 报表管理(9) 财务管理(10) 系统管理(11) 操作日志(20)
	 * 
	 * @return
	 */
	int module();

	/**
	 * 
	 * 日志信息
	 * 
	 * @return
	 */
	String message();

	/**
	 * 
	 * 日志记录等级
	 * 
	 * @return
	 */
	LogLevel level() default LogLevel.TRACE;

	/**
	 * 
	 * 是否覆盖包日志等级 1.为false不会参考level属性。 2.为true会参考level属性。
	 * 
	 * @return
	 */
	boolean override() default false;
}
