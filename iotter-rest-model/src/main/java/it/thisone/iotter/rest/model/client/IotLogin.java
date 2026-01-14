package it.thisone.iotter.rest.model.client;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotLogin", description="user credentials")
public class IotLogin implements Serializable {

    private static final long serialVersionUID = -7495897652017488896L;

    @ApiModelProperty(value="user identifier must be at least 8 chars length,  email",required=true)
    @JsonProperty(value="username", required=true)
	public String getUsername() {
		return username;
	}

    @ApiModelProperty(value="user password must be at least 8 chars length",required=true)
    @JsonProperty(value="password", required=true)
	public String getPassword() {
		return password;
	}
    
    @JsonIgnore
	@NotNull(message = "Please provide a valid username")
    private String username;
    @JsonIgnore
    @NotNull(message = "Please provide a valid password")
    private String password;
    
	public void setUsername(@JsonProperty("username") String username) {
		this.username = username;
	}

	public void setPassword(@JsonProperty("password") String password) {
		this.password = password;
	}
}
