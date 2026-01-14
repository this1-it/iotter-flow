package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotTicket",description="tracking operation")
public class IotTicket implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
    @JsonProperty("id")
    @ApiModelProperty(value="operation id",readOnly=true)
	public String getId() {
		return id;
	}
    
    @JsonProperty("operation")
    @ApiModelProperty(value="operation name",readOnly=true, allowableValues="registration, activation, password_reset")
	public String getOperation() {
		return operation;
	}

    @JsonProperty("expires")
    @ApiModelProperty(value="operation expires in seconds",readOnly=true)
	public long getExpires() {
		return expires;
	}
	

	public void setId(@JsonProperty("id") String id) {
		this.id = id;
	}

	public void setOperation(@JsonProperty("operation") String operation) {
		this.operation = operation;
	}

	public void setExpires(@JsonProperty("expires") long expires) {
		this.expires = expires;
	}
   
    @JsonIgnore
    private String id;
    @JsonIgnore
    private String operation;
    @JsonIgnore
    private long expires;


    
}
