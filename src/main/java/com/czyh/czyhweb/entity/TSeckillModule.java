package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_seckill_module")
public class TSeckillModule extends UuidEntity {

	private static final long serialVersionUID = 1L;

	private String fgoodsId;
	// private String fgoodsTitle;
	// private String fgoodsSubTitle;
	// private BigDecimal fgoodsPrice;
	// private BigDecimal fgoodsPriceMoney;
	// private Integer fgoodsLimitation;
	// private String fimage;
	private Integer fgoodstatus;
	private Integer ftype;
	private Integer ftodaySeckillType;
	private Date fgoodsCreateTime;
	private Date fgoodsUpdateTime;

	/** default constructor */
	public TSeckillModule() {
	}

	public TSeckillModule(String id) {
		this.id = id;
	}

	@Column(name = "fGoodsId", length = 36)
	public String getFgoodsId() {
		return this.fgoodsId;
	}

	public void setFgoodsId(String fgoodsId) {
		this.fgoodsId = fgoodsId;
	}

	@Column(name = "fGoodstatus")
	public Integer getFgoodstatus() {
		return this.fgoodstatus;
	}

	public void setFgoodstatus(Integer fgoodstatus) {
		this.fgoodstatus = fgoodstatus;
	}

	@Column(name = "fType")
	public Integer getFtype() {
		return this.ftype;
	}

	public void setFtype(Integer ftype) {
		this.ftype = ftype;
	}

	@Column(name = "fTodaySeckillType")
	public Integer getFtodaySeckillType() {
		return this.ftodaySeckillType;
	}

	public void setFtodaySeckillType(Integer ftodaySeckillType) {
		this.ftodaySeckillType = ftodaySeckillType;
	}

	@Column(name = "fGoodsCreateTime", length = 19)
	public Date getFgoodsCreateTime() {
		return this.fgoodsCreateTime;
	}

	public void setFgoodsCreateTime(Date fgoodsCreateTime) {
		this.fgoodsCreateTime = fgoodsCreateTime;
	}

	@Column(name = "fGoodsUpdateTime", length = 19)
	public Date getFgoodsUpdateTime() {
		return this.fgoodsUpdateTime;
	}

	public void setFgoodsUpdateTime(Date fgoodsUpdateTime) {
		this.fgoodsUpdateTime = fgoodsUpdateTime;
	}

}