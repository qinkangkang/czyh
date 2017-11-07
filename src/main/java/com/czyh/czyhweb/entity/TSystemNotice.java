package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_system_notice")
public class TSystemNotice extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String ftitle;
	private String fnoticeContent;
	private Integer fnoticeType;
	private String fnoticeVersion;
	private Integer fstatus;
	private Date fcreateTime;
	private Date fupdateTime;

	// Constructors

	/** default constructor */
	public TSystemNotice() {
	}

	/** minimal constructor */
	public TSystemNotice(String id) {
		this.id = id;
	}

	@Column(name = "ftitle")
	public String getFtitle() {
		return this.ftitle;
	}

	public void setFtitle(String ftitle) {
		this.ftitle = ftitle;
	}

	@Column(name = "fNoticeContent")
	public String getFnoticeContent() {
		return this.fnoticeContent;
	}

	public void setFnoticeContent(String fnoticeContent) {
		this.fnoticeContent = fnoticeContent;
	}

	@Column(name = "fNoticeType")
	public Integer getFnoticeType() {
		return this.fnoticeType;
	}

	public void setFnoticeType(Integer fnoticeType) {
		this.fnoticeType = fnoticeType;
	}

	@Column(name = "fNoticeVersion", length = 32)
	public String getFnoticeVersion() {
		return this.fnoticeVersion;
	}

	public void setFnoticeVersion(String fnoticeVersion) {
		this.fnoticeVersion = fnoticeVersion;
	}

	@Column(name = "fStatus")
	public Integer getFstatus() {
		return this.fstatus;
	}

	public void setFstatus(Integer fstatus) {
		this.fstatus = fstatus;
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