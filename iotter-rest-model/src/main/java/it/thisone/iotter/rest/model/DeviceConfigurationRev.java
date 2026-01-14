package it.thisone.iotter.rest.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="DeviceConfigurationRev",description="DeviceConfigurationRev")
public class DeviceConfigurationRev implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private int revision;

	
	public DeviceConfigurationRev() {
		revision = -1;
	}


	@JsonProperty("conf_rev")
	public int getRevision() {
		return revision;
	}

	@JsonIgnore
	public void setRevision(int revision) {
		this.revision = revision;
	}

}
