package it.thisone.iotter.rest.model.billings;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Token", description = "header entity")
public class RenewToken {

	@JsonProperty(value = "token")
	public String getToken() {
		return token;
	}



	@JsonProperty(value = "url")
	public String getUrl() {
		return url;
	}

	@JsonIgnore
	private String token;

	@JsonIgnore
	private String url;

	
	public void setToken(@JsonProperty(value = "token") String token) {
		this.token = token;
	}


	public void setUrl(@JsonProperty(value = "url") String message) {
		this.url = message;
	}
	
	
}
