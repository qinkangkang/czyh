package com.czyh.czyhweb.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

/**
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a> Version 1.1.0
 * @since 2012-8-2 下午5:36:39
 */
@Entity
@Table(name = "t_module")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.czyh.czyhweb.entity.TModule")
public class TModule extends IdEntity implements Comparable<TModule> {

	private static final long serialVersionUID = 1L;

	private String name;

	/**
	 * 对应的模块全类名
	 */
	private String className;

	private String menuView;

	/**
	 * 模块的入口地址
	 */
	private String url;

	private String description;

	/**
	 * 标志符，用于授权名称（类似module:save）
	 */
	private String sn;

	/**
	 * 模块的排序号,越小优先级越高
	 */
	private Integer priority = 999;

	private TModule parent;

	private List<TModule> children = new ArrayList<TModule>();

	private Set<TRole> roles = new HashSet<TRole>(0);

	// Constructors

	/** default constructor */
	public TModule() {
	}

	/** minimal constructor */
	public TModule(Long id) {
		this.id = id;
	}

	/**
	 * 返回 name 的值
	 * 
	 * @return name
	 */
	@NotBlank
	@Length(max = 64)
	@Column(length = 64)
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

	/**
	 * @return the className
	 */
	@Column(name = "class_name", length = 256)
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * 返回 url 的值
	 * 
	 * @return url
	 */
	@NotBlank
	@Length(max = 256)
	@Column(length = 256)
	public String getUrl() {
		return url;
	}

	/**
	 * 设置 url 的值
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
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

	/**
	 * 返回 priority 的值
	 * 
	 * @return priority
	 */
	@NotNull
	@Range(min = 1, max = 999)
	@Column(length = 3)
	public Integer getPriority() {
		return priority;
	}

	/**
	 * 设置 priority 的值
	 * 
	 * @param priority
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * 返回 parent 的值
	 * 
	 * @return parent
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	public TModule getParent() {
		return parent;
	}

	/**
	 * 设置 parent 的值
	 * 
	 * @param parent
	 */
	public void setParent(TModule parent) {
		this.parent = parent;
	}

	/**
	 * 返回 children 的值
	 * 
	 * @return children
	 */
	@OneToMany(mappedBy = "parent", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@OrderBy("priority ASC")
	public List<TModule> getChildren() {
		return children;
	}

	/**
	 * 设置 children 的值
	 * 
	 * @param children
	 */
	public void setChildren(List<TModule> children) {
		this.children = children;
	}

	/**
	 * 返回 sn 的值
	 * 
	 * @return sn
	 */
	@NotBlank
	@Length(max = 32)
	@Column(length = 32, unique = true)
	public String getSn() {
		return sn;
	}

	/**
	 * 设置 sn 的值
	 * 
	 * @param sn
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "modules")
	public Set<TRole> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<TRole> roles) {
		this.roles = roles;
	}

	@Override
	public int compareTo(TModule m) {
		if (m == null) {
			return -1;
		} else if (m == this) {
			return 0;
		} else if (this.priority < m.getPriority()) {
			return -1;
		} else if (this.priority > m.getPriority()) {
			return 1;
		}
		return 0;
	}

	@Transient
	public String getMenuView() {
		return menuView;
	}

	public void setMenuView(String menuView) {
		this.menuView = menuView;
	}
}