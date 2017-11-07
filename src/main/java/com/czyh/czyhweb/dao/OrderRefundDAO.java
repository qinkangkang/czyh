package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TOrderRefund;

public interface OrderRefundDAO extends JpaRepository<TOrderRefund, String>, JpaSpecificationExecutor<TOrderRefund> {

	@Query("select t from TOrderRefund t where t.forderId = ?1 and t.frefundStatus <= 30 ")
	TOrderRefund findByOrder(String orderId);
	
	@Modifying
	@Query("update TOrderRefund t set t.frefundStatus = ?1 where t.id = ?2 and t.frefundStatus < 100")
	void updateOrderStatus(Integer status, String id);
}