package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerSign;

public interface CustomerSignDAO extends JpaRepository<TCustomerSign, String>, JpaSpecificationExecutor<TCustomerSign> {

}