package it.thisone.iotter.rest.model.billings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="BillingDevice",description="device entity")
public class BillingDevice {
	
	@JsonProperty(value="serial")
	public String getSerial() {
		return serial;
	}
	
	@JsonProperty(value="owner")
	public String getOwner() {
		return owner;
	}
	
	@JsonProperty(value="activation_date")
	public long getActivation() {
		return activation;
	}

    @JsonIgnore
 	private String serial;
    @JsonIgnore
	private String owner;
    @JsonIgnore
	private long activation;
	
	
	public void setSerial(@JsonProperty(value="serial") String serial) {
		this.serial = serial;
	}
	public void setOwner(@JsonProperty(value="owner") String owner) {
		this.owner = owner;
	}
	public void setActivation(@JsonProperty(value="activation_date") long activation) {
		this.activation = activation;
	}

    @JsonIgnore
 	private String label;
    
	@JsonProperty(value="label")	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(@JsonProperty(value="label")String label) {
		this.label = label;
	}
}
