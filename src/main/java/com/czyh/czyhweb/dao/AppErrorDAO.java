package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TAppError;

public interface AppErrorDAO extends JpaRepository<TAppError, String>, JpaSpecificationExecutor<TAppError> {

}