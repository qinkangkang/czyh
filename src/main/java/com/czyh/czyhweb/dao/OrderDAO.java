package com.czyh.czyhweb.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TOrder;

public interface OrderDAO extends JpaRepository<TOrder, String>, JpaSpecificationExecutor<TOrder> {

	@Query("select max(t.forderNum) from TOrder t where t.fcreateTime > ?1")
	String getMaxOrderNum(Date date);

	@Modifying
	@Query("update TOrder t set t.fstatus = ?1 where t.id = ?2")
	void updateOrderStatus(Integer status, String id);

	@Modifying
	@Query("update TOrder t set t.flockFlag = ?1 where t.id = ?2")
	void updateOrderLockFlag(Integer flockFlag, String id);

	@Modifying
	@Query("update TOrder t set t.fcsRemark = concat(t.fcsRemark, ?2) where t.id = ?1")
	void appendCsRemark(String id, String csRemark);

	@Modifying
	@Query("update TOrder t set t.fcsRemark = ?2 where t.id = ?1")
	void updateCsRemark(String id, String csRemark);

	@Query("select count(t.id) from TOrder t where t.TEvent.id = ?1 and t.fstatus < 100")
	Long getOrderCountByEventId(String eventId);

	TOrder getByFstatusAndForderNum(Integer status, String orderNum);

	@Query("select count(t.id) from TOrder t where t.TCustomer.id = ?1 AND t.ftotal = 0 AND t.fstatus in (?2, ?3, ?4)")
	Integer countZeroOrderNumber(String string, int i, int j, int k);

	@Query("select count(t.id) from TOrder t where t.TCustomer.id = ?1 AND t.ftotal > 0 AND t.fstatus in (?2, ?3, ?4)")
	Integer countPayOrderNumber(String string, int i, int j, int k);

	@Query("select COALESCE(SUM(t.ftotal),0) from TOrder t where t.TCustomer.id = ?1 and fStatus in (?2, ?3, ?4) ")
	BigDecimal countTotalOrder(String string, int i, int j, int k);
	
	@Query("select t from TOrder t where t.fstatus > 20 and t.fstatus <100 and t.fcreateTime > '2016-07-25' order by t.fcreateTime")
	List<TOrder> findAllByStatus();
	
//	@Modifying
//	@Query("update TOrder t set t.fcommentRewardAmount = ?1 where t.id = ?2")
//	void updateOrderRewardAmount(BigDecimal fcommentRewardAmount, String id);
//	
//	@Modifying
//	@Query("update TOrder t set t.fCommentRewardBonus = ?1 where t.id = ?2")
//	void updateOrderRewardBonus(Integer RewardBonus, String id);

}