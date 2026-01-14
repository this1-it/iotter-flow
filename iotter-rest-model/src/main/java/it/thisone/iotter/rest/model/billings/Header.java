package it.thisone.iotter.rest.model.billings;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Header", description = "header entity")
public class Header {
	
	@JsonProperty(value = "type")
	public String getType() {
		return type;
	}
	@JsonProperty(value = "output")
	public int getOutput() {
		return output;
	}
	@JsonProperty(value = "message")
	public String getMessage() {
		return message;
	}

	@JsonIgnore
	private String type;
	@JsonIgnore
	private int output;
	@JsonIgnore
	private String message;

	
	public void setType(@JsonProperty(value = "type") String type) {
		this.type = type;
	}

	public void setOutput(@JsonProperty(value = "output") int output) {
		this.output = output;
	}

	public void setMessage(@JsonProperty(value = "message") String message) {
		this.message = message;
	}

}
