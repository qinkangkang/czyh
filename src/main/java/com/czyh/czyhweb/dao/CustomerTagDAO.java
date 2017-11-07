package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerTag;

public interface CustomerTagDAO extends JpaRepository<TCustomerTag, String>, JpaSpecificationExecutor<TCustomerTag> {

}