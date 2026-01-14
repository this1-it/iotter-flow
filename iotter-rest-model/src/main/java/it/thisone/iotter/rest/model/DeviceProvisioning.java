package it.thisone.iotter.rest.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "IotDeviceProvisioning", description = "configures modbus slave connected to a real device")
public class DeviceProvisioning {

	@JsonProperty("checksum")
	@ApiModelProperty(value = "checksum of profiles", readOnly = true)
	public String getChecksum() {
		return checksum;
	}

	@JsonProperty(value = "profiles", required = true)
	@ApiModelProperty(name = "profiles", value = "list of modbus slave configurations", required = true)
	public List<ModbusProvisioning> getProfiles() {
		return profiles;
	}

	public DeviceProvisioning() {
		super();
		profiles = new ArrayList<>();
	}

	@JsonIgnore
	private List<ModbusProvisioning> profiles;

	@JsonIgnore
	private String checksum;

	@JsonIgnore
	public void setProfiles(@JsonProperty("profiles") List<ModbusProvisioning> profiles) {
		this.profiles = profiles;
	}

	@JsonIgnore
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

}
