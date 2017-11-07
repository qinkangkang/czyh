package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TFavorite;

public interface FavoriteDAO extends JpaRepository<TFavorite, String>, JpaSpecificationExecutor<TFavorite> {

	@Query("select t from TFavorite t where t.TCustomer.id = ?1 and t.fobjectId = ?2 and t.ftype = 1")
	TFavorite getFavoriteEventByCustomerIdAndObejctId(String customerId, String eventId);

	@Query("select t from TFavorite t where t.TCustomer.id = ?1 and t.fobjectId = ?2 and t.ftype = 2")
	TFavorite getFavoriteMerchantByCustomerIdAndObejctId(String customerId, String merchantId);

	@Query("select count(t.id) from TFavorite t where t.fobjectId = ?1 and t.ftype = ?2")
	Long getFavoriteCount(String objectId, Integer type);

}