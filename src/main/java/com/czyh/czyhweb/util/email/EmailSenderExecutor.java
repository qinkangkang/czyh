package com.czyh.czyhweb.util.email;

import java.io.Serializable;
import java.util.concurrent.Callable;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.Exceptions;

public class EmailSenderExecutor implements Callable<String>, Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(EmailSenderExecutor.class);

	private MimeMailService mimeMailService;

	private EmailBean emailBean;

	public EmailSenderExecutor(MimeMailService mimeMailService, EmailBean emailBean) {
		this.emailBean = emailBean;
		this.mimeMailService = mimeMailService;
	}

	public String call() {
		if (emailBean.getCount() < 5) {
			try {
				mimeMailService.sendMail(emailBean);
				logger.info("send to \"" + emailBean.getToMail() + "\" email success !");
			} catch (MessagingException e) {
				logger.error("send to \"" + emailBean.getToMail() + "\" email error !", e);
				emailBean.addOne();
				EmailSenderManager.put(emailBean);
			} catch (Exception e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
		}
		return null;
	}
}