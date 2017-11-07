package com.czyh.czyhweb.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_goods_typeClass_category")
public class TGoodsTypeClassCategory extends IdEntity {

	private static final long serialVersionUID = 1L;
	
	private Long fcategoryId;
	private Long ftypeId;

	// Constructors

	/** default constructor */
	public TGoodsTypeClassCategory() {
	}


	@Column(name = "fCategoryId")
	public Long getFcategoryId() {
		return this.fcategoryId;
	}

	public void setFcategoryId(Long long1) {
		this.fcategoryId = long1;
	}

	@Column(name = "fTypeId")
	public Long getFtypeId() {
		return this.ftypeId;
	}

	public void setFtypeId(Long ftypeId) {
		this.ftypeId = ftypeId;
	}

}