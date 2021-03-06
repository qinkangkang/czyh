package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSponsorBalance;

public interface SponsorBalanceDAO
		extends JpaRepository<TSponsorBalance, String>, JpaSpecificationExecutor<TSponsorBalance> {

}