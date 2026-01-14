package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

public class SessionAuthentication implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9204745039532516744L;

	private String username;

	private String token;

	private Date expiryDate;

	private String role;

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public String getRole() {
		return role;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isExpired() {
		return (getExpiryDate() != null) ? getExpiryDate().before(new Date()): false;
	}

}
