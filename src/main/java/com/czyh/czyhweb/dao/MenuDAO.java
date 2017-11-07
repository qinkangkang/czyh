package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TModule;

public interface MenuDAO
		extends JpaRepository<TModule, String>, JpaSpecificationExecutor<TModule> {

	@Query("select t from TModule t where t.id=?1")
	TModule getOne(Long specId);

	@Modifying
	@Query("delete from TModule t where t.id=?1")
	void delete(Long id);

	@Query("select count(t.id) from TModule t where t.name=?1")
	Long countByMenuName(String name);

	@Query("select count(t.id) from TModule t where t.name=?1 and id!=?2")
	Long countByMenuNameAndNotId(String name, Long id);

	

}