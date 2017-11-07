package com.czyh.czyhweb.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.czyh.czyhweb.log.LogLevel;

@Entity
@Table(name = "t_log_info")
public class TLogInfo extends IdEntity {

	private static final long serialVersionUID = 1L;

	// Fields
	private Date createTime;
	private String ipAddress;
	private LogLevel logLevel;
	private String message;
	private String username;
	private Long userId;
	private Integer module;

	// Constructors
	/** default constructor */
	public TLogInfo() {
	}

	/** minimal constructor */
	public TLogInfo(Long id) {
		this.id = id;
	}

	// Property accessors
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "ip_address", length = 64)
	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Column(name = "log_level", length = 16)
	@Enumerated(EnumType.STRING)
	public LogLevel getLogLevel() {
		return this.logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Column(name = "message")
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name = "username", length = 64)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "user_id")
	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "module")
	public Integer getModule() {
		return this.module;
	}

	public void setModule(Integer module) {
		this.module = module;
	}

}