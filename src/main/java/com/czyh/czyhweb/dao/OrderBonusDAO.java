package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TOrderBonus;

public interface OrderBonusDAO
		extends JpaRepository<TOrderBonus, String>, JpaSpecificationExecutor<TOrderBonus> {

	@Modifying
	@Query("update TOrderBonus t set t.fstatus = ?1 where t.id = ?2")
	void updateOrderStatus(Integer status, String id);
	
	@Modifying
	@Query("update TOrderBonus t set t.fnote = ?2 where t.id = ?1")
	void updateCsRemark(String id, String fnote);
}