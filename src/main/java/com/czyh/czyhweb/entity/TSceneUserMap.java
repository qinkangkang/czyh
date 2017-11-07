package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_scene_user_map")
public class TSceneUserMap extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fsceneCode;
	private Integer fuserId;
	private Integer fstatus;
	private Date fcreateTime;

	// Constructors

	/** default constructor */
	public TSceneUserMap() {
	}

	/** minimal constructor */
	public TSceneUserMap(String id) {
		this.id = id;
	}

	// Property accessors
	@Column(name = "fSceneCode")
	public String getFsceneCode() {
		return this.fsceneCode;
	}

	public void setFsceneCode(String fsceneCode) {
		this.fsceneCode = fsceneCode;
	}

	@Column(name = "fUserId")
	public Integer getFuserId() {
		return this.fuserId;
	}

	public void setFuserId(Integer fuserId) {
		this.fuserId = fuserId;
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

	public void setFcreateTime(Date date) {
		this.fcreateTime = date;
	}

}