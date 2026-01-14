package it.thisone.iotter.rest.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="DeviceStatus", description="Configure Data Ticks")
public class DeviceOnlineStatus {

	public DeviceOnlineStatus() {
		super();
	}

	@JsonIgnore
	private String serial;

	public void setSerial(@JsonProperty("sn") String value) {
		this.serial = value;
	}

	@JsonProperty("sn")
	@ApiModelProperty(value="Serial Id",readOnly=true)
	public String getSerial() {
		return serial;
	}

	@JsonIgnore
	private long lastContact;

	public void setLastContact(@JsonProperty("lc") long timestamp) {
		this.lastContact = timestamp;
	}

	@JsonProperty("lc")
    @ApiModelProperty(value="Last Contact timestamp",readOnly=true)
	public long getLastContact() {
		return lastContact;
	}
	
	
	@JsonIgnore
	private boolean online;

	public void setOnline(@JsonProperty("online") boolean value) {
		this.online = value;
	}

	
	@JsonProperty("online")
	public boolean isOnline() {
		return online;
	}

	
}
