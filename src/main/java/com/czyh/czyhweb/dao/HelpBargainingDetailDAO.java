package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.THelpBargainingDetail;

public interface HelpBargainingDetailDAO
		extends JpaRepository<THelpBargainingDetail, String>, JpaSpecificationExecutor<THelpBargainingDetail> {

}