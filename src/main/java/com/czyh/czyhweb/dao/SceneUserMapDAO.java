package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TSceneUserMap;

public interface SceneUserMapDAO extends JpaRepository<TSceneUserMap, String>, JpaSpecificationExecutor<TSceneUserMap> {

	@Query("select t from TSceneUserMap t where t.fsceneCode = ?1 ")
	List<TSceneUserMap> findBySceneCode(String sceneCode);
}