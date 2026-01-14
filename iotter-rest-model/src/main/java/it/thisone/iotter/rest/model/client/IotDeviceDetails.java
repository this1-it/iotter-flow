package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
@ApiModel(value="IotDeviceDetails",description="device details")
public class IotDeviceDetails implements Serializable {

    private static final long serialVersionUID = -7495897652017488896L;
   
    @JsonProperty("lbl")
	public String getLabel() {
		return label;
	}
    

    @JsonProperty("model")
	public String getModel() {
 		return model;
 	}

    @JsonProperty("status")
 	public String getStatus() {
 		return status;
 	}

    @JsonProperty("lc")
 	public long getLastContact() {
 		return lastContact;
 	}

    @JsonProperty("lat")
	public double getLatitude() {
		return latitude;
	}

    @JsonProperty("long")
	public double getLongitude() {
		return longitude;
	}
    
    @JsonProperty("type")
	public String getType() {
		return type;
	}

    @JsonIgnore
    private String label;
   
    @JsonIgnore
    private String model;

    @JsonIgnore
    private String type;
    
    @JsonIgnore
    private String status;
    
    @JsonIgnore
    private long lastContact;
    
    @JsonIgnore
    private double latitude;
    
    @JsonIgnore
    private double longitude;


	public void setModel(@JsonProperty("model") String model) {
		this.model = model;
	}


	public void setType(@JsonProperty("type") String type) {
		this.type = type;
	}
    
    @JsonIgnore
	public void setStatus(@JsonProperty("status") String status) {
		this.status = status;
	}


	public void setLastContact(@JsonProperty("lc") long lastContact) {
		this.lastContact = lastContact;
	}

	public void setLatitude(@JsonProperty("lat") double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(@JsonProperty("long") double longitude) {
		this.longitude = longitude;
	}

	public void setLabel(@JsonProperty("lbl") String label) {
		this.label = label;
	}





}
