package it.thisone.iotter.rest.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="DeviceData",description="DeviceData")
public class DeviceData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceData() {
		battery_level = -1;
	}

	@JsonProperty("p")
	public PartialData getPartial() {
		return partial;
	}

	@JsonProperty("sn")
	public String getSerial() {
		return serial;
	}

	@JsonProperty("ids")
	public String[] getIds() {
		return ids;
	}

	@JsonProperty("v")
	public float[] getValue() {
		return value;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("blob")
	public String getBlob() {
		return blob;
	}

	@JsonProperty("err")
	public String[] getError() {
		return error;
	}

	@JsonProperty("batt_lvl")
	public int getBattery_level() {
		return battery_level;
	}
	
	
	/*
	 * Nel caso di richiesta cumulativa di strumenti composta da nodi è 
	 * il seriale del nodo al quale il parametro appartiene
	 */
	@JsonIgnore
	private String serial;
	
	/*
	 * Array che contiene il vari identificativi dei canali il cui valore è contenuto 
	 * nel array corrispondente alla chiave v e alla chiave err
	 */
	@JsonIgnore
	private String[] ids;

	/*
	 * Valore float della misura per quel parametro
	 */
	@JsonIgnore
	private float[] value;
	
	
	/*
	 * Identificativo del parametro nel caso di parametro singolo e 
	 * valore contenuto nella chiave blob o come array di valori con step contentui in v
	 */
	@JsonIgnore
	private String id;
	
	
	/*
	 * Stringa che rappresenta la codifica base64 di una bitmap. 
	 * Può essere utilizzato per trasportare dati di tipo stream (audio, video, etc.)
	 */
	@JsonIgnore
	private String blob;
	
	
	/*
	 * Valore che indica se il dato è stao raccolto in condizione di errore. 
	 * Questa situazione si può avere in condizioni di manutenzione di apparato o situazione di rottura di sensori.  
	 * Un dato con questo valore presente e diverso da zero è da considerarsi invalido
	 */
	@JsonIgnore
	private String[] error;
	
	/*
	 * 
	 */
	@JsonIgnore
	private int battery_level;

	@JsonIgnore
	private PartialData partial;

	@JsonIgnore
	public void setSerial(String serial) {
		this.serial = serial;
	}

	@JsonIgnore
	public void setIds(String[] ids) {
		this.ids = ids;
	}

	@JsonIgnore
	public void setValue(float[] value) {
		this.value = value;
	}

	@JsonIgnore
	public void setId(String id) {
		this.id = id;
	}

	@JsonIgnore
	public void setBlob(String blob) {
		this.blob = blob;
	}

	@JsonIgnore
	public void setError(String[] error) {
		this.error = error;
	}

	@JsonIgnore
	public void setBattery_level(int battery_level) {
		this.battery_level = battery_level;
	}


	@JsonIgnore
	public void setPartial(PartialData partial) {
		this.partial = partial;
	}

}
