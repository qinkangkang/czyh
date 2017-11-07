package com.czyh.czyhweb.entity;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a> Version 1.1.0
 * @since 2012-6-7 上午11:07:45
 */
@Entity
@Table(name = "t_role")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.czyh.czyhweb.entity.TRole")
public class TRole extends IdEntity {

	private static final long serialVersionUID = 1L;

	private String name;

	private String code;

	private String description;

	private Set<TUser> users = new HashSet<TUser>(0);

	private Set<TModule> modules = new HashSet<TModule>(0);

	/**
	 * 返回 name 的值
	 * 
	 * @return name
	 */
	@NotBlank
	@Length(max = 64)
	@Column(length = 64, nullable = false, unique = true)
	public String getName() {
		return name;
	}

	/**
	 * 设置 name 的值
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "code", length = 64, nullable = false, unique = true)
	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 返回 description 的值
	 * 
	 * @return description
	 */
	@Length(max = 256)
	@Column(length = 256)
	public String getDescription() {
		return description;
	}

	/**
	 * 设置 description 的值
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "roles")
	public Set<TUser> getUsers() {
		return this.users;
	}

	public void setUsers(Set<TUser> users) {
		this.users = users;
	}

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@JoinTable(name = "t_role_module", joinColumns = {
			@JoinColumn(name = "role_id", nullable = false, updatable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "module_id", nullable = false, updatable = false) })
	public Set<TModule> getModules() {
		return this.modules;
	}

	public void setModules(Set<TModule> modules) {
		this.modules = modules;
	}

}