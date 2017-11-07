package com.czyh.czyhweb.log.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.czyh.czyhweb.entity.TLogInfo;
import com.czyh.czyhweb.log.LogLevel;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.LogInfoService;

/**
 * 全局日志等级<包日志等级<类和方法日志等级
 */
public class Log4JDBCImpl extends LogAdapter {

	private LogLevel rootLogLevel = LogLevel.ERROR;

	@Autowired
	private LogInfoService logInfoService;

	private Map<String, LogLevel> customLogLevel = new HashMap<String, LogLevel>();

	/**
	 * 
	 * @param message
	 * @param objects
	 * @param logLevel
	 * @see com.ketayao.ketacustom.log.impl.LogAdapter#log(java.lang.String,
	 *      java.lang.Object[], com.ketayao.ketacustom.log.LogLevel)
	 */
	@Override
	public void log(int module, String message, Object[] objects, LogLevel logLevel) {
		MessageFormat mFormat = new MessageFormat(message);
		String result = mFormat.format(objects);

		if (StringUtils.isBlank(result)) {
			return;
		}
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		// result = shiroUser.toString() + ":" + result;

		TLogInfo logInfo = new TLogInfo();
		logInfo.setCreateTime(new Date());
		logInfo.setUserId(shiroUser.getId());
		logInfo.setUsername(shiroUser.getName());
		logInfo.setMessage(result);
		logInfo.setIpAddress(shiroUser.getIpAddress());
		logInfo.setLogLevel(logLevel);
		logInfo.setModule(module);

		logInfoService.save(logInfo);
	}

	public void setRootLogLevel(LogLevel rootLogLevel) {
		this.rootLogLevel = rootLogLevel;
	}

	/**
	 * 
	 * @return
	 * @see com.ketayao.ketacustom.log.LogTemplate#getRootLogLevel()
	 */
	@Override
	public LogLevel getRootLogLevel() {
		return rootLogLevel;
	}

	public void setCustomLogLevel(Map<String, LogLevel> customLogLevel) {
		this.customLogLevel = customLogLevel;
	}

	@Override
	public Map<String, LogLevel> getCustomLogLevel() {
		return customLogLevel;
	}

	public void setLogInfoService(LogInfoService logInfoService) {
		this.logInfoService = logInfoService;
	}

}