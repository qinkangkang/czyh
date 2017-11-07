package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TTimedSpike;

public interface TimedSpikeDAO extends JpaRepository<TTimedSpike, String>, JpaSpecificationExecutor<TTimedSpike> {

}