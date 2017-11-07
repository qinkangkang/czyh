package com.czyh.czyhweb.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCustomerInfo;

public interface CustomerInfoDAO extends JpaRepository<TCustomerInfo, String>, JpaSpecificationExecutor<TCustomerInfo> {

	@Query("select t from TCustomerInfo t where t.fcustomerId = ?1")
	TCustomerInfo getByCustomerId(String customerId);
	
	@Modifying
	@Query("update TCustomerInfo t set t.forderTotal = ?2 where t.fcustomerId = ?1")
	void updateOrderTotal(String customerId, BigDecimal orderTotal);
	
	@Modifying
	@Query("update TCustomerInfo t set t.fpoint = (t.fpoint + ?2) where t.fcustomerId = ?1")
	void updatePoint(String customerId, int point);
	
	@Modifying
	@Query("update TCustomerInfo t set t.fpoint = (t.fpoint + ?2),t.fusedPoint = (t.fusedPoint + ?3) where t.fcustomerId = ?1")
	void updatePointAndUsePoint(String customerId, int point,int usePoing);
	
	/*@Modifying
	@Query("update TCustomerInfo t set t.fbalance = (t.fbalance + ?2) , t.ftotalBalance = (t.ftotalBalance + ?2) where t.fcustomerId = ?1")
	void updateBalance(String customerId, BigDecimal fbalance);
	
	@Modifying
	@Query("update TCustomerInfo t set t.ffreezeBalance = (t.ffreezeBalance - ?2) , t.fbalance = (t.fbalance + ?2) where t.fcustomerId = ?1")
	void updateFreezeBalanceAndBalance(String customerId, BigDecimal balance);
	
	@Modifying
	@Query("update TCustomerInfo t set t.ffreezeBalance = (t.ffreezeBalance - ?2) , t.ftotalBalance = (t.ftotalBalance - ?2) where t.fcustomerId = ?1")
	void updateFreezeBalanceAndTotalBalance(String customerId, BigDecimal balance);*/
}