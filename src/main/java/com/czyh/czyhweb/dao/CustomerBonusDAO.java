package com.czyh.czyhweb.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCustomerBonus;

public interface CustomerBonusDAO
		extends JpaRepository<TCustomerBonus, String>, JpaSpecificationExecutor<TCustomerBonus> {

	@Query("select sum(t.fbonus) from TCustomerBonus t where t.fcustermerId = ?1 and t.fcreateTime >= date and (t.fbonus > 0 or t.ftype = 666) ")
	String findUsable(String customerId, Date date);

	@Query("select sum(t.fbonus) from TCustomerBonus t where t.fcustermerId = ?1 and t.fcreateTime <= date and t.fbonus < 0 and t.ftype != 666")
	String findUsedBonus(String customerId, Date date);

}