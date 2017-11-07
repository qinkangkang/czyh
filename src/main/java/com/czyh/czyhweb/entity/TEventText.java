package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_goods_text")
public class TEventText extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String feventId;
	private String ftitle;
	private Integer fisBody;
	private String fcontent;
	private Long fcreaterId;
	private Date fcreateTime;
	private Date fupdateTime;

	// Constructors

	/** default constructor */
	public TEventText() {
	}

	/** minimal constructor */
	public TEventText(String id) {
		this.id = id;
	}

	// Property accessors
	@Column(name = "fEventID", length = 36)
	public String getFeventId() {
		return this.feventId;
	}

	public void setFeventId(String feventId) {
		this.feventId = feventId;
	}
	
	@Column(name = "fTitle")
	public String getFtitle() {
		return this.ftitle;
	}

	public void setFtitle(String ftitle) {
		this.ftitle = ftitle;
	}

	@Column(name = "fIsBody")
	public Integer getFisBody() {
		return this.fisBody;
	}

	public void setFisBody(Integer fisBody) {
		this.fisBody = fisBody;
	}

	@Column(name = "fContent")
	public String getFcontent() {
		return this.fcontent;
	}

	public void setFcontent(String fcontent) {
		this.fcontent = fcontent;
	}
	
	@Column(name = "fCreaterId")
	public Long getFcreaterId() {
		return this.fcreaterId;
	}

	public void setFcreaterId(Long fcreaterId) {
		this.fcreaterId = fcreaterId;
	}

	@Column(name = "fCreateTime", length = 19)
	public Date getFcreateTime() {
		return this.fcreateTime;
	}

	public void setFcreateTime(Date fcreateTime) {
		this.fcreateTime = fcreateTime;
	}

	@Column(name = "fUpdateTime", length = 19)
	public Date getFupdateTime() {
		return this.fupdateTime;
	}

	public void setFupdateTime(Date fupdateTime) {
		this.fupdateTime = fupdateTime;
	}

}