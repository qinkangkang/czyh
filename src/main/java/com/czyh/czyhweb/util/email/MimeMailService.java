/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package com.czyh.czyhweb.util.email;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * MIME邮件服务类.
 * 
 * 演示由Freemarker引擎生成的的html格式邮件.
 * 
 * @author calvin
 */
public class MimeMailService {

	private static final String DEFAULT_ENCODING = "utf-8";

	private static Logger logger = LoggerFactory.getLogger(MimeMailService.class);

	private JavaMailSender mailSender;

	private Map<Integer, Template> templateMap = Maps.newHashMap();

	/**
	 * 发送MIME格式的用户修改通知邮件.
	 */
	public void sendMail(EmailBean emailBean) throws MessagingException {

		MimeMessage msg = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, true, DEFAULT_ENCODING);

		helper.setTo(emailBean.getToMail());
		helper.setFrom(PropertiesUtil.getProperty(Constant.EmailFrom));
		helper.setSubject(emailBean.getSubject());

		String content = generateContent(emailBean.getEmailType(), emailBean.getContentMap());
		helper.setText(content, true);

		mailSender.send(msg);
	}

	/**
	 * 使用Freemarker生成html格式内容.
	 */
	private String generateContent(int emailType, Map<String, Object> context) throws MessagingException {

		try {
			return FreeMarkerTemplateUtils.processTemplateIntoString(templateMap.get(emailType), context);
		} catch (IOException e) {
			logger.error("生成邮件内容失败, FreeMarker模板不存在", e);
			throw new MessagingException("FreeMarker模板不存在", e);
		} catch (TemplateException e) {
			logger.error("生成邮件内容失败, FreeMarker处理失败", e);
			throw new MessagingException("FreeMarker处理失败", e);
		}
	}

	/**
	 * Spring的MailSender.
	 */
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * 注入Freemarker引擎配置,构造Freemarker 邮件内容模板.
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
		// 根据freemarkerConfiguration的templateLoaderPath载入文件.
		Template templateScheduleConfirm = freemarkerConfiguration.getTemplate("mailTemplateScheduleConfirm.ftl",
				DEFAULT_ENCODING);
		templateMap.put(EmailBean.SCHEDULE_CONFIRM_EMAIL, templateScheduleConfirm);
	}
}
