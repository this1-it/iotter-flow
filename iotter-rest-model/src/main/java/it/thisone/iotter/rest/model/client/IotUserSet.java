package it.thisone.iotter.rest.model.client;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import it.thisone.iotter.rest.model.ServiceableRetrieval;


@ApiModel(value="IotUserSet",description="user list")
public class IotUserSet {

	
	@JsonIgnore
	private List<IotUser> values;

	@JsonProperty("values")
	public List<IotUser> getValues() {
		return values;
	}

	@JsonIgnore
	public void setValues(List<IotUser> values) {
		this.values = values;
	}
	
 	public IotUserSet() {
		values = new ArrayList<IotUser>();
	}


	@JsonIgnore
	private ServiceableRetrieval batch;

	@JsonProperty("batch")
 	public ServiceableRetrieval getBatch() {
		return batch;
	}

	@JsonIgnore
	public void setBatch(ServiceableRetrieval batch) {
		this.batch = batch;
	}

}
