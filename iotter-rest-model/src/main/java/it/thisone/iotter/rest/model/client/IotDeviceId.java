package it.thisone.iotter.rest.model.client;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotDeviceId",description="device identification")
public class IotDeviceId implements Serializable {

    private static final long serialVersionUID = -7495897652017488896L;

    @ApiModelProperty(value="unique device identifier, it must be at least 12 chars long",required=true)
    @JsonProperty(value="sn", required=true)
 	public String getSerial() {
 		return serial;
 	}

    @ApiModelProperty(value="unique device activation key, it must be at least 12 chars long",required=true)
    @JsonProperty(value = "ak" , required=true)
	public String getActivationKey() {
		return activationKey;
	}
   
    
    @JsonIgnore
    @NotNull(message = "Please provide a valid serial")
    private String serial;
    @JsonIgnore
    @NotNull(message = "Please provide a valid activationKey")
    private String activationKey;

	public void setSerial(@JsonProperty("sn") String serial) {
		this.serial = serial;
	}

	public void setActivationKey(@JsonProperty("ak") String activationKey) {
		this.activationKey = activationKey;
	}



}
