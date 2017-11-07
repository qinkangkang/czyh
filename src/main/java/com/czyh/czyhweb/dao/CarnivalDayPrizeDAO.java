package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCarnivalDayPrize;

public interface CarnivalDayPrizeDAO
		extends JpaRepository<TCarnivalDayPrize, String>, JpaSpecificationExecutor<TCarnivalDayPrize> {

	@Modifying
	@Query("delete from TCarnivalDayPrize t where t.fprizeId = ?1")
	void delCarnivalDaysPrize(String fprizeId);

}