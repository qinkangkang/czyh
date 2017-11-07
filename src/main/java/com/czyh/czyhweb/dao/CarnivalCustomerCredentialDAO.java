package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCarnivalCustomerCredential;

public interface CarnivalCustomerCredentialDAO extends JpaRepository<TCarnivalCustomerCredential, String>,
		JpaSpecificationExecutor<TCarnivalCustomerCredential> {

}