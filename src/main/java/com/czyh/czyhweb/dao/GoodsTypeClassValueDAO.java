package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TGoodsTypeClassValue;

public interface GoodsTypeClassValueDAO
		extends JpaRepository<TGoodsTypeClassValue, Long>, JpaSpecificationExecutor<TGoodsTypeClassValue> {
	@Query("select count(a.id) from TGoodsTypeClassValue a where a.fvalue = ?1 and a.fextendClassId = ?2")
	Long countByNameAndClassId(String name, Long fextendClassId);//新增时判断属性值是否已存在

	
	@Query("select count(a.id) from TGoodsTypeClassValue a where a.fvalue = ?1 and a.fextendClassId = ?2 and a.id != ?3")
	Long countByNameAndClassIdNotId(String name, Long fextendClassId, Long id);

	@Query("select count(a.id) from TGoodsTypeClassValue a where a.fsort = ?1 and a.fextendClassId = ?2 and a.id != ?3")
	Long countBySortAndClassIdNotId(Integer sort, Long fextendClassId, Long id);

	@Query("select count(a.id) from TGoodsTypeClassValue a where a.fsort = ?1 and a.fextendClassId = ?2")
	Long countBySortAndClassId(Integer sort, Long fextendClassId);

	@Query("select t from TGoodsTypeClassValue t where t.fextendClassId=?1")
	List<TGoodsTypeClassValue> getTypeValueListByTypeId(Long typeClassId);

}