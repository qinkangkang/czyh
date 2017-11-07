package com.czyh.czyhweb.dto;

import java.io.Serializable;

import com.czyh.czyhweb.util.NullToEmptySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class OrderRecipientDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String recipient;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String phone;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String address;

	private int specPerson = 0;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String insuranceInfo;

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getSpecPerson() {
		return specPerson;
	}

	public void setSpecPerson(int specPerson) {
		this.specPerson = specPerson;
	}

	public String getInsuranceInfo() {
		return insuranceInfo;
	}

	public void setInsuranceInfo(String insuranceInfo) {
		this.insuranceInfo = insuranceInfo;
	}

}