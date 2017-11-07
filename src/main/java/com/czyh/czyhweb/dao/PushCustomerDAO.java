package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TPushCustomer;

public interface PushCustomerDAO extends JpaRepository<TPushCustomer, String>, JpaSpecificationExecutor<TPushCustomer> {

}