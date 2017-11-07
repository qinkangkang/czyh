package com.czyh.czyhweb.util.html;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.Exceptions;

public class StaticHtmlManager extends Thread {

	private static Logger logger = LoggerFactory.getLogger(StaticHtmlManager.class);

	private static ArrayBlockingQueue<StaticHtmlBean> abq = new ArrayBlockingQueue<StaticHtmlBean>(1000, true);

	private static ThreadPoolExecutor producerPool = null;

	public static void put(StaticHtmlBean staticHtmlBean) {
		try {
			abq.put(staticHtmlBean);
		} catch (InterruptedException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
	}

	public StaticHtmlManager() {
		if (producerPool == null) {
			producerPool = new ThreadPoolExecutor(5, 20, 3600, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1000),
					new ThreadPoolExecutor.DiscardOldestPolicy());
		}
	}

	@Override
	public void run() {
		StaticHtmlBean staticHtmlBean = null;
		try {
			while (true) {
				if (abq.isEmpty()) {
					Thread.sleep(10000);
				} else {
					staticHtmlBean = abq.poll();
					if (staticHtmlBean != null) {
						producerPool.submit(new StaticHtmlExecutor(staticHtmlBean));
						logger.info("开始执行生成静态化页面！TemplateName：" + staticHtmlBean.getTemplateName() + "；ID："
								+ staticHtmlBean.getObjectId());
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