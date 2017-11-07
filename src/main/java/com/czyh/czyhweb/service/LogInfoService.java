package com.czyh.czyhweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.czyh.czyhweb.dao.LogInfoDAO;
import com.czyh.czyhweb.entity.TLogInfo;

@Service
@Transactional
public class LogInfoService {

	@Autowired
	private LogInfoDAO logInfoDAO;

	public void save(TLogInfo logInfo) {
		logInfoDAO.save(logInfo);
	}

}
