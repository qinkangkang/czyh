package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TGoodsTypeClass;

public interface GoodsTypeClassDAO
		extends JpaRepository<TGoodsTypeClass, Long>, JpaSpecificationExecutor<TGoodsTypeClass> {

	@Query("select count(t.id) from TGoodsTypeClass t where t.fclassName=?1")
	Long countByTypeClassName(String typeClassName);

	@Query("select count(t.id) from TGoodsTypeClass t where t.fclassName=?1 and id!=?2")
	Long countByTypeClassNameAndNotId(String typeClassName, Long id) ;

	@Query("select  count(t.id) from TGoodsTypeClass t where t.fsort=?1 ")
	Long countByTypeClassSort(Integer sort);

	@Query("select count(t.id) from TGoodsTypeClass t where t.fsort=?1 and id!=?2")
	Long countByTypeClassSorAndNotId(Integer sort, Long id);
	
	

	

}