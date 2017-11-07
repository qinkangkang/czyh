package com.czyh.czyhweb.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_carnival_prize")
public class TCarnivalPrize extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fcarnivalId;
	private String ftitle;
	private Integer flevel;
	private Long fimage;
	private Integer fcount;
	private String feventId;
	private String fsessionId;
	private String fspecId;
	private String frule;

	// Constructors

	/** default constructor */
	public TCarnivalPrize() {
	}

	/** minimal constructor */
	public TCarnivalPrize(String id) {
		this.id = id;
	}

	// Property accessors
	@Column(name = "fCarnivalId", length = 36)
	public String getFcarnivalId() {
		return this.fcarnivalId;
	}

	public void setFcarnivalId(String fcarnivalId) {
		this.fcarnivalId = fcarnivalId;
	}

	@Column(name = "fTitle")
	public String getFtitle() {
		return this.ftitle;
	}

	public void setFtitle(String ftitle) {
		this.ftitle = ftitle;
	}

	@Column(name = "fLevel")
	public Integer getFlevel() {
		return this.flevel;
	}

	public void setFlevel(Integer flevel) {
		this.flevel = flevel;
	}

	@Column(name = "fImage")
	public Long getFimage() {
		return this.fimage;
	}

	public void setFimage(Long fimage) {
		this.fimage = fimage;
	}

	@Column(name = "fCount")
	public Integer getFcount() {
		return this.fcount;
	}

	public void setFcount(Integer fcount) {
		this.fcount = fcount;
	}

	@Column(name = "fEventId", length = 36)
	public String getFeventId() {
		return this.feventId;
	}

	public void setFeventId(String feventId) {
		this.feventId = feventId;
	}

	@Column(name = "fSessionId", length = 36)
	public String getFsessionId() {
		return this.fsessionId;
	}

	public void setFsessionId(String fsessionId) {
		this.fsessionId = fsessionId;
	}

	@Column(name = "fSpecId", length = 36)
	public String getFspecId() {
		return this.fspecId;
	}

	public void setFspecId(String fspecId) {
		this.fspecId = fspecId;
	}

	@Column(name = "fRule")
	public String getFrule() {
		return this.frule;
	}

	public void setFrule(String frule) {
		this.frule = frule;
	}

}