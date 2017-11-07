package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.czyh.czyhweb.entity.TCustomerTicket;

public interface CustomerTicketDAO
		extends JpaRepository<TCustomerTicket, String>, JpaSpecificationExecutor<TCustomerTicket> {

}