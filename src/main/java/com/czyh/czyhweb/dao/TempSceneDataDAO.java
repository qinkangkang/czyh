package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TempSceneData;

public interface TempSceneDataDAO
		extends JpaRepository<TempSceneData, String>, JpaSpecificationExecutor<TempSceneData> {

	@Query("select t from TempSceneData t where t.fhsid = ?1")
	List<TempSceneData> findDataBySessionId(String sessionId);
	
	@Modifying
	@Query("delete from TempSceneData t where t.fhsid = ?1")
	void deleteBySessionId(String sessionId);
}