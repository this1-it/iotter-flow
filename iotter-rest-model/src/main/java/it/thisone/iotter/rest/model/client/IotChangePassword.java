package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotChangePassword",description="user change password")
public class IotChangePassword implements Serializable {

    private static final long serialVersionUID = -7495897652017488896L;

    @ApiModelProperty(value="user identifier",required=true)
    @JsonProperty(value="username", required=true)
	public String getUsername() {
		return username;
	}

    @ApiModelProperty(value="new password must be at least 8 chars length",required=true)
    @JsonProperty(value="password", required=true)
	public String getPassword() {
		return password;
	}


    @ApiModelProperty(value="autorization token",required=true)
    @JsonProperty(value="token", required=true)
	public String getToken() {
		return token;
	}
    
    @JsonIgnore
    private String username;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String token;
    
	public void setUsername(@JsonProperty("username") String username) {
		this.username = username;
	}

	public void setPassword(@JsonProperty("password") String password) {
		this.password = password;
	}

	public void setToken(@JsonProperty("token") String token) {
		this.token = token;
	}
}
