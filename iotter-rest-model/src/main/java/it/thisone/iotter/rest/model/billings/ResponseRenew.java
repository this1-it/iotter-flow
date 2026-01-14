package it.thisone.iotter.rest.model.billings;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "ResponseRenew", description = "response entity")
public class ResponseRenew {

	@JsonIgnore
	private Header header;
	@JsonIgnore
	private RenewPlant data;

	@JsonProperty("header")
	public Header getHeader() {
		return header;
	}


	public void setHeader(@JsonProperty("header") Header header) {
		this.header = header;
	}

	@JsonProperty("data")
	public RenewPlant getData() {
		return data;
	}


	public void setData(@JsonProperty("data") RenewPlant data) {
		this.data = data;
	}


}
