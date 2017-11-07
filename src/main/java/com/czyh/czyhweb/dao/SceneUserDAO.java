package com.czyh.czyhweb.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TSceneUser;

public interface SceneUserDAO extends JpaRepository<TSceneUser, String>, JpaSpecificationExecutor<TSceneUser> {
	@Query("select t from TSceneUser t where t.fopenId = ?1 and t.fsubscribe = 1 ")
	TSceneUser findBysceneStrAndopenID(String openID);
	
	@Modifying
	@Query("update TSceneUser t set t.fsubscribe = ?2, t.funSubscribe = ?3,t.funSubscribeTime = ?4 where t.id = ?1")
	void setUnSubscribe(String string, int i, int j, Date date);
	
	@Query("select t from TSceneUser t where t.fopenId = ?1 ")
	TSceneUser findOneByOpenid(String openID);
	
	@Modifying
	@Query("update TSceneUser t set t.fsceneGps = ?1 where t.id = ?2")
	void saveGps(String gps, String string);

	@Modifying
	@Query("update TSceneUser t set t.fdelivery = ?2,t.fdeliveryTime = ?3 where t.fopenId = ?1")
	void updateDelivery(String openid, int i, Date now);
	
	@Modifying
	@Query("update TSceneUser t set t.fregister = ?2,t.fregisterTime = ?3 where t.fopenId = ?1")
	void updateRegister(String openid, int i, Date now);
	
	@Query("select t.fsceneStr from TSceneUser t GROUP BY t.fsceneStr ")
	List<String> getSceneStrList();
	
	@Query("select COUNT(1) from TSceneUser t WHERE fsubscribeTime BETWEEN ?2 and ?3 AND fsceneStr = ?1 ")
	Integer getSubscribeNumBysceneStr(String fsceneStr, Date fcreateTimeStart, Date fcreateTimeEnd);
	
	@Query("select COUNT(1) from TSceneUser t WHERE fsubscribeTime BETWEEN ?2 and ?3 AND fsceneStr = ?1 and fsceneGps IS NOT NULL")
	Integer getGpsNum(String sceneStr, Date fcreateTimeStart, Date fcreateTimeEnd);
	
	@Query("select COUNT(1) from TSceneUser t WHERE fsubscribeTime BETWEEN ?2 and ?3 AND fsceneStr = ?1 AND fregister = 1 ")
	Integer getRegisterNum(String sceneStr, Date fcreateTimeStart, Date fcreateTimeEnd);
	
	@Query("select COUNT(1) from TSceneUser t WHERE fsceneStr = ?1 and funSubscribe = 1 ")
	Integer getUnRegisterNum(String sceneStr);
	
	@Query("select COUNT(1) from TSceneUser t WHERE fsubscribeTime BETWEEN ?2 and ?3 AND fsceneStr = ?1 and funSubscribe = 1 and  DATE_FORMAT(fsubscribeTime,'%Y-%m-%d') like DATE_FORMAT(funSubscribeTime,'%Y-%m-%d') ")
	Integer getTodayUnRegisterNum(String sceneStr, Date fcreateTimeStart, Date fcreateTimeEnd);
	
	@Query("select COUNT(DISTINCT t.fsceneStr) from TSceneUser t WHERE t.fsceneStr = ?1")
	long countSceneTotal(String string);
	
	@Query("select COUNT(DISTINCT t.fsceneStr) from TSceneUser t WHERE 1 = 1 ")
	long countTotal();
	
	@Query("select COUNT(1) from TSceneUser t WHERE fsceneStr = ?1 and fsubscribe = 1 ")
	Integer getRegisterNum(String sceneStr);
	
}