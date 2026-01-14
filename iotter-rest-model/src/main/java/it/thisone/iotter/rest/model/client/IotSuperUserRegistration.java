package it.thisone.iotter.rest.model.client;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotSuperUserRegistration", description="super user registration root entity")
public class IotSuperUserRegistration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3489559884790316558L;

    @JsonProperty(value="tenant",required=true)
 	public String getTenant() {
		return tenant;
	}

    @JsonProperty(value="user",required=true)
	public IotLogin getUser() {
		return user;
	}

    @JsonProperty(value="device",required=true)
	public IotDeviceId getDevice() {
		return device;
	}

	
 	public IotSuperUserRegistration() {
	}

    
    @JsonIgnore
	@NotNull(message = "Please provide a valid tenant")
	private String tenant;
	
    @JsonIgnore
    @NotNull(message = "Please provide a valid login")
	private IotLogin user;
	
    @JsonIgnore
    @NotNull(message = "Please provide a valid device")
	private IotDeviceId device;
    
    @JsonIgnore
	private IotUserDetails userDetails;
	
    @JsonIgnore
	private IotDeviceDetails deviceDetails;
    
	public void setTenant(@JsonProperty("tenant") String tenant) {
		this.tenant = tenant;
	}

	public void setUser(@JsonProperty("user") IotLogin user) {
		this.user = user;
	}

	public void setDevice(@JsonProperty("device") IotDeviceId device) {
		this.device = device;
	}

	@JsonProperty("user_details")
	public IotUserDetails getUserDetails() {
		return userDetails;
	}

	@JsonProperty("device_details")
	public IotDeviceDetails getDeviceDetails() {
		return deviceDetails;
	}

	public void setUserDetails(@JsonProperty("user_details") IotUserDetails userDetails) {
		this.userDetails = userDetails;
	}

	public void setDeviceDetails(@JsonProperty("device_details") IotDeviceDetails deviceDetails) {
		this.deviceDetails = deviceDetails;
	}


	

}
