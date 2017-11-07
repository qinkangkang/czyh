package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventSpec;

public interface EventSpecDAO extends JpaRepository<TEventSpec, String>, JpaSpecificationExecutor<TEventSpec> {

	@Modifying
	@Query("update TEventSpec t set t.fstatus = ?1, t.fupdateTime = now() where t.id = ?2")
	void saveStatus(Integer status, String id);

	@Modifying
	@Query("update TEventSpec t set t.fstock = t.fstock + ?1 where t.id = ?2")
	void addStock(Integer count, String id);

	@Modifying
	@Query("update TEventSpec t set t.fstock = t.fstock - ?1 where t.id = ?2")
	void subtractStock(Integer count, String id);

	@Query("select coalesce(sum(s.fstock),0) from TEventSpec s where s.TEvent.id = ?1 and s.fstatus < 999")
	Integer getSumStock(String eventId);

	@Query("from TEventSpec t where t.TEventSession.id = ?1 and t.fstatus < 999")
	List<TEventSpec> getTEventSpec(String SessionId);

}