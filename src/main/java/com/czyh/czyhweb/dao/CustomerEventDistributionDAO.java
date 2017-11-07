package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerEventDistribution;

public interface CustomerEventDistributionDAO extends JpaRepository<TCustomerEventDistribution, String>,
		JpaSpecificationExecutor<TCustomerEventDistribution> {
	
	TCustomerEventDistribution getByFcustomerIdAndFeventId(String customerId, String eventId);
}