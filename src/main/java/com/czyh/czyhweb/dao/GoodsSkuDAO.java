package com.czyh.czyhweb.dao;


import java.util.Map;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TGoodsSku;

public interface GoodsSkuDAO extends JpaRepository<TGoodsSku, String>, JpaSpecificationExecutor<TGoodsSku> {


	void save(Map<String, Object> map);

	
	@Query("select t from TGoodsSku t where t.fgoodsId = ?1 ")
	List<TGoodsSku> findGoodsListSku(String fgoodsId);
	
	
	@Query("update TGoodsSku t set t.fimage=?2 where t.id=?1")
	void updateImage(String id, String string, String ids);

	@Query("select count(a.id) from TGoodsSku a where a.fgoodsNO = ?1 and a.fgoodsId = ?2")
	Long countByGoodsNOAndClassId(String fgoodsNO, String fgoodsId);

	@Query("select count(a.id) from TGoodsSku a where a.fgoodsNO = ?1 and a.fgoodsId = ?2 and a.id != ?3")
	Long countByGoodsNOAndClassIdNotId(String fgoodsNO, String fgoodsId, String id);


	
	

}