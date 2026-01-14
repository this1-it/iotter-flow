package it.thisone.iotter.rest.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value="IotAttribute",description="changeable configuration attribute")
public class ConfigAttribute {
	@JsonProperty("min")
	public Float getMin() {
		return min;
	}
	@JsonProperty("section")
	public String getSection() {
		return section;
	}
	@JsonProperty("value")
	public Float getValue() {
		return value;
	}
	@JsonProperty("max")
	public Float getMax() {
		return max;
	}
	@JsonProperty("topic")
	public String getTopic() {
		return topic;
	}

	@JsonProperty("option")
	public String getOption() {
		return option;
	}
	
	@JsonProperty("format")
	public String getFormat() {
		return format;
	}
	
	@JsonProperty("perm")
	public String getPermission() {
		return permission;
	}

	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("sn")
	public String getSerial() {
		return serial;
	}
	
	@JsonProperty("oid")
	public String getOid() {
		return oid;
	}
	

	@JsonProperty("group")
	public String getGroup() {
		return group;
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
	private String oid;

	@JsonIgnore
	private String serial;
	
	@JsonIgnore
	private String id;

	@JsonIgnore
	private String section;

	@JsonIgnore
	private String group;

	@JsonIgnore
	private Float value;

	@JsonIgnore
	private Float min;

	@JsonIgnore
	private Float max;

	@JsonIgnore
	private String topic;

	@JsonIgnore
	private String label;

	@JsonIgnore
	private String permission;
	
	@JsonIgnore
	private String unit;

	@JsonIgnore
	private String option;

	@JsonIgnore
	private String format;
	

	public void setGroup(@JsonProperty("group") String group) {
		this.group = group;
	}
	

	public void setLabel(@JsonProperty("label") String label) {
		this.label = label;
	}


	public void setUnit(@JsonProperty("unit") String unit) {
		this.unit = unit;
	}
	

	public void setOption(@JsonProperty("option") String option) {
		this.option = option;
	}
	

	public void setFormat(@JsonProperty("format") String format) {
		this.format = format;
	}


	public void setPermission(@JsonProperty("permission") String permission) {
		this.permission = permission;
	}


	public void setId(@JsonProperty("id") String id) {
		this.id = id;
	}


	public void setSection(@JsonProperty("section") String section) {
		this.section = section;
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

	public void setOid(@JsonProperty("oid") String oid) {
		this.oid = oid;
	}

}
