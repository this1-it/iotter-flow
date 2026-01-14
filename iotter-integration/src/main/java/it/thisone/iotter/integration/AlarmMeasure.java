package it.thisone.iotter.integration;

import java.io.Serializable;

public class AlarmMeasure implements Serializable {
	private static final long serialVersionUID = 5248914189402562079L;
	public AlarmMeasure(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}
	private String label;
	private String value;
	
	public String getLabel() {
		return label;
	}
	public String getValue() {
		return value;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
