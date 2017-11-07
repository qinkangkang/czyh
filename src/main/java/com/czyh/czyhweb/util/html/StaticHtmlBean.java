package com.czyh.czyhweb.util.html;

import java.io.Serializable;
import java.util.Map;

public class StaticHtmlBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String objectId;

	private String relativePath;

	private String templateName;

	private Map<String, Object> contentMap;

	private int counter = 0;

	public void addCounterOne() {
		counter++;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public Map<String, Object> getContentMap() {
		return contentMap;
	}

	public void setContentMap(Map<String, Object> contentMap) {
		this.contentMap = contentMap;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

}