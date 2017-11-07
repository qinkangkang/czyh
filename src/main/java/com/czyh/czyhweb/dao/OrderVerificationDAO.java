package com.czyh.czyhweb.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TOrderVerification;

public interface OrderVerificationDAO
		extends JpaRepository<TOrderVerification, String>, JpaSpecificationExecutor<TOrderVerification> {

	@Query("select t from TOrderVerification t where t.TSponsor.id = ?1 and t.fstatus = 10 and (t.fcreateTime between ?2 and ?3)")
	List<TOrderVerification> findByUnSettlement(String merchantId, Date startDate, Date endDate);

	@Modifying
	@Query("update TOrderVerification t set t.fstatus = ?2 where t.id = ?1")
	void updateStatus(String id, Integer status);
	
	@Query("select t from TOrderVerification t where t.fstatus = 10 and (t.fcreateTime between ?1 and ?2)")
	List<TOrderVerification> findByTime(Date startDate, Date endDate);
	
	@Modifying
	@Query("update TOrderVerification t set t.fstatus = ?3 where (t.fcreateTime between ?1 and ?2)")
	void updateStatusByTime(Date startDate, Date endDate, Integer status);

}