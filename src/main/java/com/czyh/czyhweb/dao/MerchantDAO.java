package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TMerchant;

public interface MerchantDAO extends JpaRepository<TMerchant, String>, JpaSpecificationExecutor<TMerchant> {

}