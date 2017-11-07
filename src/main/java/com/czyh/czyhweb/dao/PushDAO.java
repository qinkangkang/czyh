package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TPush;

public interface PushDAO extends JpaRepository<TPush, String>, JpaSpecificationExecutor<TPush> {

	
	@Query("select p from TPush p where 1=1")
	List<TPush> findAllPushList();
		
	@Modifying
	@Query("update TPush t set t.fstatus = ?1 where t.id = ?2")
	void saveUpdatedel(Integer status, String id);
	
	@Modifying
	@Query("update TPush t set t.fauditStatus = ?1, t.fstatus = ?2,t.foperator=?4 ,t.fauditTime=now() where t.id = ?3")
	void saveUpdatePush(Integer fauditStatus,Integer status, String id,Long foperator);
	
	@Modifying
	@Query("update TPush t set t.fauditMessage = ?2 where t.id = ?1")
	void updateAuditMessage(String id, String auditMessage);
	
}