package it.thisone.iotter.rest.model;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="DeviceConfigurationSet",description="DeviceConfigurationSet")
public class DeviceConfigurationSet {

	@JsonProperty("conf_rev")
	public int getRevision() {
		return revision;
	}

	@JsonProperty("conf")
	public List<ConfigAttribute> getAttributes() {
		return attributes;
	}

	@JsonProperty("conf_ts")
	public long getRevisionTime() {
		return revisionTime;
	}

	@JsonProperty("serial")
	public String getSerial() {
		return serial;
	}

	@JsonProperty("conf_tz")
	public String getTz() {
		return tz;
	}

	
	public DeviceConfigurationSet() {
		revision = -1;
		attributes =  new ArrayList<ConfigAttribute>();
	}	

	@JsonIgnore
	private int revision;
	
	@JsonIgnore
	private List<ConfigAttribute> attributes;

	@JsonIgnore
	private long revisionTime;
	
	@JsonIgnore
	private String serial;

	@JsonIgnore
	private String tz;


	@JsonIgnore
	public void setRevision(int revision) {
		this.revision = revision;
	}

	@JsonIgnore
	public void setAttributes(List<ConfigAttribute> attributes) {
		this.attributes = attributes;
	}

	@JsonIgnore
	public void setRevisionTime(long revisionTime) {
		this.revisionTime = revisionTime;
	}

	@JsonIgnore
	public void setSerial(String serial) {
		this.serial = serial;
	}

	@JsonIgnore
	public void setTz(String tz) {
		this.tz = tz;
	}
	

}
