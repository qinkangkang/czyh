package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerWithdrawal;

public interface CustomerWithdrawalDAO
		extends JpaRepository<TCustomerWithdrawal, String>, JpaSpecificationExecutor<TCustomerWithdrawal> {

}