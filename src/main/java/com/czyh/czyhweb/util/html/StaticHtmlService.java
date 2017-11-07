package com.czyh.czyhweb.util.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.PropertiesUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * HTML内容静态化服务类.
 * 
 * 演示由Freemarker引擎生成的的html格式文件.
 * 
 * @author zgzhou
 */
@Component
public class StaticHtmlService {

	private static final String DEFAULT_ENCODING = "UTF-8";

	private static Logger logger = LoggerFactory.getLogger(StaticHtmlService.class);

	private Configuration freemarkerConfiguration = null;

	/**
	 * 生成静态化页面
	 */
	public void createStaticHtml(StaticHtmlBean staticHtmlBean) throws ServiceException {
		try {

			// 定义全路径，为了以后将相对路径和文件名添加进去
			StringBuilder rootPath = new StringBuilder(Constant.RootPath)
					.append(PropertiesUtil.getProperty("htmlRootPath")).append(staticHtmlBean.getRelativePath());

			Template template = freemarkerConfiguration.getTemplate(staticHtmlBean.getTemplateName(), DEFAULT_ENCODING);

			File file = new File(rootPath.toString());
			File dir = new File(rootPath.substring(0, rootPath.lastIndexOf("/")));
			FileUtils.forceMkdir(dir);
			Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			template.process(staticHtmlBean.getContentMap(), out);
			IOUtils.closeQuietly(out);
		} catch (IOException e) {
			logger.error("生成静态网页失败, FreeMarker模板不存在", e);
			throw new ServiceException("FreeMarker模板不存在", e);
		} catch (TemplateException e) {
			logger.error("生成静态网页失败, FreeMarker处理失败", e);
			throw new ServiceException("FreeMarker处理失败", e);
		} catch (Exception e) {
			logger.error("生成静态页失败", e);
			throw new ServiceException("FreeMarker页面静态化出错", e);
		}
	}

	/**
	 * 注入Freemarker引擎配置,构造Freemarker静态化页面模板.
	 */
	public void setFreemarkerConfiguration(Configuration freemarkerConfiguration) throws IOException {

		freemarkerConfiguration.setEncoding(Locale.CHINA, "UTF-8");
		freemarkerConfiguration.setTemplateUpdateDelayMilliseconds(0);
		freemarkerConfiguration.setDefaultEncoding("UTF-8");
		freemarkerConfiguration.setURLEscapingCharset("UTF-8");
		freemarkerConfiguration.setLocale(Locale.CHINA);
		freemarkerConfiguration.setBooleanFormat("true,false");
		freemarkerConfiguration.setDateFormat("yyyy-MM-dd");
		freemarkerConfiguration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
		freemarkerConfiguration.setTimeFormat("HH:mm:ss");
		freemarkerConfiguration.setNumberFormat("0.######");
		freemarkerConfiguration.setWhitespaceStripping(true);

		this.freemarkerConfiguration = freemarkerConfiguration;
	}
}
