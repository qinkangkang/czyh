package com.czyh.czyhweb.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "t_carnival")
public class TCarnival extends UuidEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private String ftitle;
	private Date fstartTime;
	private Date fendTime;
	private Integer fdayNumber;
	private String fsceneStr;
	private String fwxQrCodeUrl;
	private String fwxMsg;
	private String fwxMsgImgUrl;
	private Long fimage;
	private Integer fchannel;
	private Integer flotteryNumber;
	private Integer fcredentialNumber;
	private BigDecimal fodds;
	private Long fcredentialClassId;
	private String frule;
	private Integer fstatus;

	// Constructors

	/** default constructor */
	public TCarnival() {
	}

	/** minimal constructor */
	public TCarnival(String id) {
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

	@Column(name = "fDayNumber")
	public Integer getFdayNumber() {
		return this.fdayNumber;
	}

	public void setFdayNumber(Integer fdayNumber) {
		this.fdayNumber = fdayNumber;
	}

	@Column(name = "fSceneStr")
	public String getFsceneStr() {
		return this.fsceneStr;
	}

	public void setFsceneStr(String fsceneStr) {
		this.fsceneStr = fsceneStr;
	}

	@Column(name = "fWxQrCodeUrl", length = 2048)
	public String getFwxQrCodeUrl() {
		return this.fwxQrCodeUrl;
	}

	public void setFwxQrCodeUrl(String fwxQrCodeUrl) {
		this.fwxQrCodeUrl = fwxQrCodeUrl;
	}

	@Column(name = "fWxMsg")
	public String getFwxMsg() {
		return this.fwxMsg;
	}

	public void setFwxMsg(String fwxMsg) {
		this.fwxMsg = fwxMsg;
	}

	@Column(name = "fWxMsgImgUrl", length = 2048)
	public String getFwxMsgImgUrl() {
		return this.fwxMsgImgUrl;
	}

	public void setFwxMsgImgUrl(String fwxMsgImgUrl) {
		this.fwxMsgImgUrl = fwxMsgImgUrl;
	}

	@Column(name = "fImage")
	public Long getFimage() {
		return this.fimage;
	}

	public void setFimage(Long fimage) {
		this.fimage = fimage;
	}

	@Column(name = "fChannel")
	public Integer getFchannel() {
		return this.fchannel;
	}

	public void setFchannel(Integer fchannel) {
		this.fchannel = fchannel;
	}

	@Column(name = "fLotteryNumber")
	public Integer getFlotteryNumber() {
		return this.flotteryNumber;
	}

	public void setFlotteryNumber(Integer flotteryNumber) {
		this.flotteryNumber = flotteryNumber;
	}

	@Column(name = "fCredentialNumber")
	public Integer getFcredentialNumber() {
		return this.fcredentialNumber;
	}

	public void setFcredentialNumber(Integer fcredentialNumber) {
		this.fcredentialNumber = fcredentialNumber;
	}

	@Column(name = "fOdds", precision = 8)
	public BigDecimal getFodds() {
		return this.fodds;
	}

	public void setFodds(BigDecimal fodds) {
		this.fodds = fodds;
	}

	@Column(name = "fCredentialClassId")
	public Long getFcredentialClassId() {
		return this.fcredentialClassId;
	}

	public void setFcredentialClassId(Long fcredentialClassId) {
		this.fcredentialClassId = fcredentialClassId;
	}

	@Column(name = "fRule")
	public String getFrule() {
		return this.frule;
	}

	public void setFrule(String frule) {
		this.frule = frule;
	}

	@Column(name = "fStatus")
	public Integer getFstatus() {
		return this.fstatus;
	}

	public void setFstatus(Integer fstatus) {
		this.fstatus = fstatus;
	}

}