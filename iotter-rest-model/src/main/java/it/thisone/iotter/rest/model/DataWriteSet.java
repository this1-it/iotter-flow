package it.thisone.iotter.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="DataWriteSet", description="data write")
public class DataWriteSet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@JsonProperty("ts")
	public long getTimestamp() {
		return timestamp;
	}

	@JsonProperty("step")
	public float getStep() {
		return step;
	}

	@JsonProperty("overwrite_tr")
	public long getOverwrite_tr() {
		return overwrite_tr;
	}

	@JsonProperty("values")
	public List<DeviceData> getValues() {
		return values;
	}
	
	public DataWriteSet() {
		values = new ArrayList<DeviceData>();
		step = -1f;
	}
	
	/**
	 * Timestamp UTC dell’istante del valore di misura
	 *  (valore espresso in secondi)
	 */
	@JsonIgnore
	private long timestamp;
	
	/**
	 * Nel caso in cui si dovessero comunicare misure il cui spaziamento temporale è minore di un secondo 
	 * questa chiave contiene la spaziatura temporale tra i valori contenuti nella chiave v.
	 * I valori verranno quindi salvati con un timestamp che segue la regola:
	 * il primo datapoint è temporalmente posizionato all'instante indicato dal timestamp (ts) del dataset, 
	 * il secondo all'istante ts - 1*step, il terzo timestamo - 2*step e così via
	 *  (valore espresso in secondi)
	 */
	@JsonIgnore
	private float step;

	@JsonIgnore
	private long overwrite_tr;

	/**
	 * Contiene i vari DataPoint identificati dal numero del canale (id)
	 */
	@JsonIgnore
	private List<DeviceData> values;



	@JsonIgnore
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@JsonIgnore
	public void setStep(float step) {
		this.step = step;
	}

	@JsonIgnore
	public void setOverwrite_tr(long overwrite_tr) {
		this.overwrite_tr = overwrite_tr;
	}

	@JsonIgnore
	public void setValues(List<DeviceData> values) {
		this.values = values;
	}


}
