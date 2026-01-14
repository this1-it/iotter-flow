package it.thisone.iotter.rest.model.billings;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="RenewDevice",description="device entity")
public class RenewDevice {

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

	@JsonProperty(value="last_renew_date")
	public long getLastRenew() {
		return lastRenew;
	}
	@JsonProperty(value="next_renew_date")
	public long getNextRenew() {
		return nextRenew;
	}
	
	@JsonProperty(value="block_date")
	public long getBlock() {
		return block;
	}	
	
	@JsonProperty(value="status")
	public String getStatus() {
		return status;
	}	
	
    @JsonIgnore
 	private String serial;
    
    @JsonIgnore
	private String owner;
    
    @JsonIgnore
	private long activation;
    
    @JsonIgnore
	private long lastRenew;
    
    @JsonIgnore
	private long nextRenew;

    @JsonIgnore
	private long block;
    
    @JsonIgnore
 	private String status;


	public void setSerial(@JsonProperty(value="serial") String serial) {
		this.serial = serial;
	}
	public void setOwner(@JsonProperty(value="owner") String owner) {
		this.owner = owner;
	}
	public void setActivation(@JsonProperty(value="activation_date") long activation) {
		this.activation = activation;
	}
	public void setLastRenew(@JsonProperty(value="last_renew_date") long lastRenew) {
		this.lastRenew = lastRenew;
	}
	public void setNextRenew(@JsonProperty(value="next_renew_date") long nextRenew) {
		this.nextRenew = nextRenew;
	}
	public void setStatus(@JsonProperty(value="status")String status) {
		this.status = status;
	}
	public void setBlock(@JsonProperty(value="block_date") long block) {
		this.block = block;
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
