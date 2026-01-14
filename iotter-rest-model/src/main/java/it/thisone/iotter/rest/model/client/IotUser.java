package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotUser",description="user root entity")
public class IotUser implements Serializable {
    public IotUser() {
		super();
	}
	
	public IotUser(String username) {
		super();
		this.username = username;
	}

	private static final long serialVersionUID = -7495897652017488896L;
    
    @JsonProperty(value="username", required=true)
	public String getUsername() {
		return username;
	}

	@JsonProperty(value="details")
	public IotUserDetails getDetails() {
		return details;
	}

    @JsonIgnore
    private String username;
    
    @JsonIgnore
	private IotUserDetails details;

    public void setUsername(@JsonProperty("username") String username) {
		this.username = username;
	}

	public void setDetails(@JsonProperty("details") IotUserDetails details) {
		this.details = details;
	}
}
