package it.thisone.iotter.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;




@ApiModel(value="IotDeviceConfiguration",description="configures parameters of a real device")
public class DeviceConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty(value="api-key",required=true)
	@ApiModelProperty(value="write api-key", required=true)
	public String getApi_key() {
		return api_key;
	}

	@JsonProperty("fw_rel")
	public String getFirmware_release() {
		return firmware_release;
	}

	@JsonProperty("app_rel")
	public String getApp_release() {
		return app_release;
	}

	@JsonProperty("ts")
	public long getDatetime() {
		return datetime;
	}

	@JsonProperty("sample_period")
	public long getSample_period() {
		return sample_period;
	}

	@JsonProperty("params")
	public List<ParamConfiguration> getParams() {
		return params;
	}

	@JsonProperty("network")
	public NetworkConfiguration getNetwork() {
		return network;
	}

	@JsonProperty("visualization_id")
	public String getVisualization_id() {
		return visualization_id;
	}

	@JsonProperty("partial")
	public boolean isPartial() {
		return partial;
	}

	@JsonProperty("conf_rev")
	public int getRevision() {
		return revision;
	}

	@JsonProperty("conf_ts")
	public long getRevisionTime() {
		return revisionTime;
	}

	@JsonProperty("conf")
	public List<ConfigAttribute> getAttributes() {
		return attributes;
	}

	
	public DeviceConfiguration() {
		datetime = -1;
		sample_period = -1;
		revision = -1;
		attributes =  new ArrayList<ConfigAttribute>();
		params = new ArrayList<ParamConfiguration>();
	}
	
	/**
	 * La chiave di accesso al device. 
	 * Sarà generata automaticamente dallo strumento con un algoritmo custom
	 * (una forma di MD5) con seme il numero seriale dello strumento
	 */
	@JsonIgnore
	private String api_key;
	
	/*
	 * Versione del firware a bordo dello strumento
	 */
	@JsonIgnore
	private String firmware_release;

	/*
	 * Versione della eventaule applicazione a bordo dello strumento
	 */
	@JsonIgnore
	private String app_release;

	/*
	 * Valore UTC di validità della configurazione in secondi
	 */
	@JsonIgnore
	private long datetime;

	/*
	 * Valore in secondi del tempo di campionamento
	 */
	@JsonIgnore
	private long sample_period;

	/*
	 * Elenco dei canali/parametri di misura che fornisce lo strumento
	 */
	@JsonIgnore
	private List<ParamConfiguration> params;

	/**
	 * MANDATORY = FALSE
	 * Tupla contenente la definizione di una rete.
	 * Questa chiave è valida per quegli strumento che sono composti da vari nodi 
	 * e che dispongono della funzionalità “plug&play” 
	 * che permette la registrazione automatica degli strumenti che compongono la rete, 
	 * del nome della rete e la loro associazione alla rete
	 */
	@JsonIgnore
	private NetworkConfiguration network;
	
	/**
	 * FTP D32 importer must put an extra-key to identifies visualization name
	 */
	@JsonIgnore
	private String visualization_id;

	@JsonIgnore
	private boolean partial;
	
	@JsonIgnore
	private int revision;
	
	@JsonIgnore
	private long revisionTime;
	
	@JsonIgnore
	private List<ConfigAttribute> attributes;

	public void setApi_key(@JsonProperty(value="api-key") String api_key) {
		this.api_key = api_key;
	}


	public void setFirmware_release(@JsonProperty("fw_rel") String firmware_release) {
		this.firmware_release = firmware_release;
	}


	public void setApp_release(@JsonProperty("app_rel") String app_release) {
		this.app_release = app_release;
	}


	public void setDatetime(@JsonProperty("ts") long datetime) {
		this.datetime = datetime;
	}

	public void setSample_period(@JsonProperty("sample_period") long sample_period) {
		this.sample_period = sample_period;
	}

	public void setParams(@JsonProperty("params") List<ParamConfiguration> params) {
		this.params = params;
	}

	public void setNetwork(@JsonProperty("network") NetworkConfiguration network) {
		this.network = network;
	}

	public void setVisualization_id(@JsonProperty("visualization_id") String visualization_id) {
		this.visualization_id = visualization_id;
	}

	public void setPartial(@JsonProperty("partial") boolean partial) {
		this.partial = partial;
	}

	public void setRevision(@JsonProperty("conf_rev") int revision) {
		this.revision = revision;
	}

	public void setRevisionTime(@JsonProperty("conf_ts") long revisionTime) {
		this.revisionTime = revisionTime;
	}

	public void setAttributes(@JsonProperty("conf") List<ConfigAttribute> attributes) {
		this.attributes = attributes;
	}



}
