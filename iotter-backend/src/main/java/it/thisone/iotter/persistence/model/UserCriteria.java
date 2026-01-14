package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import it.thisone.iotter.enums.AccountStatus;

public class UserCriteria implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserCriteria() {
	}

	
	private String owner;
	private AccountStatus status;
	private String username;
	private String email;
	private String lastName;
	private String role;
	private String group;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	
	
}
