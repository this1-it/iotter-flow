package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotNetworkDetails", description="network details")
public class IotNetworkDetails implements Serializable {
    private static final long serialVersionUID = -7495897652017488896L;
    
    
    @JsonProperty("tz")
	public String getTimeZone() {
		return timeZone;
	}

    @JsonIgnore
    private String timeZone;

    @JsonIgnore
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

}
