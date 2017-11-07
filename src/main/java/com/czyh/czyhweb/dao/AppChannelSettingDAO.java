package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TAppChannelSetting;
import com.czyh.czyhweb.entity.TOrder;

public interface AppChannelSettingDAO
		extends JpaRepository<TAppChannelSetting, String>, JpaSpecificationExecutor<TAppChannelSetting> {

	@Modifying
	@Query("delete from TAppChannelSetting t where t.id = ?1")
	void deleteByChannelId(String channelId);
	
	@Query("select t from TAppChannelSetting t where t.fisVisible = 1 and t.fcode = 'index_activity_1' ")
	List<TAppChannelSetting> findByCode();
	
}