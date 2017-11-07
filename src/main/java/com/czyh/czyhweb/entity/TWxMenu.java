package com.czyh.czyhweb.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "t_wx_menu")
public class TWxMenu extends IdEntity {

	private static final long serialVersionUID = 1L;

	private String menuName;
	private String content;
	private String url;
	private Integer type;
	private Integer menuOrder;
	private Set<TWxMenuItem> TWxMenuItems = new HashSet<TWxMenuItem>(0);

	// Constructors

	/** default constructor */
	public TWxMenu() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "menu_name", length = 16)
	public String getMenuName() {
		return this.menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	@Column(name = "content")
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "url")
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "type")
	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "menu_order")
	public Integer getMenuOrder() {
		return menuOrder;
	}

	public void setMenuOrder(Integer menuOrder) {
		this.menuOrder = menuOrder;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "TWxMenu")
	public Set<TWxMenuItem> getTWxMenuItems() {
		return this.TWxMenuItems;
	}

	public void setTWxMenuItems(Set<TWxMenuItem> TWxMenuItems) {
		this.TWxMenuItems = TWxMenuItems;
	}

}