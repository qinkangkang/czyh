package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TConfiguration;

public interface ConfigurationDAO
		extends JpaRepository<TConfiguration, String>, JpaSpecificationExecutor<TConfiguration> {

	@Modifying
	@Query("update TCoupon t set t.fstatus = ?1, t.fupdateTime = now() where t.id = ?2")
	void saveStatus(Integer status, String id);

}