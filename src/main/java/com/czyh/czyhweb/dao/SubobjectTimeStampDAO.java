package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TSubobjectTimeStamp;

public interface SubobjectTimeStampDAO
		extends JpaRepository<TSubobjectTimeStamp, String>, JpaSpecificationExecutor<TSubobjectTimeStamp> {
	
	@Query("from TSubobjectTimeStamp t where t.fsubObject = ?1")
	TSubobjectTimeStamp findByObject(int Object);

}