package com.czyh.czyhweb.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TGoodsSku;

public interface EventDAO extends JpaRepository<TEvent, String>, JpaSpecificationExecutor<TEvent> {

	@Modifying
	@Query("delete TEvent t where t.fstatus is null and t.fcreateTime <= ?1")
	int clearTempEvent(Date date);

	@Query("select t from TEvent t where t.fdetailHtmlUrl is null and t.fdetail is not null and (t.fstatus between 4 and 99)")
	List<TEvent> findByFdetailHtmlUrlIsnull();

	@Modifying
	@Query("update TEvent t set t.fdetailHtmlUrl = ?2 where t.id = ?1")
	void updateFdetailHtmlUrl(String id, String fdetailHtmlUrl);

	@Modifying
	@Query("update TEvent t set t.fimage1 = ?2, t.fimage2 = ?3 where t.id = ?1")
	void updateImage12(String id, String image1, String image2);

	@Modifying
	@Query("update TEvent t set t.fstatus = ?1 where t.id = ?2")
	void saveStatus(Integer status, String id);

	@Modifying
	@Query("update TEvent t set t.fonSaleTime = ?1, t.fstatus = ?2 where t.id = ?3")
	void saveOnTimeAndStatus(Date onSaleTime, Integer status, String id);

	@Modifying
	@Query("update TEvent t set t.fstatus = ?1 where t.id = ?2")
	void saveOffTimeAndStatus(Integer status, String id);

	@Modifying
	@Query("update TEvent t set t.fonSaleTime = ?1, t.foffSaleTime = ?2 where t.id = ?3")
	void saveOOTime(Date onSaleTime, Date offSaleTime, String id);

	// @Modifying
	// @Query("update TEvent t set t.fsaleFlag = (select sum(s.flimitation) from
	// TEventSession s where s.TEvent.id = ?1 and s.fstatus < 999) where t.id =
	// ?1")
	// void updateStockFlagBySession(String eventId);

//	@Modifying
//	@Query("update TEvent t set t.fsaleFlag = (select sum(s.fstock) from TEventSpec s where s.TEvent.id = ?1 and s.fstatus < 999) where t.id = ?1")
//	void updateStockFlagBySpec(String eventId);

//	@Modifying
//	@Query("update TEvent t set t.fsaleFlag = t.fsaleFlag + ?1 where t.id = ?2")
//	void addStock(Integer count, String eventId);

//	@Modifying
//	@Query("update TEvent t set t.fsaleFlag = t.fsaleFlag - ?1 where t.id = ?2")
//	void subStock(Integer count, String eventId);

	@Query("select COUNT(t.id) from TEvent t where t.ftypeA = ?1 AND t.fstatus = ?2 ")
	Long getCountByStatusAndType(Integer integer, int i);

	@Modifying
	@Query("update TEvent t set t.foffSaleTime = ?1 where t.id = ?2")
	void updatetEventFoffSaleTime(Date offSaleTime, String id);

	@Query("select t from TEvent t where t.id = ?1")
	TEvent getImageA(String eventId);

	@Modifying
	@Query("update TEvent t set t.fsalesType = ?1 where t.id = ?2")
	void updateSaleType(Integer saleType, String eventId);

	@Modifying
	@Query("update TEvent t set t.fstock = t.fstock + ?1 where t.id = ?2")
	void backStock(Integer count, String eventId);

	@Query("select t from TEvent t where t.id = ?1")
	TEvent getSeckillModule(String eventId);
	
	@Modifying
	@Query("update TEvent t set t.fgoodsSpecImage=?2 where t.id= ?1")
	void updateGoodsSpec(String eventId, String imagePath2);
	
	
}