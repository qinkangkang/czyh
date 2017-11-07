package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventCategory;

public interface EventCategoryDAO
		extends JpaRepository<TEventCategory, Long>, JpaSpecificationExecutor<TEventCategory> {

	TEventCategory getByLevelAndValue(int level, int value);

	List<TEventCategory> findByLevel(int level);

	List<TEventCategory> findByLevelAndParentId(int level, long parentId);

	@Query("select t from TEventCategory  t where t.value=?1 ")
	TEventCategory getCategoryByValue(Integer value);

	@Query("select t.name from TEventCategory t where t.value=?1 ")
	String getCategoryAName(Integer value);

	@Query("select max(value) from TEventCategory t ")
	int findMaxValue();
	
	@Query("select count(t.id) from TEventCategory t where t.name=?1")
	Long countByCategoryName(String categoryNameValue);

	@Query("select count(t.id) from TEventCategory t where t.name=?1 and id!=?2")
	Long countByCategoryNameAndNotId(String categoryNameValue, Long id);

}