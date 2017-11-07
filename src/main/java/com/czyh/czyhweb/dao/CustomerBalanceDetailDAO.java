package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerBalanceDetail;

public interface CustomerBalanceDetailDAO
		extends JpaRepository<TCustomerBalanceDetail, String>, JpaSpecificationExecutor<TCustomerBalanceDetail> {

}