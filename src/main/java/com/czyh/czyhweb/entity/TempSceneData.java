package com.czyh.czyhweb.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tempSceneData")
public class TempSceneData extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String fsceneStr;
	private Integer fsubscribeNum;
	private Integer fgpsNum;
	private Integer fdeliveryNum;
	private Integer ftodaysubscribe;
	private Integer ftodayUnRegisterNum;
	private Integer funSubscribeNum;
	private Integer fhregisterNum;
	private Integer fsweepCount;
	private String fhsid;
	private Long fcreateTime;

	// Constructors

	/** default constructor */
	public TempSceneData() {
	}

	/** minimal constructor */
	public TempSceneData(String id) {
		this.id = id;
	}

	// Property accessors
	@Column(name = "fSceneStr")
	public String getFsceneStr() {
		return this.fsceneStr;
	}

	public void setFsceneStr(String fsceneStr) {
		this.fsceneStr = fsceneStr;
	}

	@Column(name = "fSubscribeNum")
	public Integer getFsubscribeNum() {
		return this.fsubscribeNum;
	}

	public void setFsubscribeNum(Integer fsubscribeNum) {
		this.fsubscribeNum = fsubscribeNum;
	}

	@Column(name = "fGpsNum")
	public Integer getFgpsNum() {
		return this.fgpsNum;
	}

	public void setFgpsNum(Integer fgpsNum) {
		this.fgpsNum = fgpsNum;
	}

	@Column(name = "fDeliveryNum")
	public Integer getFdeliveryNum() {
		return this.fdeliveryNum;
	}

	public void setFdeliveryNum(Integer fdeliveryNum) {
		this.fdeliveryNum = fdeliveryNum;
	}

	@Column(name = "fTodaysubscribe")
	public Integer getFtodaysubscribe() {
		return this.ftodaysubscribe;
	}

	public void setFtodaysubscribe(Integer ftodaysubscribe) {
		this.ftodaysubscribe = ftodaysubscribe;
	}

	@Column(name = "fTodayUnRegisterNum")
	public Integer getFtodayUnRegisterNum() {
		return this.ftodayUnRegisterNum;
	}

	public void setFtodayUnRegisterNum(Integer ftodayUnRegisterNum) {
		this.ftodayUnRegisterNum = ftodayUnRegisterNum;
	}

	@Column(name = "fUnSubscribeNum")
	public Integer getFunSubscribeNum() {
		return this.funSubscribeNum;
	}

	public void setFunSubscribeNum(Integer funSubscribeNum) {
		this.funSubscribeNum = funSubscribeNum;
	}

	@Column(name = "fHRegisterNum")
	public Integer getFhregisterNum() {
		return this.fhregisterNum;
	}

	public void setFhregisterNum(Integer fhregisterNum) {
		this.fhregisterNum = fhregisterNum;
	}

	@Column(name = "fSweepCount")
	public Integer getFsweepCount() {
		return fsweepCount;
	}

	public void setFsweepCount(Integer fsweepCount) {
		this.fsweepCount = fsweepCount;
	}

	@Column(name = "fHsid", length = 32)
	public String getFhsid() {
		return this.fhsid;
	}

	public void setFhsid(String fhsid) {
		this.fhsid = fhsid;
	}

	@Column(name = "fCreateTime")
	public Long getFcreateTime() {
		return this.fcreateTime;
	}

	public void setFcreateTime(Long fcreateTime) {
		this.fcreateTime = fcreateTime;
	}

}