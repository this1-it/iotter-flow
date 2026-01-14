package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

/*
 DROP TABLE configuration_registry;

CREATE TABLE configuration_registry (
sn varchar, 
own boolean, 
rev int,
id varchar,
sect varchar,
grp varchar,
lbl varchar,
perm varchar,
tpc varchar,
val float,
maxval float,
minval float,
ts timestamp,
PRIMARY KEY ((sn), own, id));

 */

public class ConfigurationRegistry implements Serializable {
	public ConfigurationRegistry() {
		super();

	}
	
	public ConfigurationRegistry(String serial, boolean owner, String id) {
		super();
		this.serial = serial;
		this.owner = owner;
		this.id = id;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String serial;
	private boolean owner;
	private Date timestamp;
	private int revision;
	private String id;
	private String section;
	private String group;
	private Float value;
	private Float min;
	private Float max;
	private String topic;
	private String label;
	private String permission;

	public boolean getOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public Float getMin() {
		return min;
	}

	public void setMin(Float min) {
		this.min = min;
	}

	public Float getMax() {
		return max;
	}

	public void setMax(Float max) {
		this.max = max;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
