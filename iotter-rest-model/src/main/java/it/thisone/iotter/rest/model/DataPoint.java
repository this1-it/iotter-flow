package it.thisone.iotter.rest.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="DataPoint",description="time-series data point")
public class DataPoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



    @JsonProperty("id")
    @ApiModelProperty(name="id", value="parameter id",readOnly=true)
	public String getId() {
		return id;
	}

    @JsonProperty("ts")
    @ApiModelProperty(name="ts", value="timestamp in seconds",readOnly=true)
	public Long getTs() {
		return ts;
	}

    @JsonProperty("v")
    @ApiModelProperty(name="v", value="parameter value",readOnly=true)
	public Float getValue() {
		return value;
	}

    @JsonProperty("err")
    @ApiModelProperty(name="err", value="error code, value is invalid",readOnly=true)
	public String getError() {
		return error;
	}

    @JsonProperty("label")
    @ApiModelProperty(name="label", value="parameter label",readOnly=true)
	public String getLabel() {
		return label;
	}

    @JsonProperty("unit")
    @ApiModelProperty(name="unit", value="parameter unit",readOnly=true)
	public String getUnit() {
		return unit;
	}

    @JsonProperty("qual")
    @ApiModelProperty(name="qual", value="parameter qualifier",readOnly=true)
	public Integer getQual() {
		return qual;
	}

    @JsonProperty("typ")
    @ApiModelProperty(name="typ", value="parameter type",readOnly=true)
	public String getTypeVar() {
		return typeVar;
	}
    
    @JsonProperty("scale")
    @ApiModelProperty(name="scale", value="parameter scale",readOnly=true)
	public Float getScale() {
		return scale;
	}
    
    @JsonProperty("offset")
    @ApiModelProperty(name="offset", value="parameter offset",readOnly=true)
	public Float getOffset() {
		return offset;
	}
	
	
	/*
	 * Identificativo del canale 
	 */
    @JsonIgnore
	private String id;
	
	/*
	 * Timestamp UTC dell’istante del valore di misura
	 *  (valore espresso in secondi)
	 */
    @JsonIgnore
	private Long ts;

	
	/*
	 * Valore float della misura per quel canale
	 */
    @JsonIgnore
	private Float value;

	/*
	 * Valore che indica se il dato è stao raccolto in condizione di errore. 
	 * Questa situazione si può avere in condizioni di manutenzione di apparato o situazione di rottura di sensori.  
	 * Un dato con questo valore presente e diverso da null è da considerarsi invalido
	 */
    @JsonIgnore
	private String error;	
	
	
	/*
	 * Stringa associata al parametro nella visualizzazione dei grafici. 
	 */
    @JsonIgnore
	private String label;

	/*
	 * unità di misura associata parametro nella visualizzazione dei grafici. 
	 */
    @JsonIgnore
	private String unit;

	/*
	 * Identificativo del qualificatore
	 */
    @JsonIgnore
	private Integer qual;

    @JsonIgnore
	private String typeVar;

    @JsonIgnore
	private Float scale;
    
    @JsonIgnore
	private Float offset;
    
    @JsonIgnore
	public void setId(String id) {
		this.id = id;
	}

    @JsonIgnore
	public void setTs(Long ts) {
		this.ts = ts;
	}

    @JsonIgnore
	public void setValue(Float value) {
		this.value = value;
	}

    @JsonIgnore
	public void setError(String error) {
		this.error = error;
	}

    @JsonIgnore
	public void setLabel(String label) {
		this.label = label;
	}

    @JsonIgnore
	public void setUnit(String unit) {
		this.unit = unit;
	}

    @JsonIgnore
	public void setQual(Integer qual) {
		this.qual = qual;
	}
    
    @JsonIgnore
	public void setTypeVar(String typeVar) {
		this.typeVar = typeVar;
	}
    
	@Override
	@JsonIgnore
	public String toString() {
		return String.format("%s %f %d", getId(), getValue(), getTs());
	}

	@JsonIgnore
	public void setScale(Float scale) {
		this.scale = scale;
	}
	
	@JsonIgnore
	public void setOffset(Float offset) {
		this.offset = offset;
	}
}
