package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCarnival;

public interface CarnivalDAO extends JpaRepository<TCarnival, String>, JpaSpecificationExecutor<TCarnival> {

	@Modifying
	@Query("update TCarnival t set t.fstatus = ?1 where t.id = ?2")
	void delCarnival(Integer status, String id);

	@Modifying
	@Query("update TCarnival t set t.fstatus = ?1 where t.id = ?2")
	void saveStatusCarnival(Integer status, String id);

	@Query("select max(t.fsceneStr) from TCarnival t where 1=1")
	String getMaxPointCode();
	
}