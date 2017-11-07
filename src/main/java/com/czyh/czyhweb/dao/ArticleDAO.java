package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TArticle;

public interface ArticleDAO extends JpaRepository<TArticle, String>, JpaSpecificationExecutor<TArticle> {

	@Modifying
	@Query("update TArticle t set t.fupdateTime = now(), t.fstatus = ?1 where t.id = ?2")
	void saveUpdateTimeAndStatus(Integer status, String id);

}