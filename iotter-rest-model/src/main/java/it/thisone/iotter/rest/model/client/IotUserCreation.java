package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotUserCreation", description="user registration root entity")
public class IotUserCreation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3489559884790316558L;

	@ApiModelProperty(value="network to assign to the user",required=true)
    @JsonProperty(value="network",required=true)
	public IotNetwork getNetwork() {
		return network;
	}

	@ApiModelProperty(value="user credentials",required=true)
    @JsonProperty(value="user",required=true)
	public IotLogin getUser() {
		return user;
	}

	@JsonProperty("user_details")
	public IotUserDetails getUserDetails() {
		return userDetails;
	}
    
    
 	public IotUserCreation() {
	}
	
    @JsonIgnore
	private IotLogin user;
    
    @JsonIgnore
	private IotUserDetails userDetails;
	
    @JsonIgnore
    private IotNetwork network;


	public void setNetwork(@JsonProperty("network") IotNetwork network) {
		this.network = network;
	}

	public void setUser(@JsonProperty("user") IotLogin user) {
		this.user = user;
	}


	public void setUserDetails(@JsonProperty("user_details") IotUserDetails userDetails) {
		this.userDetails = userDetails;
	}



}
