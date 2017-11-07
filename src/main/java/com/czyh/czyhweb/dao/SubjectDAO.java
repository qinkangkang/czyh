package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TSubject;

public interface SubjectDAO extends JpaRepository<TSubject, String>, JpaSpecificationExecutor<TSubject> {

}