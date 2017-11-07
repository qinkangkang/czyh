package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TDictionaryClass;

public interface DictionaryClassDao extends JpaRepository<TDictionaryClass, Long>,
		JpaSpecificationExecutor<TDictionaryClass> {

}