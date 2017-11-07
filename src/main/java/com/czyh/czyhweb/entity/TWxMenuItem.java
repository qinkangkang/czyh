package com.czyh.czyhweb.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "t_wx_menu_item")
public class TWxMenuItem extends IdEntity {

	private static final long serialVersionUID = 1L;

	private TWxMenu TWxMenu;
	private String itemName;
	private String content;
	private String materialName;
	private Integer materialTypeId;
	private String materialUrl;
	private String text;
	private Integer type;
	private Integer menuOrder;

	// Constructors

	/** default constructor */
	public TWxMenuItem() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id")
	public TWxMenu getTWxMenu() {
		return this.TWxMenu;
	}

	public void setTWxMenu(TWxMenu TWxMenu) {
		this.TWxMenu = TWxMenu;
	}

	@Column(name = "item_name", length = 16)
	public String getItemName() {
		return this.itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Column(name = "content", length = 2048)
	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "material_name")
	public String getMaterialName() {
		return this.materialName;
	}

	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}

	@Column(name = "material_type_id")
	public Integer getMaterialTypeId() {
		return this.materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	@Column(name = "material_url")
	public String getMaterialUrl() {
		return this.materialUrl;
	}

	public void setMaterialUrl(String materialUrl) {
		this.materialUrl = materialUrl;
	}

	@Column(name = "text")
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
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

}