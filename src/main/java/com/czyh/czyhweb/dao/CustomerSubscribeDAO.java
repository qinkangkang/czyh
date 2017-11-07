package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerSubscribe;

public interface CustomerSubscribeDAO
		extends JpaRepository<TCustomerSubscribe, String>, JpaSpecificationExecutor<TCustomerSubscribe> {

}