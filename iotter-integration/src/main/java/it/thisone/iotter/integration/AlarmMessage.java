package it.thisone.iotter.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlarmMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5248914189402562079L;
	
	private String network;
	private String device;
	private String status;
	private String label;
	private String value;
	private String threshold;
	private String timestamp;
	private String created;
	private String serial;
	private String operator;
	private String members;
	private String message;
	private List<AlarmMeasure> measures;

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (device != null) {
			sb.append(device);
		}
		if (label != null) {
			sb.append(String.format(", [%s] %s %s ", status, label, value));
		}
		if (message != null) {
			sb.append(String.format(", [%s] %s ", status, message));
		}
		if (operator != null) {
			sb.append(String.format(", operator: %s ", operator));
		}
		if (members != null) {
			sb.append(String.format(", members: %s ", members));
		}
		if (created != null) {
			sb.append(String.format(", created: %s ", created));
		}
		return sb.toString();
	}
	
	public String getNetwork() {
		return network;
	}

	public String getDevice() {
		return device;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getThreshold() {
		return threshold;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public String getSerial() {
		return serial;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public String getMembers() {
		return members;
	}
	
	public void setNetwork(String network) {
		this.network = network;
	}
	
	public void setDevice(String device) {
		this.device = device;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public void setMembers(String members) {
		this.members = members;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<AlarmMeasure> getMeasures() {
		return measures;
	}

	public void addMeasure(String label, String value) {
		if (measures==null) measures = new ArrayList<>();
		measures.add(new AlarmMeasure(label, value));
	}
	

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

}
