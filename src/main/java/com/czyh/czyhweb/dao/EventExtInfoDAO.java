package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventExtInfo;

public interface EventExtInfoDAO extends JpaRepository<TEventExtInfo, String>, JpaSpecificationExecutor<TEventExtInfo> {

	@Query("from TEventExtInfo t where t.feventId = ?1")
	List<TEventExtInfo> getTEventExtInfoByEventId(String eventId);
	
//	@Modifying
//	@Query("update TEventExtInfo t set t.forder = ?2,t.fprompt = ?3,t.fname = ?4,t.fisRequired = ?5,t.fisEveryone = ?6 where t.id = ?1")
//	void updateForder(String id, Integer forder, String fprompt,String fname,Integer fisRequired,Integer fisEveryone);
	
	@Modifying
	@Query("update TEventExtInfo t set t.fstatus = ?1 where t.id = ?2")
	void deleteTEventExtInfo(Integer status, String id);
	
}