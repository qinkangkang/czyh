package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSponsorWithdraw;

public interface SponsorWithdrawDAO
		extends JpaRepository<TSponsorWithdraw, String>, JpaSpecificationExecutor<TSponsorWithdraw> {

}