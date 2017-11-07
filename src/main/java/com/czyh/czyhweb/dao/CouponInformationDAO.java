package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCouponInformation;

public interface CouponInformationDAO
		extends JpaRepository<TCouponInformation, String>, JpaSpecificationExecutor<TCouponInformation> {

	@Modifying
	@Query("update TCouponInformation t set t.fcouponStatus = ?1, t.fupdateTime = now() where t.id = ?2")
	void updateCouponInfoStatus(Integer status, String id);
}