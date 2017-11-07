package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventBonus;

public interface EventBonusDAO extends JpaRepository<TEventBonus, String>, JpaSpecificationExecutor<TEventBonus> {
	
	@Modifying
	@Query("update TEventBonus t set t.fstatus = ?1 where t.id = ?2")
	void saveStatusBonus(Integer status, String id);
	
	@Modifying
	@Query("update TEventBonus t set t.fstock = t.fstock +?1 where t.id = ?2")
	void updatetStock(Integer fstock, String id);
}