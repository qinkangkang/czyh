package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventSession;

public interface EventSessionDAO extends JpaRepository<TEventSession, String>, JpaSpecificationExecutor<TEventSession> {

	@Query("from TEventSession t where t.TEvent.id = ?1 and t.fstatus < 999")
	List<TEventSession> findByEventId(String eventId);

	@Modifying
	@Query("update TEventSession t set t.fstatus = ?1, t.fupdateTime = now() where t.id = ?2")
	void saveStatus(Integer status, String id);

//	@Modifying
//	@Query("update TEventSession t set t.flimitation = t.flimitation + ?1 where t.id = ?2")
//	void addLimitation(Integer count, String id);

//	@Modifying
//	@Query("update TEventSession t set t.flimitation = t.flimitation - ?1 where t.id = ?2")
//	void subtractLimitation(Integer count, String id);

//	@Query("select sum(s.flimitation) from TEventSession s where s.TEvent.id = ?1 and s.fstatus < 999")
//	Integer getSumStock(String eventId);

	@Query("select max(s.fdeadline) from TEventSession s where s.TEvent.id = ?1 and s.fstatus < 999")
	String getFdeadline(String eventId);

//	@Query("select coalesce(sum(s.flimitation),0) from TEventSession s where s.TEvent.id = ?1 and s.fstatus < 999")
//	Integer getSumLimitation(String eventId);
	
	@Query("from TEventSession t where t.TEvent.id =?1 and t.fsalesFlag=?2")
	TEventSession findSaleFlag(String eventId,Integer fsalesFlag);
}