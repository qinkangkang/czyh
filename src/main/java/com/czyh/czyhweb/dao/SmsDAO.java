package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSms;

public interface SmsDAO extends JpaRepository<TSms, Long>, JpaSpecificationExecutor<TSms> {

}