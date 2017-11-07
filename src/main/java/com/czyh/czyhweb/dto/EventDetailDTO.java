package com.czyh.czyhweb.dto;

import java.io.Serializable;

public class EventDetailDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String imageUrl;

	private String title;

	private String[] content;

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String[] getContent() {
		return content;
	}

	public void setContent(String[] content) {
		this.content = content;
	}

}