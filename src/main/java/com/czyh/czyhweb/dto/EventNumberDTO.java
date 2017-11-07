package com.czyh.czyhweb.dto;

import java.io.Serializable;

public class EventNumberDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long onSaleCount;

	private Long offSaleCount;

	private Long count;

	private String title;

	public Long getOnSaleCount() {
		return onSaleCount;
	}

	public void setOnSaleCount(Long onSaleCount) {
		this.onSaleCount = onSaleCount;
	}

	public Long getOffSaleCount() {
		return offSaleCount;
	}

	public void setOffSaleCount(Long offSaleCount) {
		this.offSaleCount = offSaleCount;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
