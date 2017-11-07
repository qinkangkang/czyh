package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TOrderGoods;

public interface OrderGoodsDAO extends JpaRepository<TOrderGoods, Long>, JpaSpecificationExecutor<TOrderGoods> {

	@Query("select t from TOrderGoods t where t.forderId = ?1")
	List<TOrderGoods> findByOrderId(String orderId);
}	