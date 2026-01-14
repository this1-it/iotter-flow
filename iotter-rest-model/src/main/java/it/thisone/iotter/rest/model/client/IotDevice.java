package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotDevice",description="device root entity")
public class IotDevice implements Serializable {

    private static final long serialVersionUID = -7495897652017488896L;

    @JsonProperty(value="sn", required=true)
 	public String getSerial() {
 		return serial;
 	}

    @JsonProperty(value="master")
	public String getId() {
		return master;
	}
    
    
    @JsonProperty(value="details")
	public IotDeviceDetails getDetails() {
		return details;
	}
  
    @JsonProperty("net")
	public IotNetwork getNetwork() {
		return network;
	}
    
    @JsonIgnore
    private String serial;
    
    @JsonIgnore
    private String master;

    @JsonIgnore
	private IotDeviceDetails details;

    @JsonIgnore
    private IotNetwork network;


	public void setNetwork(@JsonProperty("net") IotNetwork network) {
		this.network = network;
	}


    public void setMaster(@JsonProperty("master") String master) {
		this.master = master;
	}

    
    public void setSerial(@JsonProperty("sn") String serial) {
		this.serial = serial;
	}
    
	public void setDetails(@JsonProperty("details") IotDeviceDetails details) {
		this.details = details;
	}



}
