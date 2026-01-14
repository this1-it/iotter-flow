package it.thisone.iotter.rest.model.client;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import it.thisone.iotter.rest.model.ServiceableRetrieval;


@ApiModel(value="IotNetworkSet", description="network list")

public class IotNetworkSet {

	
	@JsonIgnore
	private List<IotNetwork> values;

	@JsonProperty("values")
	public List<IotNetwork> getValues() {
		return values;
	}

	@JsonIgnore
	public void setValues(List<IotNetwork> values) {
		this.values = values;
	}
	
 	public IotNetworkSet() {
		values = new ArrayList<IotNetwork>();
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
