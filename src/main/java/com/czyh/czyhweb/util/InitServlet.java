package com.czyh.czyhweb.util;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.czyh.czyhweb.service.ConfigurationService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.system.AppService;
import com.czyh.czyhweb.util.html.StaticHtmlManager;
import com.czyh.czyhweb.util.solr.SolrUtil;

/**
 * 一个初始化的servlet类，在应用启动的时候，做一些初始化的工作。
 */
public class InitServlet extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(InitServlet.class);

	private static final long serialVersionUID = 1L;

	private StaticHtmlManager staticHtmlManager = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Constant.init();
		Constant.loadWatermarkFile(config.getServletContext().getRealPath("/"));
		logger.warn("上传文件路径设置完成！");
		// SmsUtil.init();
		// logger.warn("短信网关初始化完成！");
		UserService userService = SpringContextHolder.getBean(UserService.class);
		userService.initDictionary();
		userService.initEventCategory();
		logger.warn("数据字典缓存初始化完成！");
		AppService appService = SpringContextHolder.getBean(AppService.class);
		appService.initAppChannelMap();
		logger.warn("应用栏目MAP缓存初始化完成！");
		staticHtmlManager = new StaticHtmlManager();
		staticHtmlManager.start();
		logger.warn("静态化异步任务线程池启动成功！");
		HttpClientUtil.init();
		logger.warn("搜索引擎配置启动成功！");
		SolrUtil.init();
		logger.warn("Http客户端类初始化完成！");
		ConfigurationService configurationService = SpringContextHolder.getBean(ConfigurationService.class);
		configurationService.initConfigurationMap();
		logger.warn("配置表缓存完成！");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	@Override
	public void destroy() {
		DictionaryUtil.clear();
		staticHtmlManager.interrupt();
		HttpClientUtil.destroy();
		logger.warn("Http客户端类卸载完成！");
		super.destroy();
	}
}