package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TSystemNotice;

public interface TSystemNoticeDAO extends JpaRepository<TSystemNotice, String>, JpaSpecificationExecutor<TSystemNotice> {

	@Modifying
	@Query("update TSystemNotice t set t.fstatus = ?1 where t.id = ?2")
	void del(Integer status, String id);
	
}