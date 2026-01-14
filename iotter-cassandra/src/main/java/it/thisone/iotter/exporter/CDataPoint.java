package it.thisone.iotter.exporter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CDataPoint {

	@JsonProperty("id")
	private String id;

	@JsonProperty("value")
	private Float value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", value=" + value + "]";
	}
}