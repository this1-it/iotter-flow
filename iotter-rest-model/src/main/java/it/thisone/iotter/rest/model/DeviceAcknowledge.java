package it.thisone.iotter.rest.model;


import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotDeviceAck",description="response from device endpoints")
public class DeviceAcknowledge implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("ts")
	public long getTs() {
		return ts;
	}

	@JsonProperty("check_config")
	public String getCheckConfig() {
		return checkConfig;
	}
	
	@JsonIgnore
	private long ts;
	
	@JsonIgnore
	private String checkConfig;
	
	public DeviceAcknowledge() {
		this.ts = System.currentTimeMillis()/1000;
		this.checkConfig = "false";
	}
	
	public DeviceAcknowledge(long ts, boolean checkConfig) {
		this.ts = ts / 1000;
		if (checkConfig) {
			this.checkConfig = "true";
		}
		else {
			this.checkConfig = null;
		}
	}

	@JsonIgnore
	public void setTs(long ts) {
		this.ts = ts;
	}

	@JsonIgnore
	public void setCheckConfig(String checkConfig) {
		this.checkConfig = checkConfig;
	}
}
