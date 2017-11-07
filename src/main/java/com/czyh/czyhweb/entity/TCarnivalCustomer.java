package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_carnival_customer")
public class TCarnivalCustomer extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fcarnivalId;
	private String fcustomerId;
	private String fphoto;
	private String fname;
	private Integer flevel;
	private Integer fcredit;
	private String fprizeId;
	private String fdayPrizeId;
	private Integer fdelivery;
	private Date fdeliveryTime;
	private Integer fsweepCount;
	private Integer fcredentialCount;
	private String fprizeOrderId;
	private Date fcreateTime;
	private Date fupdateTime;

	// Constructors

	/** default constructor */
	public TCarnivalCustomer() {
	}

	/** minimal constructor */
	public TCarnivalCustomer(String id) {
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

	@Column(name = "fCustomerId", length = 36)
	public String getFcustomerId() {
		return this.fcustomerId;
	}

	public void setFcustomerId(String fcustomerId) {
		this.fcustomerId = fcustomerId;
	}

	@Column(name = "fPhoto", length = 2048)
	public String getFphoto() {
		return this.fphoto;
	}

	public void setFphoto(String fphoto) {
		this.fphoto = fphoto;
	}

	@Column(name = "fName")
	public String getFname() {
		return this.fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	@Column(name = "fLevel")
	public Integer getFlevel() {
		return this.flevel;
	}

	public void setFlevel(Integer flevel) {
		this.flevel = flevel;
	}

	@Column(name = "fCredit")
	public Integer getFcredit() {
		return this.fcredit;
	}

	public void setFcredit(Integer fcredit) {
		this.fcredit = fcredit;
	}

	@Column(name = "fPrizeId", length = 36)
	public String getFprizeId() {
		return this.fprizeId;
	}

	public void setFprizeId(String fprizeId) {
		this.fprizeId = fprizeId;
	}

	@Column(name = "fDayPrizeId", length = 36)
	public String getFdayPrizeId() {
		return this.fdayPrizeId;
	}

	public void setFdayPrizeId(String fdayPrizeId) {
		this.fdayPrizeId = fdayPrizeId;
	}

	@Column(name = "fDelivery")
	public Integer getFdelivery() {
		return this.fdelivery;
	}

	public void setFdelivery(Integer fdelivery) {
		this.fdelivery = fdelivery;
	}

	@Column(name = "fDeliveryTime", length = 19)
	public Date getFdeliveryTime() {
		return this.fdeliveryTime;
	}

	public void setFdeliveryTime(Date fdeliveryTime) {
		this.fdeliveryTime = fdeliveryTime;
	}

	@Column(name = "fSweepCount")
	public Integer getFsweepCount() {
		return this.fsweepCount;
	}

	public void setFsweepCount(Integer fsweepCount) {
		this.fsweepCount = fsweepCount;
	}

	@Column(name = "fCredentialCount")
	public Integer getFcredentialCount() {
		return this.fcredentialCount;
	}

	public void setFcredentialCount(Integer fcredentialCount) {
		this.fcredentialCount = fcredentialCount;
	}

	@Column(name = "fPrizeOrderId", length = 36)
	public String getFprizeOrderId() {
		return this.fprizeOrderId;
	}

	public void setFprizeOrderId(String fprizeOrderId) {
		this.fprizeOrderId = fprizeOrderId;
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