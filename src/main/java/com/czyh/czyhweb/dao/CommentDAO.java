package com.czyh.czyhweb.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TComment;

public interface CommentDAO extends JpaRepository<TComment, String>, JpaSpecificationExecutor<TComment> {

	@Query("select count(t.id) from TComment t where t.fobjectId = ?1 and t.ftype = ?2")
	Long getCommentCount(String objectId, Integer type);
	
	@Modifying
	@Query("update TComment t set t.fstatus = ?2 where t.id = ?1")
	void updateStatus(String commentId, Integer i);
	
	@Modifying
	@Query("update TComment t set t.forder = ?2 where t.id = ?1")
	void updateOrder(String string, Integer valueOf);
	
	@Modifying
	@Query("update TComment t set t.fstatus = ?2,t.fcommentRewardAmount = ?3 where t.id = ?1")
	void updateRewardAmount(String commentId, Integer i,BigDecimal rewardAmount);
	
	@Modifying
	@Query("update TComment t set t.fstatus = ?2,t.fcommentRewardBonus = ?3 where t.id = ?1")
	void updateRewardBonus(String commentId, Integer i,Integer rewardBonus);
}