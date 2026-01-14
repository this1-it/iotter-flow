package it.thisone.iotter.rest.model.billings;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "BillingResponse", description = "response entity")
public class ResponseBilling {


	@JsonProperty("header")
	public Header getHeader() {
		return header;
	}

	@JsonProperty("data")
	public List<BillingDevice> getData() {
		return data;
	}

	public void setHeader(@JsonProperty("header") Header header) {
		this.header = header;
	}

	public void setData(@JsonProperty("data") List<BillingDevice> data) {
		this.data = data;
	}
	
	@JsonIgnore
	private Header header;
	@JsonIgnore
	private List<BillingDevice> data;

}
