package com.czyh.czyhweb.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.czyh.czyhweb.entity.TUser;

public interface UserDAO extends JpaRepository<TUser, Long>, JpaSpecificationExecutor<TUser> {

	TUser getByUsername(String username);

	TUser getByPhone(String phone);

	TUser getByEmail(String email);

	TUser getByUsernameOrPhoneOrEmail(String username, String phone, String email);

	@Query("select count(t.id) from TUser t where t.username = ?1")
	Long checkUsername(String username);

	@Query("select count(t.id) from TUser t where t.username = ?1 and t.id != ?2")
	Long checkEditUsername(String username, Long id);

	@Query("select count(t.id) from TUser t where t.phone = ?1")
	Long checkPhone(String username);

	@Query("select count(t.id) from TUser t where t.phone = ?1 and t.id != ?2")
	Long checkPhone(String username, Long id);

	@Query("select count(t.id) from TUser t where t.email = ?1")
	Long checkEmail(String username);

	@Query("select count(t.id) from TUser t where t.email = ?1 and t.id != ?2")
	Long checkEmail(String username, Long id);

}