package it.thisone.iotter.rest.model.client;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotNetwork", description="network root entity")
public class IotNetwork implements Serializable {
	
	public IotNetwork() {
		super();
	}
	
	public IotNetwork(String name, String tenant) {
		super();
		this.name = name;
		this.tenant = tenant;
	}

	private static final long serialVersionUID = -7495897652017488896L;
    
    @ApiModelProperty(value="network name",required=true)
    @JsonProperty(value="name", required=true)
	public String getName() {
		return name;
	}

    @ApiModelProperty(value="administrator userid",required=true)
    @JsonProperty(value="tenant", required=true)
	public String getTenant() {
		return tenant;
	}    
    
    @JsonProperty("id")
    @ApiModelProperty(name="id", value="id should not be set",readOnly=true)
	public String getId() {
		return id;
	}

    @JsonIgnore
    private IotNetworkDetails details;
  
    @JsonIgnore
    private String name;

    @JsonIgnore
    private String id;
    
    @JsonIgnore
    private String tenant;    
    
    
    public void setName(@JsonProperty("name") String name) {
		this.name = name;
	}

    @JsonIgnore
	public void setId(String id) {
		this.id = id;
	}


    @JsonProperty("details")
	public IotNetworkDetails getDetails() {
		return details;
	}

	public void setDetails(@JsonProperty("details") IotNetworkDetails details) {
		this.details = details;
	}
	


	public void setTenant(@JsonProperty("tenant")  String tenant) {
		this.tenant = tenant;
	}

    
}
