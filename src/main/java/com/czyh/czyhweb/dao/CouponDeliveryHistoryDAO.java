package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCouponDeliveryHistory;

public interface CouponDeliveryHistoryDAO
		extends JpaRepository<TCouponDeliveryHistory, String>, JpaSpecificationExecutor<TCouponDeliveryHistory> {

//	@Modifying
//	@Query("update TCouponDelivery t set t.TOrder.id = null, t.fuseTime = null where t.TOrder.id = ?1 and t.fstatus = ?2")
//	void clearOrderIdAndUseTime(String orderId, Integer status);
	
	@Query("from TCouponDeliveryHistory t where t.TOrder.id = ?1 and t.fstatus = 20")
	TCouponDeliveryHistory getCouponbyOrder(String orderId);
}