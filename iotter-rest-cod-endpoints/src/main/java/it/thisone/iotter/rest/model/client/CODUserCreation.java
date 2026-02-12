package it.thisone.iotter.rest.model.client;

import java.io.Serializable;

import javax.validation.constraints.NotNull;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="CODUserCreation", description="cod user creation")
public class CODUserCreation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3489559884790316558L;


	@ApiModelProperty(value="user credentials",required=true)
    @JsonProperty(value="login",required=true)
	public IotLogin getLogin() {
		return login;
	}

	@JsonProperty("user_details")
	public IotUserDetails getUserDetails() {
		return userDetails;
	}
    
    
 	public CODUserCreation() {
	}
	
    @JsonIgnore
	@NotNull(message = "Please provide a valid login")
	private IotLogin login;
    
    @JsonIgnore
	private IotUserDetails userDetails;
	

	public void setLogin(@JsonProperty("login") IotLogin login) {
		this.login = login;
	}


	public void setUserDetails(@JsonProperty("user_details") IotUserDetails userDetails) {
		this.userDetails = userDetails;
	}



}
