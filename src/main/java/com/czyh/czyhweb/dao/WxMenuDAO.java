package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TWxMenu;

public interface WxMenuDAO extends JpaRepository<TWxMenu, Long>, JpaSpecificationExecutor<TWxMenu> {

	@Query("select t from TWxMenu t where 1=1 order by t.menuOrder desc")
	List<TWxMenu> getMenuList();

	@Modifying
	@Query("update TWxMenu t set t.menuName = ?2 ,t.url = ?3 where t.id=?1")
	void updateMenu(Long id, String menuName, String url);

}