package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TAppChannelEvent;

public interface AppChannelEventDAO
		extends JpaRepository<TAppChannelEvent, String>, JpaSpecificationExecutor<TAppChannelEvent> {

	@Modifying
	@Query("update TAppChannelEvent t set t.forder = ?2 where t.id = ?1")
	void updateForder(String id, Integer forder);

	@Modifying
	@Query("delete from TAppChannelEvent t where t.TEvent.id = ?1")
	void deleteByEventId(String eventId);
	
	@Modifying
	@Query("delete from TAppChannelEvent t where t.TAppChannelSetting.id = ?1")
	void deleteByChannelId(String channelId);

}