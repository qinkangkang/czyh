package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TRelation;

public interface RelationDAO extends JpaRepository<TRelation, String>, JpaSpecificationExecutor<TRelation> {

}