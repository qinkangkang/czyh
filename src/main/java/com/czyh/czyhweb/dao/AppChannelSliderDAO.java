package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TAppChannelSlider;

public interface AppChannelSliderDAO
		extends JpaRepository<TAppChannelSlider, String>, JpaSpecificationExecutor<TAppChannelSlider> {

	@Modifying
	@Query("delete from TAppChannelSlider t where t.TAppChannelSetting.id = ?1")
	void deleteByChannelId(String channelId);
}