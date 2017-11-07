package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TAppVersion;

public interface AppVersionDAO extends JpaRepository<TAppVersion, String>, JpaSpecificationExecutor<TAppVersion> {

}