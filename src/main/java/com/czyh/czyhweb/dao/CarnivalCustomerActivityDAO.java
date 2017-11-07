package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCarnivalCustomerActivity;

public interface CarnivalCustomerActivityDAO
		extends JpaRepository<TCarnivalCustomerActivity, String>, JpaSpecificationExecutor<TCarnivalCustomerActivity> {

}