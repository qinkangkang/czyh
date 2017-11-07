package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TIssueBonus;

public interface IssusBonusDAO extends JpaRepository<TIssueBonus, String>, JpaSpecificationExecutor<TIssueBonus> {

}