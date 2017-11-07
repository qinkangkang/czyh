package com.czyh.czyhweb.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a> Version 1.1.0
 * @since 2012-8-2 下午2:44:58
 */
@Entity
@Table(name = "t_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.czyh.czyhweb.entity.TUser")
public class TUser extends IdEntity {

	private static final long serialVersionUID = 1L;

	private String realname;
	private String username;
	private String password;
	private String plainPassword;
	private String salt;
	private String phone;
	private String email;
	private Integer status;
	private Date createTime;
	private String type;
	private String category;
	private Set<TRole> roles = new HashSet<TRole>(0);

	public TUser() {
	}

	public TUser(Long id) {
		this.id = id;
	}

	/**
	 * 返回 realname 的值
	 * 
	 * @return realname
	 */
	@NotBlank
	@Length(max = 32)
	@Column(length = 32, nullable = false)
	public String getRealname() {
		return realname;
	}

	/**
	 * 设置 realname 的值
	 * 
	 * @param realname
	 */
	public void setRealname(String realname) {
		this.realname = realname;
	}

	/**
	 * 返回 username 的值
	 * 
	 * @return username
	 */
	@NotBlank
	@Length(max = 32)
	@Column(length = 32, nullable = false, unique = true, updatable = false)
	public String getUsername() {
		return username;
	}

	/**
	 * 设置 username 的值
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 返回 password 的值
	 * 
	 * @return password
	 */
	@Column(length = 64, nullable = false)
	public String getPassword() {
		return password;
	}

	/**
	 * 设置 password 的值
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 返回 createTime 的值
	 * 
	 * @return createTime
	 */
	/**
	 * 帐号创建时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", updatable = false)
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * 设置 createTime 的值
	 * 
	 * @param createTime
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 返回 status 的值
	 * 
	 * @return status
	 */
	@NotNull
	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	/**
	 * 设置 status 的值
	 * 
	 * @param status
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 返回 plainPassword 的值
	 * 
	 * @return plainPassword
	 */
	@Transient
	public String getPlainPassword() {
		return plainPassword;
	}

	/**
	 * 设置 plainPassword 的值
	 * 
	 * @param plainPassword
	 */
	public void setPlainPassword(String plainPassword) {
		this.plainPassword = plainPassword;
	}

	/**
	 * 返回 salt 的值
	 * 
	 * @return salt
	 */
	@Column(length = 32, nullable = false)
	public String getSalt() {
		return salt;
	}

	/**
	 * 设置 salt 的值
	 * 
	 * @param salt
	 */
	public void setSalt(String salt) {
		this.salt = salt;
	}

	/**
	 * 返回 email 的值
	 * 
	 * @return email
	 */
	@Email
	@Length(max = 128)
	@Column(length = 128)
	public String getEmail() {
		return email;
	}

	/**
	 * 设置 email 的值
	 * 
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * 返回 type 的值
	 * 
	 * @return type
	 */
	@Length(max = 32)
	@Column(length = 32)
	public String getType() {
		return type;
	}

	/**
	 * 设置 type 的值
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 返回 phone 的值
	 * 
	 * @return phone
	 */
	@Length(max = 32)
	@Column(length = 32)
	public String getPhone() {
		return phone;
	}

	/**
	 * 设置 phone 的值
	 * 
	 * @param phone
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "category", length = 128)
	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@JoinTable(name = "t_user_role", joinColumns = {
			@JoinColumn(name = "user_id", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "role_id", nullable = false, updatable = false) })
	public Set<TRole> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<TRole> roles) {
		this.roles = roles;
	}

}