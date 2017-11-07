package com.czyh.czyhweb.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TGoodsSpaceValue;

public interface GoodsSpaceValueDAO
		extends JpaRepository<TGoodsSpaceValue, String>, JpaSpecificationExecutor<TGoodsSpaceValue> {

	@Query("select t from TGoodsSpaceValue t where t.fgoodsId = ?1 ")
	List<TGoodsSpaceValue> findGoodsList(String goodsId);
	@Modifying
	@Query("delete from  TGoodsSpaceValue t where t.fgoodsId=?1 ")
	void deleteByEventId(String eventId);
	
	@Query("select t from TGoodsSpaceValue t where t.fgoodsId=?1 ")
	List<TGoodsSpaceValue> getSpecList(String eventId);
	
	@Query("select count(t.id) from TGoodsSpaceValue t where t.fspaceName=?1 and t.fgoodsId=?2")
	Long countBySpaceName(String fspaceName, String fgoodsId);
	
	@Query("select count(t.id) from TGoodsSpaceValue t where t.fspaceName=?1 and t.fgoodsId=?3 and id!=?2")
	Long countByGoodsIdAndValueName(String value, Long id, String fgoodsId);
	
	

}