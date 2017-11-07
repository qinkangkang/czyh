package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TAppStartup;

public interface AppStartupDAO extends JpaRepository<TAppStartup, String>, JpaSpecificationExecutor<TAppStartup> {

}