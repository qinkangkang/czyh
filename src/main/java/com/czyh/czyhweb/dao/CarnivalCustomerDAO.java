package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCarnivalCustomer;

public interface CarnivalCustomerDAO
		extends JpaRepository<TCarnivalCustomer, String>, JpaSpecificationExecutor<TCarnivalCustomer> {

	@Query("select t from TCarnivalCustomer t where t.fcarnivalId = ?1 and t.fcustomerId = ?2")
	TCarnivalCustomer getByFcarnivalIdAndFcustomerId(String fcarnivalId, String fcustomerId);
}