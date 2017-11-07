package com.czyh.czyhweb.dto;

import java.io.Serializable;

import com.czyh.czyhweb.util.NullToEmptySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class RecipientDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String recipient;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String phone;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String address;

	private int specPerson = 0;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String insuredA;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String idCardA;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String insuredB;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String idCardB;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String insuredC;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String idCardC;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String insuredD;

	@JsonSerialize(nullsUsing = NullToEmptySerializer.class)
	private String idCardD;

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

	public String getInsuredA() {
		return insuredA;
	}

	public void setInsuredA(String insuredA) {
		this.insuredA = insuredA;
	}

	public String getIdCardA() {
		return idCardA;
	}

	public void setIdCardA(String idCardA) {
		this.idCardA = idCardA;
	}

	public String getInsuredB() {
		return insuredB;
	}

	public void setInsuredB(String insuredB) {
		this.insuredB = insuredB;
	}

	public String getIdCardB() {
		return idCardB;
	}

	public void setIdCardB(String idCardB) {
		this.idCardB = idCardB;
	}

	public String getInsuredC() {
		return insuredC;
	}

	public void setInsuredC(String insuredC) {
		this.insuredC = insuredC;
	}

	public String getIdCardC() {
		return idCardC;
	}

	public void setIdCardC(String idCardC) {
		this.idCardC = idCardC;
	}

	public String getInsuredD() {
		return insuredD;
	}

	public void setInsuredD(String insuredD) {
		this.insuredD = insuredD;
	}

	public String getIdCardD() {
		return idCardD;
	}

	public void setIdCardD(String idCardD) {
		this.idCardD = idCardD;
	}

	public int getSpecPerson() {
		return specPerson;
	}

	public void setSpecPerson(int specPerson) {
		this.specPerson = specPerson;
	}

}