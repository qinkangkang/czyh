package com.czyh.czyhweb.dto.system;

import java.io.Serializable;
import java.util.Date;

import com.czyh.czyhweb.util.NullToEmptySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class ObjectTimeStampDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private Integer fsubObject;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private Date fsubUpdateTime;
	
	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private Date fsubCreateTime;

	
	public Integer getFsubObject() {
		return fsubObject;
	}

	public void setFsubObject(Integer fsubObject) {
		this.fsubObject = fsubObject;
	}

	public Date getFsubUpdateTime() {
		return fsubUpdateTime;
	}

	public void setFsubUpdateTime(Date fsubUpdateTime) {
		this.fsubUpdateTime = fsubUpdateTime;
	}

	public Date getFsubCreateTime() {
		return fsubCreateTime;
	}

	public void setFsubCreateTime(Date fsubCreateTime) {
		this.fsubCreateTime = fsubCreateTime;
	}
	
	
}