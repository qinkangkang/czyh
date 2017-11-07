package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_carnival_customer_credential")
public class TCarnivalCustomerCredential extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fcarnivalId;
	private String fccId;
	private Integer fcredentialType;
	private Integer fnumber;
	private Integer fgetNumber;
	private Date fdate;
	private Date fcreateTime;
	private Date fupdateTime;

	// Constructors

	/** default constructor */
	public TCarnivalCustomerCredential() {
	}

	/** minimal constructor */
	public TCarnivalCustomerCredential(String id) {
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

	@Column(name = "fCredentialType")
	public Integer getFcredentialType() {
		return this.fcredentialType;
	}

	public void setFcredentialType(Integer fcredentialType) {
		this.fcredentialType = fcredentialType;
	}

	@Column(name = "fNumber")
	public Integer getFnumber() {
		return this.fnumber;
	}

	public void setFnumber(Integer fnumber) {
		this.fnumber = fnumber;
	}

	@Column(name = "fGetNumber")
	public Integer getFgetNumber() {
		return this.fgetNumber;
	}

	public void setFgetNumber(Integer fgetNumber) {
		this.fgetNumber = fgetNumber;
	}

	@Column(name = "fDate", length = 19)
	public Date getFdate() {
		return this.fdate;
	}

	public void setFdate(Date fdate) {
		this.fdate = fdate;
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