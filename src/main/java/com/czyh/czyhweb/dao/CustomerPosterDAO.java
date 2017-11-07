package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerPoster;


public interface CustomerPosterDAO extends JpaRepository<TCustomerPoster, String>, JpaSpecificationExecutor<TCustomerPoster> {
	
}