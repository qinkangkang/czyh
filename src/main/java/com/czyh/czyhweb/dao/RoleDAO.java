package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TRole;

public interface RoleDAO extends JpaRepository<TRole, Long>, JpaSpecificationExecutor<TRole> {

	TRole findByName(String name);

	@Query("select count(t.id) from TRole t where t.code = ?1")
	Long checkRolecode(String code);

	@Query("select count(t.id) from TRole t where t.code = ?1 and t.id != ?2")
	Long checkEditRolecode(String code, Long id);

}