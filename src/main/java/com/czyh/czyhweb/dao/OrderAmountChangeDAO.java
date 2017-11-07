package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TOrderAmountChange;

public interface OrderAmountChangeDAO
		extends JpaRepository<TOrderAmountChange, String>, JpaSpecificationExecutor<TOrderAmountChange> {

}