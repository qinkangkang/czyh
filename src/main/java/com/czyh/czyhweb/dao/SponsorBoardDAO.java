package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSponsorBoard;

public interface SponsorBoardDAO extends JpaRepository<TSponsorBoard, String>, JpaSpecificationExecutor<TSponsorBoard> {

}