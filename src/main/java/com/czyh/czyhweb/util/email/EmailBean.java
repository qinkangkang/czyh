package com.czyh.czyhweb.util.email;

import java.io.Serializable;
import java.util.Map;

public class EmailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int SCHEDULE_CONFIRM_EMAIL = 1;

	// public static final int FIND_PWD = 2;

	private int count = 0;

	private int emailType;

	private Map<String, Object> contentMap;

	private String toMail;

	private String subject;

	private long memberId;

	public long getMemberId() {
		return memberId;
	}

	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}

	public void addOne() {
		count++;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getEmailType() {
		return emailType;
	}

	public void setEmailType(int emailType) {
		this.emailType = emailType;
	}

	public Map<String, Object> getContentMap() {
		return contentMap;
	}

	public void setContentMap(Map<String, Object> contentMap) {
		this.contentMap = contentMap;
	}

	public String getToMail() {
		return toMail;
	}

	public void setToMail(String toMail) {
		this.toMail = toMail;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
