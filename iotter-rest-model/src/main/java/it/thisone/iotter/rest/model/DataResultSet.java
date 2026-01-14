package it.thisone.iotter.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "DataResultSet", description = "it defines a time-ordered set of data points")
public class DataResultSet implements Serializable {

	@JsonProperty("serial")
	@ApiModelProperty(name = "serial", value = "device identifier", readOnly = true)
	public String getSerial() {
		return serial;
	}
	
	@JsonProperty("from")
	@ApiModelProperty(name = "from", value = "interval start date in secs, UTC time zone", readOnly = true)
	public long getFrom() {
		return from;
	}

	@JsonProperty("to")
	@ApiModelProperty(name = "to", value = "interval end date in secs, UTC time zone", readOnly = true)
	public long getTo() {
		return to;
	}
	
	@JsonProperty("last_contact")
	@ApiModelProperty(name = "last_contact", value = "last contact from device, date in secs, UTC time zone", readOnly = true)
	public long getLastContact() {
		return lastContact;
	}	
	

	@JsonProperty("values")
	@ApiModelProperty(name = "values", value = "", readOnly = true)
	public List<DataPoint> getValues() {
		return values;
	}

	@JsonProperty("rows")
	@ApiModelProperty(name = "rows", value = "", readOnly = true)
	public List<DataRow> getRows() {
		return rows;
	}

	@JsonProperty("batch")
	@ApiModelProperty(name = "batch", value = "optional attribute to allow paginated retrieval", readOnly = true)
	public ServiceableRetrieval getBatch() {
		return batch;
	}

	/**
	 * 
	 */
	@JsonIgnore
	private static final long serialVersionUID = 1L;

	
	@JsonIgnore
	private String serial;
	
	/*
	 * Rappresenta la data-ora UTC di inizio del periodo temporale dei dati
	 * richiesti
	 */
	@JsonIgnore
	private long from;

	/*
	 * Rappresenta la data-ora UTC di fine del periodo temporale dei dati
	 * richiesti. Nel caso non sia presente si intende la data-ora di arrivo
	 * della richiesta
	 */
	@JsonIgnore
	private long to;

	@JsonIgnore
	private long lastContact;

	/*
	 * Contiene i vari DataPoint identificati dal numero del canale (id)
	 */
	@JsonIgnore
	private List<DataPoint> values;

	@JsonIgnore
	private List<DataRow> rows;
	
	@JsonIgnore
	private ServiceableRetrieval batch;


	public DataResultSet() {
		values = new ArrayList<DataPoint>();
		rows = new ArrayList<DataRow>();
		batch = new ServiceableRetrieval();
	}

	@JsonIgnore
	public void setFrom(long from) {
		this.from = from;
	}

	@JsonIgnore
	public void setTo(long to) {
		this.to = to;
	}

	@JsonIgnore
	public void setValues(List<DataPoint> values) {
		this.values = values;
	}

	@JsonIgnore
	public void setBatch(ServiceableRetrieval batch) {
		this.batch = batch;
	}

	@JsonIgnore
	public void setRows(List<DataRow> rows) {
		this.rows = rows;
	}
	
	@JsonIgnore
	public void setLastContact(long lastContact) {
		this.lastContact = lastContact;
	}

	@JsonIgnore
	public void setSerial(String serial) {
		this.serial = serial;
	}

}
