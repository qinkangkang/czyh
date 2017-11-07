package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TModule;

public interface ModuleDAO extends JpaRepository<TModule, Long>, JpaSpecificationExecutor<TModule> {

	@Query("select distinct(m) from TModule m inner join m.roles r inner join r.users u where u.id =?1 and m.parent.id != null order by m.priority asc")
	List<TModule> getModuleByUserId(Long userId);

	@Query("select distinct(m) from TModule m inner join m.roles r inner join r.users u where u.id =?1 and m.parent.id = 10 order by m.priority asc")
	List<TModule> findLevelB(Long userId);

	@Query("select distinct(m.sn) from TModule m inner join m.roles r inner join r.users u where u.id =?1")
	List<String> findByUserId(Long userId);

}