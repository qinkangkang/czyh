package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCustomerBargaining;

public interface CustomerBargainingDAO
		extends JpaRepository<TCustomerBargaining, String>, JpaSpecificationExecutor<TCustomerBargaining> {

	@Query("select t from TCustomerBargaining t where t.forderId = ?1")
	TCustomerBargaining getByOrderId(String orderId);

	@Modifying
	@Query("update TCustomerBargaining t set t.fstatus = ?2 where t.forderId = ?1")
	void updateStatus(String orderId, Integer status);

}