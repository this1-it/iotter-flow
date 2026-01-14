package it.thisone.iotter.rest.model.client;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="IotSetpoint",description="Setpoint Parameter")
public class IotSetpoint {
	
    @JsonProperty("ts")
    @ApiModelProperty(name="ts", value="timestamp in seconds",readOnly=true)
	public long getTs() {
		return ts;
	}

	
	@JsonProperty("min")
    @ApiModelProperty(name="min", value="min allowed value",readOnly=true)
	public Float getMin() {
		return min;
	}
	
	@JsonProperty("value")
    @ApiModelProperty(name="value", value="current value",readOnly=true)
	public Float getValue() {
		return value;
	}
	
	@JsonProperty("max")
    @ApiModelProperty(name="max", value="max allowed value",readOnly=true)
	public Float getMax() {
		return max;
	}
	
	@JsonProperty("topic")
    @ApiModelProperty(name="topic", value="mqtt topic")
	public String getTopic() {
		return topic;
	}
	
	@JsonProperty("perm")
    @ApiModelProperty(name="perm", value="permission",readOnly=true)
	public String getPermission() {
		return permission;
	}

	@JsonProperty("id")
	@ApiModelProperty(name="id", value="parameter id",readOnly=true)
	public String getId() {
		return id;
	}

	@JsonProperty("sn")
	public String getSerial() {
		return serial;
	}

	@JsonProperty("label")
	public String getLabel() {
		return label;
	}
	
	@JsonProperty("unit")
	public String getUnit() {
		return unit;
	}

    @JsonIgnore
	private long ts;

	@JsonIgnore
	private String serial;
	
	@JsonIgnore
	private String id;

	@JsonIgnore
	private Float value;

	@JsonIgnore
	private Float min;

	@JsonIgnore
	private Float max;

	@JsonIgnore
	private String topic;

	@JsonIgnore
	private String permission;
	
	@JsonIgnore
	private String unit;

	@JsonIgnore
	private String label;
	

	public void setLabel(@JsonProperty("label") String label) {
		this.label = label;
	}

	public void setUnit(@JsonProperty("unit") String unit) {
		this.unit = unit;
	}

	public void setPermission(@JsonProperty("perm") String permission) {
		this.permission = permission;
	}

	public void setId(@JsonProperty("id") String id) {
		this.id = id;
	}

	public void setValue(@JsonProperty("value") Float value) {
		this.value = value;
	}

	public void setMin(@JsonProperty("min") Float min) {
		this.min = min;
	}

	public void setMax(@JsonProperty("max") Float max) {
		this.max = max;
	}

	public void setTopic(@JsonProperty("topic") String topic) {
		this.topic = topic;
	}

	public void setSerial(@JsonProperty("sn") String serialNumber) {
		this.serial = serialNumber;
	}


	public void setTs(long ts) {
		this.ts = ts;
	}

}
