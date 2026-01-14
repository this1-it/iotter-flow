package it.thisone.iotter.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="DataRow",description="")
public class DataRow implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5568385077107339174L;
	
    @JsonProperty("ts")
    @ApiModelProperty(name="ts", value="",readOnly=true)
    public long getTimestamp() {
		return timestamp;
	}

    @JsonProperty("v")
    @ApiModelProperty(name="v", value="",readOnly=true)	
    public List<Float> getValues() {
		return values;
	}
	
    @JsonIgnore
	private long timestamp;

    @JsonIgnore
	private List<Float> values;
	
	
    @JsonIgnore
	public DataRow() {
		super();
		values = new ArrayList<Float>();
	}

    @JsonIgnore
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

    @JsonIgnore
	public void setValues(List<Float> values) {
		this.values = values;
	}
	
	

}
