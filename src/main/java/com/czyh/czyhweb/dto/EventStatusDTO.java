package com.czyh.czyhweb.dto;

import java.io.Serializable;

public class EventStatusDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;

	private String operate;

	private String name;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOperate() {
		return operate;
	}

	public void setOperate(String operate) {
		this.operate = operate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
