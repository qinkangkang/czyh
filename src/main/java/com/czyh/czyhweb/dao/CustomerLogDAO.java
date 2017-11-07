package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerLog;

public interface CustomerLogDAO extends JpaRepository<TCustomerLog, Long>, JpaSpecificationExecutor<TCustomerLog> {

}