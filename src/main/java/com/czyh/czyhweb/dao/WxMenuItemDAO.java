package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TWxMenuItem;

public interface WxMenuItemDAO extends JpaRepository<TWxMenuItem, Long>, JpaSpecificationExecutor<TWxMenuItem> {

	@Query("select t from TWxMenuItem t where t.TWxMenu.id=?1")
	List<TWxMenuItem> getMenuList(Long menuId);

	@Modifying
	@Query("update TWxMenuItem t set t.itemName = ?2 ,t.content = ?3,t.text = ?4,t.menuOrder =?5 where t.id=?1")
	void updateMenuItem(Long id, String itemName, String content, String text, int orderm);

}