package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCouponActivitycoupons;

public interface CouponActivitycouponsDAO
		extends JpaRepository<TCouponActivitycoupons, String>, JpaSpecificationExecutor<TCouponActivitycoupons> {
	
	@Query("from TCouponActivitycoupons t where t.fcouponId = ?1")
	TCouponActivitycoupons getTActivitycoupons(String couponId);

}