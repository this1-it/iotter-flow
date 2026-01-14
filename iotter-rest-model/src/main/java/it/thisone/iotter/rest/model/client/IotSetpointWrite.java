package it.thisone.iotter.rest.model.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotSetpointWrite",description="Setpoint Write")
public class IotSetpointWrite {
	
	@JsonProperty("value")
    @ApiModelProperty(name="value", value="value to be written")
	public Float getValue() {
		return value;
	}

	@JsonProperty("id")
	@ApiModelProperty(name="id", value="parameter id")
	public String getId() {
		return id;
	}
	
	@JsonIgnore
	private String id;

	@JsonIgnore
	private Float value;
	
	public void setValue(@JsonProperty("value") Float value) {
		this.value = value;
	}

	public void setId(@JsonProperty("id") String id) {
		this.id = id;
	}

}
