package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TAppNotice;

public interface AppNoticeDAO extends JpaRepository<TAppNotice, String>, JpaSpecificationExecutor<TAppNotice> {

	@Query("select t from TAppNotice t order by forder")
	List<TAppNotice> findOrderBy();
}