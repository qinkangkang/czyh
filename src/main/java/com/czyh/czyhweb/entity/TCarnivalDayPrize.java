package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_carnival_day_prize")
public class TCarnivalDayPrize extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fcarnivalId;
	private String fprizeId;
	private Integer flevel;
	private Date fcarnivalDay;
	private Integer fcarnivalDaySerial;
	private Date fstartTime;
	private Date fendTime;
	private Integer fcount;
	private Integer funSendCount;
	private Integer facceptCount;
	private Integer freceiveCount;
	// Constructors

	/** default constructor */
	public TCarnivalDayPrize() {
	}

	/** minimal constructor */
	public TCarnivalDayPrize(String id) {
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

	@Column(name = "fPrizeId", length = 36)
	public String getFprizeId() {
		return this.fprizeId;
	}

	public void setFprizeId(String fprizeId) {
		this.fprizeId = fprizeId;
	}

	@Column(name = "fLevel")
	public Integer getFlevel() {
		return this.flevel;
	}

	public void setFlevel(Integer flevel) {
		this.flevel = flevel;
	}

	@Column(name = "fCarnivalDay", length = 19)
	public Date getFcarnivalDay() {
		return this.fcarnivalDay;
	}

	public void setFcarnivalDay(Date fcarnivalDay) {
		this.fcarnivalDay = fcarnivalDay;
	}

	@Column(name = "fCarnivalDaySerial")
	public Integer getFcarnivalDaySerial() {
		return this.fcarnivalDaySerial;
	}

	public void setFcarnivalDaySerial(Integer fcarnivalDaySerial) {
		this.fcarnivalDaySerial = fcarnivalDaySerial;
	}

	@Column(name = "fStartTime", length = 19)
	public Date getFstartTime() {
		return this.fstartTime;
	}

	public void setFstartTime(Date fstartTime) {
		this.fstartTime = fstartTime;
	}

	@Column(name = "fEndTime", length = 19)
	public Date getFendTime() {
		return this.fendTime;
	}

	public void setFendTime(Date fendTime) {
		this.fendTime = fendTime;
	}

	@Column(name = "fCount")
	public Integer getFcount() {
		return this.fcount;
	}

	public void setFcount(Integer fcount) {
		this.fcount = fcount;
	}

	@Column(name = "fUnSendCount")
	public Integer getFunSendCount() {
		return this.funSendCount;
	}

	public void setFunSendCount(Integer funSendCount) {
		this.funSendCount = funSendCount;
	}

	@Column(name = "fAcceptCount")
	public Integer getFacceptCount() {
		return this.facceptCount;
	}

	public void setFacceptCount(Integer facceptCount) {
		this.facceptCount = facceptCount;
	}

	@Column(name = "fReceiveCount")
	public Integer getFreceiveCount() {
		return this.freceiveCount;
	}

	public void setFreceiveCount(Integer freceiveCount) {
		this.freceiveCount = freceiveCount;
	}

}