package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TOrderExtInfo;

public interface OrderExtInfoDAO extends JpaRepository<TOrderExtInfo, String>, JpaSpecificationExecutor<TOrderExtInfo> {

}