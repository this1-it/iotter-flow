package it.thisone.iotter.rest.model.client;

import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import it.thisone.iotter.rest.model.ServiceableRetrieval;


@ApiModel(value="IotDeviceSet",description="device list")
public class IotDeviceSet {

	
	@JsonIgnore
	private List<IotDevice> values;

	@JsonProperty("values")
	public List<IotDevice> getValues() {
		return values;
	}

	@JsonIgnore
	public void setValues(List<IotDevice> values) {
		this.values = values;
	}
	
 	public IotDeviceSet() {
		values = new ArrayList<IotDevice>();
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
