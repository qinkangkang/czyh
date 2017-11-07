package com.czyh.czyhweb.util.email;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.Exceptions;

public class EmailSenderManager extends Thread {

	private static Logger logger = LoggerFactory.getLogger(EmailSenderManager.class);

	private static ArrayBlockingQueue<EmailBean> abq = new ArrayBlockingQueue<EmailBean>(1000, true);

	private static ThreadPoolExecutor producerPool = null;

	private MimeMailService mimeMailService;

	public static void put(EmailBean emailBean) {
		try {
			abq.put(emailBean);
		} catch (InterruptedException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	public EmailSenderManager(MimeMailService mimeMailService) {
		if (producerPool == null) {
			producerPool = new ThreadPoolExecutor(5, 20, 3600, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());
		}
		this.mimeMailService = mimeMailService;
	}

	@Override
	public void run() {
		EmailBean emailBean = null;
		try {
			while (true) {
				if (abq.isEmpty()) {
					Thread.sleep(10000);
				} else {
					emailBean = abq.poll();
					if (emailBean != null) {
						producerPool.submit(new EmailSenderExecutor(mimeMailService, emailBean));
						logger.info("start send one email to " + emailBean.getToMail());
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		} finally {
			interrupt();
		}
	}

	@Override
	public void interrupt() {
		producerPool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!producerPool.awaitTermination(10, TimeUnit.SECONDS)) {
				producerPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!producerPool.awaitTermination(10, TimeUnit.SECONDS)) {
					logger.error("Pool did not terminated");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			producerPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}