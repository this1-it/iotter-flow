package it.thisone.iotter.rest.model.billings;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "ResponseRenew", description = "response entity")
public class ResponseToken {

	@JsonIgnore
	private Header header;
	@JsonIgnore
	private RenewToken data;

	@JsonProperty("header")
	public Header getHeader() {
		return header;
	}

	@JsonProperty("data")
	public RenewToken getData() {
		return data;
	}


	public void setHeader(@JsonProperty("header") Header header) {
		this.header = header;
	}


	public void setData(@JsonProperty("token") RenewToken data) {
		this.data = data;
	}



}
