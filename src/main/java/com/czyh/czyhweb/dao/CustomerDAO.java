package com.czyh.czyhweb.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TCustomer;

public interface CustomerDAO extends JpaRepository<TCustomer, String>, JpaSpecificationExecutor<TCustomer> {

	@Query("select count(t.id) from TCustomer t where t.fusername = ?1 and t.fstatus < 999")
	Long checkUsername(String username);

	@Query("select count(t.id) from TCustomer t where t.id != (select s.TCustomer.id from TSponsor s where s.id = ?1) and t.fusername = ?2 and t.fstatus < 999")
	Long checkEditUsername(String id, String username);

	@Query("select t from TCustomer t where t.fusername = ?1 and t.ftype = ?2 and t.fstatus < 999")
	TCustomer getByFusernameAndFtype(String fusername, Integer ftype);

	@Query("select t from TCustomer t where t.fticket = ?1 and t.ftype = ?2 and t.fstatus < 999")
	TCustomer getByFticketAndFtype(String fticket, Integer ftype);

	@Modifying
	@Query("update TCustomer t set t.fstatus = ?2 where t.id = ?1")
	void updateStatus(String customerId, Integer status);

	@Modifying
	@Query("update TCustomer t set t.fweixinUnionId = ?1 where t.id = ?2")
	void saveUnionId(String unionId, String customerId);

	@Modifying
	@Query("update TCustomer t set t.fpassword = ?1, t.fsalt = ?2 where t.id = ?3")
	void savePasswordAndSalt(String password, String fsalt, String customerId);

	@Modifying
	@Query("update TCustomer t set t.fusername = ?1, t.fphone = ?1 where t.id = ?2")
	void saveUsernameAndPhone(String phone, String customerId);

	@Modifying
	@Query("update TCustomer t set t.fphoto = ?1 where t.id = ?2")
	void savePhoto(String photo, String customerId);

	@Modifying
	@Query("update TCustomer t set t.fticket = null where t.id = ?1")
	void clearTicket(String customerId);
	
	@Query("select t from TCustomer t where t.fusername = ?1 and t.fstatus < 999")
	TCustomer getByCustomerId(String fusername);
	
	@Query("select t.id as id from TCustomer t where t.fcreateTime between ?1 and ?2 and t.ftype = 1 and t.fstatus = 1 and t.fweixinId is not null ")
	List<String> getByTime(Date from,Date end);
	
	@Query("select t.fweixinId as id from TCustomer t where t.fcreateTime between ?1 and ?2 and t.ftype = 1 and t.fstatus = 1 and t.fweixinId is not null ")
	List<String> getOpenIdByTime(Date from,Date end);

}