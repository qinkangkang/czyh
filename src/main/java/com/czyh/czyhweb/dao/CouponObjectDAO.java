package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCouponObject;

public interface CouponObjectDAO extends JpaRepository<TCouponObject, String>, JpaSpecificationExecutor<TCouponObject> {

}