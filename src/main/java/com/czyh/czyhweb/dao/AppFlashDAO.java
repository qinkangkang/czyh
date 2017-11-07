package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TAppFlash;

public interface AppFlashDAO extends JpaRepository<TAppFlash, String>, JpaSpecificationExecutor<TAppFlash> {
	
	TAppFlash findByFcity(Integer cityId);

}