package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TEventText;

public interface EventTextDAO extends JpaRepository<TEventText, String>, JpaSpecificationExecutor<TEventText> {

}