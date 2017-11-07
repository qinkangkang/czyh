package com.czyh.czyhweb.util.html;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.util.SpringContextHolder;

public class StaticHtmlExecutor implements Callable<String>, Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(StaticHtmlExecutor.class);

	private StaticHtmlBean staticHtmlBean;

	public StaticHtmlExecutor(StaticHtmlBean staticHtmlBean) {
		this.staticHtmlBean = staticHtmlBean;
	}

	@Override
	public String call() throws Exception {
		// 处理五次都有异常，就不再重复执行了
		if (staticHtmlBean.getCounter() < 5) {
			try {
				StaticHtmlService staticHtmlService = SpringContextHolder.getBean(StaticHtmlService.class);
				staticHtmlService.createStaticHtml(staticHtmlBean);
			} catch (ServiceException e) {
				logger.error("执行生成静态化页面出错，TemplateName：" + staticHtmlBean.getTemplateName() + "；ID："
						+ staticHtmlBean.getObjectId(), e);
				staticHtmlBean.addCounterOne();
				StaticHtmlManager.put(staticHtmlBean);
			}
		}
		return null;
	}
}