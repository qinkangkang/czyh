package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSponsorStatement;

public interface SponsorStatementDAO
		extends JpaRepository<TSponsorStatement, String>, JpaSpecificationExecutor<TSponsorStatement> {

}