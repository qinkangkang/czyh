package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TGoodsTypeClassCategory;

public interface GoodsTypeClassCategoryDAO
		extends JpaRepository<TGoodsTypeClassCategory, Long>, JpaSpecificationExecutor<TGoodsTypeClassCategory> {

	@Query("select t from TGoodsTypeClassCategory t where t.fcategoryId=?1 order by id asc")
	List<TGoodsTypeClassCategory> getTypeCategory(long categoryId);
	
	@Modifying
	@Query("delete  from TGoodsTypeClassCategory t where t.fcategoryId=?1")
	void deleteByCategoryId(Long categoryId);
	
	@Query("delete  from TGoodsTypeClassCategory t where t.fcategoryId=?1 and t.ftypeId=?2")
	void deleteByCategoryIdAndTypeId(String id, Long ftypeId);
	
	

}