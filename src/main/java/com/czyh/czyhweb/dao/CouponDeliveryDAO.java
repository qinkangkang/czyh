package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import

org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCouponDelivery;

public interface

CouponDeliveryDAO extends JpaRepository<TCouponDelivery, String>, JpaSpecificationExecutor<TCouponDelivery> {


	@Query("from TCouponDelivery t where (t.TCustomer.id = ?1 or t.TCustomer.id is null ) and t.TCouponInformation.id = ?2")
	TCouponDelivery getCouponbyCustomer(String customerId, String couponId);

	@Modifying
	@Query("delete from TCouponDelivery t where t.TDelivery.id = ?1")
	void deleteByDeliveryId(String deliveryId);

}