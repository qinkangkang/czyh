package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCoupon;

public interface CouponDAO extends JpaRepository<TCoupon, String>, JpaSpecificationExecutor<TCoupon> {

	@Modifying
	@Query("update TCoupon t set t.fstatus = ?1, t.fupdateTime = now() where t.id = ?2")
	void saveStatus(Integer status, String id);

	@Modifying
	@Query("update TCoupon t set t.fsendCount = ?1 ,t.fstatus = ?2, t.fupdateTime = now() where t.id = ?3")
	void saveSendCountAndStatus(Integer sendCount, Integer status, String id);

}