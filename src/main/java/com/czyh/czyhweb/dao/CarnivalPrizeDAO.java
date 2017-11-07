package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCarnivalPrize;

public interface CarnivalPrizeDAO
		extends JpaRepository<TCarnivalPrize, String>, JpaSpecificationExecutor<TCarnivalPrize> {

	@Query("from TCarnivalPrize t where t.fcarnivalId =?1 and t.flevel=?2")
	TCarnivalPrize findRepeatPrize(String carnivalId, Integer flag);
	

	@Modifying
	@Query("delete from TCarnivalPrize t where t.id = ?1")
	void delCarnivalPrize(String id);
}