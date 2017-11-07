package com.czyh.czyhweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.service.system.AppService;
import com.czyh.czyhweb.util.SpringContextHolder;
import com.czyh.czyhweb.util.asynchronoustasks.ITaskBean;

/**
 * 
 * @author zgzhou
 */
@Component
@Transactional
public class AsynchronousTasksService {

	private static Logger logger = LoggerFactory.getLogger(AsynchronousTasksService.class);

	/**
	 * 执行异步任务
	 */
	public void performTasks(ITaskBean taskBean) throws ServiceException {
		int taskType = taskBean.getTaskType();
		switch (taskType) {
		case 1: {
			// 异步写入用户日志
			try {
				AppService appService = SpringContextHolder.getBean(AppService.class);
				break;
			} catch (Exception e) {
				logger.error("异步写入用户日志出错", e);
				throw new ServiceException("异步写入用户日志出错", e);
			}

		}

		default: {
			break;
		}
		}
	}

}