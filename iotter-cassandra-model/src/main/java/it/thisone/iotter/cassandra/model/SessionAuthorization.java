package it.thisone.iotter.cassandra.model;

import java.io.Serializable;


public class SessionAuthorization implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9204745039532516744L;

	private String token;

	private String grant;

	public String getToken() {
		return token;
	}

	public String getGrant() {
		return grant;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setGrant(String grant) {
		this.grant = grant;
	}

}
