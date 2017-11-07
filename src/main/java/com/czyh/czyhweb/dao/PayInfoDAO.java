package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TPayInfo;
import com.czyh.czyhweb.entity.TUser;
import com.czyh.czyhweb.entity.TWxPay;

public interface PayInfoDAO extends JpaRepository<TPayInfo, String>, JpaSpecificationExecutor<TPayInfo> {
	
	@Query("from TPayInfo t where t.forderId = ?1 and t.finOut = ?2")
	TPayInfo getByOrderId(String orderId, Integer inOut);
}