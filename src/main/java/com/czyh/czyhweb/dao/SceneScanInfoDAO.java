package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSceneScanInfo;

public interface SceneScanInfoDAO
		extends JpaRepository<TSceneScanInfo, String>, JpaSpecificationExecutor<TSceneScanInfo> {

}