package com.czyh.czyhweb.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TSeckillModule;

public interface SeckillModuleDAO
		extends JpaRepository<TSeckillModule, String>, JpaSpecificationExecutor<TSeckillModule> {

	@Modifying
	@Query("update TSeckillModule t set t.fgoodstatus = ?1 ,t.fgoodsUpdateTime = ?3 where t.id = ?2")
	void saveSaleSeckillModule(Integer status, String id, Date updateTime);

	@Query("from TSeckillModule t where t.fgoodsId = ?1 and t.fgoodstatus<999")
	TSeckillModule getGoodsIdDetail(String goodsId);
}