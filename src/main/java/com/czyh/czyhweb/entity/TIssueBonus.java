package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_issue_bonus")
public class TIssueBonus extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String ftitle;
	private String fdec;
	private Integer fbonus;
	private Date fbeginTime;
	private Date fendTime;
	private Integer ftype;
	private Date fcreateTime;
	private Integer fcount;

	// Constructors

	/** default constructor */
	public TIssueBonus() {
	}

	/** minimal constructor */
	public TIssueBonus(String id) {
		this.id = id;
	}

	// Property accessors
	@Column(name = "fTitle")
	public String getFtitle() {
		return this.ftitle;
	}

	public void setFtitle(String ftitle) {
		this.ftitle = ftitle;
	}

	@Column(name = "fDec")
	public String getFdec() {
		return this.fdec;
	}

	public void setFdec(String fdec) {
		this.fdec = fdec;
	}

	@Column(name = "fBonus")
	public Integer getFbonus() {
		return this.fbonus;
	}

	public void setFbonus(Integer fbonus) {
		this.fbonus = fbonus;
	}

	@Column(name = "fBeginTime", length = 19)
	public Date getFbeginTime() {
		return this.fbeginTime;
	}

	public void setFbeginTime(Date fbeginTime) {
		this.fbeginTime = fbeginTime;
	}

	@Column(name = "fEndTime", length = 19)
	public Date getFendTime() {
		return this.fendTime;
	}

	public void setFendTime(Date fendTime) {
		this.fendTime = fendTime;
	}

	@Column(name = "fType")
	public Integer getFtype() {
		return this.ftype;
	}

	public void setFtype(Integer ftype) {
		this.ftype = ftype;
	}

	@Column(name = "fCreateTime", length = 19)
	public Date getFcreateTime() {
		return this.fcreateTime;
	}

	public void setFcreateTime(Date fcreateTime) {
		this.fcreateTime = fcreateTime;
	}

	@Column(name = "fCount")
	public Integer getFcount() {
		return fcount;
	}

	public void setFcount(Integer fcount) {
		this.fcount = fcount;
	}

}