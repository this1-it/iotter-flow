package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value="IotToken",description="keep session authorization")
public class IotToken implements Serializable {

    private static final long serialVersionUID = -7495897652017488896L;

    @JsonProperty("token")
    @ApiModelProperty(value="authorization api-key",readOnly=true)
	public String getToken() {
		return token;
	}

    @JsonProperty("expires")
    @ApiModelProperty(value="api-key expiry date in seconds",readOnly=true)
	public long getExpires() {
		return expires;
	}
    
    @JsonProperty("role")
    @ApiModelProperty(value="client authorization",readOnly=true, allowableValues="Administrator, Superuser, User")
	public String getRole() {
		return role;
	}


    @JsonIgnore
    private String token;
    @JsonIgnore
    private long expires;
    @JsonIgnore
    private String role;


	public void setToken(@JsonProperty("token") String token) {
		this.token = token;
	}

	public void setExpires(@JsonProperty("expires") long expires) {
		this.expires = expires;
	}
	
	public void setRole(@JsonProperty("role") String role) {
		this.role = role;
	}


}
