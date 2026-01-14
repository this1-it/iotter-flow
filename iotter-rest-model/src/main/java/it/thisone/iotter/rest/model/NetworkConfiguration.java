package it.thisone.iotter.rest.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

/*
 * Tupla contenente la definizione di una rete.
 * Questa chiave è valida per quegli strumento che sono composti da vari nodi 
 * e che dispongono della funzionalità “plug&play” 
 * che permette la registrazione automatica degli strumenti che compongono la rete, 
 * del nome della rete e la loro associazione alla rete
 */

@ApiModel(value="IotNetworkConfiguration", description="configures network of virtual devices")
public class NetworkConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("name")
	public String getName() {
		return name;
	}
	
	@JsonProperty("devices")
	public String[] getDevices() {
		return devices;
	}
	
	public NetworkConfiguration() {
		//
	}

	/*
	 * Nome della rete, mandatory
	 */
	@JsonIgnore
	private String name;

	/*
	 * Array contenente tutti i numeri seriali degli strumenti che compongono la rete
	 */
	@JsonIgnore
	private String[] devices;

	public void setName(@JsonProperty("name")String name) {
		this.name = name;
	}

	public void setDevices(@JsonProperty("devices")String[] devices) {
		this.devices = devices;
	}
	

}