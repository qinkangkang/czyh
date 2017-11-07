package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TColumnBanner;

public interface ColumnBannerDAO extends JpaRepository<TColumnBanner, String>, JpaSpecificationExecutor<TColumnBanner> {

	@Query("select t from TColumnBanner t where t.ftag = ?1 and t.ftype = ?2 and t.fstatus < 999")
	TColumnBanner findColumnBanner(Integer tag,Integer type);

	@Query("select t from TColumnBanner t where t.fchannelId = ?1 and t.fstatus < 999")
	TColumnBanner findChannelId(String channelId);

	@Query("select t from TColumnBanner t where t.ftag = ?1 and t.ftype = ?2 and t.fstatus < 999")
	TColumnBanner findSeckillTime(Integer tag,Integer ftype);
	
	@Query("select t from TColumnBanner t where t.ftag = ?1 and t.ftype = ?2 and t.fstatus < 999")
	TColumnBanner findByType(Integer tag,Integer ftype);
	
	@Modifying
	@Query("delete from TColumnBanner t where t.id = ?1")
	void deleteById(String id);
}