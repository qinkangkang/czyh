package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TPoster;

public interface PosterDAO extends JpaRepository<TPoster, String>, JpaSpecificationExecutor<TPoster> {
	
	@Query("select t from TPoster t where t.fstatus = 20")
	TPoster findPosterStatus();
	
	@Modifying
	@Query("update TPoster t set t.fstatus = ?1 where t.id = ?2")
	void saveStatusPoster(Integer status, String id);
}