package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TAppChannelSlider;
import com.czyh.czyhweb.entity.TAppIndexItem;

public interface AppIndexItemDAO extends JpaRepository<TAppIndexItem, String>, JpaSpecificationExecutor<TAppIndexItem> {

	@Query("select t from TAppIndexItem t where t.flagType = 1 ")
	List<TAppIndexItem> findBySliderList();
}