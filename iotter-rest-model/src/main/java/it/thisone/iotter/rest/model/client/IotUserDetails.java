package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "IotUserDetails", description = "user details")
public class IotUserDetails implements Serializable {
	private static final long serialVersionUID = -7495897652017488896L;

	@JsonProperty("firstName")
	public String getFirstName() {
		return firstName;
	}

	@JsonProperty("lastName")
	public String getLastName() {
		return lastName;
	}

	@JsonProperty("phone")
	public String getPhone() {
		return phone;
	}

	@JsonProperty("country")
	public String getCountry() {
		return country;
	}
	
	@JsonProperty("email")
	public String getEmail() {
		return email;
	}

	
	@JsonIgnore
	private String email;
	@JsonIgnore
	private String firstName;
	@JsonIgnore
	private String lastName;
	@JsonIgnore
	private String phone;
	@JsonIgnore
	private String country;

	public void setFirstName(@JsonProperty("firstName") String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(@JsonProperty("lastName") String lastName) {
		this.lastName = lastName;
	}

	public void setPhone(@JsonProperty("phone") String phone) {
		this.phone = phone;
	}

	public void setCountry(@JsonProperty("country") String country) {
		this.country = country;
	}

	public void setEmail(@JsonProperty("email") String email) {
		this.email = email;
	}
}
