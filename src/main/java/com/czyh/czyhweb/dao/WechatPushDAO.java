package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TWechatPush;

public interface WechatPushDAO extends JpaRepository<TWechatPush, String>, JpaSpecificationExecutor<TWechatPush> {

	@Modifying
	@Query("update TWechatPush t set t.fstatus = ?1 where t.id = ?2")
	void updateStatus(Integer status, String id);
}