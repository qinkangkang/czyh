package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TConsult;

public interface ConsultDAO extends JpaRepository<TConsult, String>, JpaSpecificationExecutor<TConsult> {

	@Query("select COUNT(t.id) from TConsult t where t.fstatus = ?1 ")
	Long getCountByStatus(Integer integer);
	
	@Modifying
	@Query("update TConsult t set t.fstatus = ?1 where t.id = ?2")
	void updateStatusConsult(Integer status, String id);
}