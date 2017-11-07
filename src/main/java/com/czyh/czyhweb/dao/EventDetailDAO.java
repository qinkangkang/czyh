package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventDetail;

public interface EventDetailDAO extends JpaRepository<TEventDetail, String>, JpaSpecificationExecutor<TEventDetail> {

	
	@Modifying
	@Query("update TEventDetail t set t.ftype = ?2,t.fcontent =?3 where t.id = ?1")
	void updateDetail(String id, Integer ftype,String fcontent);
	
	@Query("select t from TEventDetail t where t.TEvent.id = ?1")
	List<TEventDetail> findTEventDetailList(String eventId);
	
	@Modifying
	@Query("delete from TEventDetail t where t.TEvent.id = ?1")
	void deleteByEventId(String eventId);
	
}