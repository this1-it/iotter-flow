package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class ChannelRemoteControl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6200484510236064709L;

	@Column(name = "CTRL_MIN")
	private Float min;
	@Column(name = "CTRL_MAX")
	private Float max;
	@Column(name = "CTRL_TOPIC")
	private String topic;
	@Column(name = "CTRL_VALUE")
	private Double value;
	@Column(name = "CTRL_PERMISSION")
	private String permission;
	@Column(name = "CTRL_REVISION")
	private int revision;

	
	@Override
	public String toString() {
		return String.format("min=%f ,max=%f, value=%f, topic=%s, permission=%s", min, max, value,  topic, permission);
	}
	
	@Transient
	public boolean isValid() {
		return (topic != null);
	}
	
	public Float getMin() {
		return min;
	}
	public Float getMax() {
		return max;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public void setMin(Float min) {
		this.min = min;
	}
	
	public void setMax(Float max) {
		this.max = max;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}


}
