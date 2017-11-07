package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_carnival_customer_activity")
public class TCarnivalCustomerActivity extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fcarnivalId;
	private String fccId;
	private Integer ftype;
	private Date ftime;
	private String fdata;
	private String fgps;

	// Constructors

	/** default constructor */
	public TCarnivalCustomerActivity() {
	}

	/** minimal constructor */
	public TCarnivalCustomerActivity(String id) {
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

	@Column(name = "fCcId", length = 36)
	public String getFccId() {
		return this.fccId;
	}

	public void setFccId(String fccId) {
		this.fccId = fccId;
	}

	@Column(name = "fType")
	public Integer getFtype() {
		return this.ftype;
	}

	public void setFtype(Integer ftype) {
		this.ftype = ftype;
	}

	@Column(name = "fTime", length = 19)
	public Date getFtime() {
		return this.ftime;
	}

	public void setFtime(Date ftime) {
		this.ftime = ftime;
	}

	@Column(name = "fData")
	public String getFdata() {
		return this.fdata;
	}

	public void setFdata(String fdata) {
		this.fdata = fdata;
	}

	@Column(name = "fGPS")
	public String getFgps() {
		return this.fgps;
	}

	public void setFgps(String fgps) {
		this.fgps = fgps;
	}

}