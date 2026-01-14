package it.thisone.iotter.rest.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotDeviceProvisioningWrite",description="configures modbus slaves connected to a real device")
public class DeviceProvisioningWrite {

	@JsonProperty("api-key")
	public String getApiKey() {
		return apiKey;
	}

	@JsonProperty("profiles")
	public List<ModbusProvisioning> getProfiles() {
		if (profiles == null) {
			profiles = new ArrayList<>();
		}
		return profiles;
	}
	
	public DeviceProvisioningWrite() {
		super();
		profiles = new ArrayList<>();
	}

	@JsonIgnore
	private List<ModbusProvisioning> profiles;

	@JsonIgnore
	private String apiKey;
	
	@JsonIgnore
	public void setProfiles(List<ModbusProvisioning> profiles) {
		this.profiles = profiles;
	}

	@JsonIgnore
	public void setApiKey(String apikey) {
		this.apiKey = apikey;
	}

}
