package com.czyh.czyhweb.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Table;

/**
 * TWechatPush entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "t_wechat_push")
public class TWechatPush extends UuidEntity {

	private static final long serialVersionUID = 1L;

	private String ftitle;
	private String fbeginTitle;
	private String fendTitle;
	private String femailTitle;
	private String fsender;
	private String furl;
	private String fsendList;
	private Integer fplanNum;
	private Integer fdeliveryNum;
	private Date fstartTime;
	private Date fendTime;
	private Integer fstatus;
	private Long foperator;
	private Date fcreateTime;

	public TWechatPush() {
	}

	public TWechatPush(String id) {
		this.id = id;
	}

	@Column(name = "fTitle")
	public String getFtitle() {
		return this.ftitle;
	}

	public void setFtitle(String ftitle) {
		this.ftitle = ftitle;
	}

	@Column(name = "fBeginTitle")
	public String getFbeginTitle() {
		return this.fbeginTitle;
	}

	public void setFbeginTitle(String fbeginTitle) {
		this.fbeginTitle = fbeginTitle;
	}

	@Column(name = "fEndTitle")
	public String getFendTitle() {
		return this.fendTitle;
	}

	public void setFendTitle(String fendTitle) {
		this.fendTitle = fendTitle;
	}

	@Column(name = "fEmailTitle")
	public String getFemailTitle() {
		return this.femailTitle;
	}

	public void setFemailTitle(String femailTitle) {
		this.femailTitle = femailTitle;
	}

	@Column(name = "fSender")
	public String getFsender() {
		return this.fsender;
	}

	public void setFsender(String fsender) {
		this.fsender = fsender;
	}

	@Column(name = "fUrl")
	public String getFurl() {
		return this.furl;
	}

	public void setFurl(String furl) {
		this.furl = furl;
	}

	@Column(name = "fSendList")
	public String getFsendList() {
		return this.fsendList;
	}

	public void setFsendList(String fsendList) {
		this.fsendList = fsendList;
	}

	@Column(name = "fPlanNum")
	public Integer getFplanNum() {
		return this.fplanNum;
	}

	public void setFplanNum(Integer fplanNum) {
		this.fplanNum = fplanNum;
	}

	@Column(name = "fDeliveryNum")
	public Integer getFdeliveryNum() {
		return this.fdeliveryNum;
	}

	public void setFdeliveryNum(Integer fdeliveryNum) {
		this.fdeliveryNum = fdeliveryNum;
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

	@Column(name = "fStatus")
	public Integer getFstatus() {
		return this.fstatus;
	}

	public void setFstatus(Integer fstatus) {
		this.fstatus = fstatus;
	}

	@Column(name = "fOperator")
	public Long getFoperator() {
		return this.foperator;
	}

	public void setFoperator(Long foperator) {
		this.foperator = foperator;
	}

	@Column(name = "fCreateTime", length = 19)
	public Date getFcreateTime() {
		return this.fcreateTime;
	}

	public void setFcreateTime(Date fcreateTime) {
		this.fcreateTime = fcreateTime;
	}

}