package com.czyh.czyhweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TEventRelation;

public interface EventRelationDAO
		extends JpaRepository<TEventRelation, String>, JpaSpecificationExecutor<TEventRelation> {

	@Query("from TEventRelation t where t.feventId = ?1 order by t.forder desc")
	List<TEventRelation> getEventRelationList(String eventId);

	@Query("delete from TEventRelation t where t.id = ?1")
	void deleteEventRelation(String id);
}