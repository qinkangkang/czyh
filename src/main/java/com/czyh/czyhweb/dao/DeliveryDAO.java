package com.czyh.czyhweb.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TDelivery;

public interface DeliveryDAO extends JpaRepository<TDelivery, String>, JpaSpecificationExecutor<TDelivery> {

	@Modifying
	@Query("update TDelivery t set t.fstatus = ?1,t.fauditor = ?2,t.fupdateTime = ?3 where t.id = ?4")
	void updatedeliverystatus(Integer status,long fAuditor,Date updatetime, String id);
	
	@Modifying
	@Query("update TDelivery t set t.fstatus = ?1,t.fauditor = ?2,t.fdeliveryCreateTime = ?3 where t.id = ?4")
	void issuedeliverystatus(Integer status,long fAuditor,Date updatetime, String id);
}