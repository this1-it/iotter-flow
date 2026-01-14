package it.thisone.iotter.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="DataWrite",description="write datapoints")
public class DataWrite implements Serializable {
    @JsonProperty(value="api-key", required=true)
	public String getApi_key() {
		return api_key;
	}

	@JsonProperty(value="bat_lvl", required=true)
	public int getBattery_level() {
		return battery_level;
	}

	@JsonProperty("data")
	public List<DataWriteSet> getData() {
		return data;
	}
	
	
	/**
	 * 
	 */
	@JsonIgnore
	private static final long serialVersionUID = 1L;

	/*
	 * La chiave di accesso al device. 
	 * Sarà generata automaticamente dallo strumento con un algoritmo custom DelatOhm (una forma di MD5) 
	 * con seme il numero seriale dello strumento
	 */
	@JsonIgnore
	private String api_key;
	
	/*
	 * Livello della batteria per gli strumenti dotati di essa. 
	 * Il valore può andare da 0 (scarica) a 100 (piena carica)
	 */
	@JsonIgnore
	private int battery_level;
	
	/*
	 * Contiene i vari DataSet raggruppati per istante temporale (ts)
	 */
	@JsonIgnore
	private List<DataWriteSet> data;

	public DataWrite() {
		data = new ArrayList<DataWriteSet>();
		battery_level = -1;
	}



	@JsonIgnore
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	@JsonIgnore
	public void setBattery_level(int battery_level) {
		this.battery_level = battery_level;
	}

	@JsonIgnore
	public void setData(List<DataWriteSet> data) {
		this.data = data;
	}

}
