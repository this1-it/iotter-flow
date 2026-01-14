package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class SimCard implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimCard() {
		super();
	}

	public SimCard(String imei, String phoneNumber, String operator) {
		super();
		this.imei = imei;
		this.phoneNumber = phoneNumber;
		this.operator = operator;
	}

	@Column(name = "IMEI")
	private String imei;
	
	@Column(name = "PHONE")
	private String phoneNumber;

	@Column(name = "OPERATOR")
	private String operator;


	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	
	
}
