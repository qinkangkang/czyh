package com.czyh.czyhweb.log;

import java.util.Map;

public interface LogAPI {
	void log(int module, String message, LogLevel logLevel);

	void log(int module, String message, Object[] objects, LogLevel logLevel);

	/**
	 * 
	 * 得到全局日志等级
	 * 
	 * @return
	 */
	LogLevel getRootLogLevel();

	/**
	 * 
	 * 得到自定义包的日志等级
	 * 
	 * @return
	 */
	Map<String, LogLevel> getCustomLogLevel();
}
