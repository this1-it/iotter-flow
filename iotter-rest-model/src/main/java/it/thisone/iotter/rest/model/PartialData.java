package it.thisone.iotter.rest.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="PartialData",description="Configure Data Ticks")
public class PartialData {

	
	@JsonProperty("i")
    @ApiModelProperty(value="parameter ids to be ignored as data ticks ",readOnly=true)
	public String[] getIgnore() {
		return ignore;
	}

	public PartialData() {
		super();
		ignore = new String[] {};
	}

	
	@JsonIgnore
	private String[] ignore;


	public void setIgnore(@JsonProperty("i") String[] ignore) {
		this.ignore = ignore;
	}

/*
	@JsonIgnore
	private long timestamp;
	public void setTimestamp(@JsonProperty("ts") long timestamp) {
		this.timestamp = timestamp;
	}

	@JsonProperty("ts")
    @ApiModelProperty(value="timestamp of data ticks",readOnly=true)
	public long getTimestamp() {
		return timestamp;
	}
*/
	
}
