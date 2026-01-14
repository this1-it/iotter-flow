package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

/*

create table iotter.measure_stats (
sn varchar, 
key varchar, 
lbl varchar, 
own varchar,
freq float, 
first timestamp, 
last timestamp, 
created timestamp, 
updated timestamp, 
since timestamp, 
running boolean, 
rec bigint, 
qual int,
PRIMARY KEY ((sn), key) ;

ALTER TABLE measure_stats ADD since timestamp;

*/

public class MeasureStats implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MeasureStats(String serial, String key) {
		this.serial = serial;
		this.key = key;
	}
	
	private String serial;
	private String key;
	private String label;
	private Date since;
	private Date created;
	private Date updated;
	private Date lastMeasureDate;
	private Date firstMeasureDate;
	private Float frequency;
	private boolean running;
	private String owner;
	private long records;
	private int qualifier;
	
	public boolean isValid() {
		return (key != null && !key.isEmpty()) && //
				(frequency != null && frequency > 0) && //
				(since != null) && //
				(lastMeasureDate != null) && //
				(firstMeasureDate != null);
	}
	
	public Float getFrequency() {
		return frequency;
	}
	public void setFrequency(Float frequency) {
		this.frequency = frequency;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public Date getLastMeasureDate() {
		return lastMeasureDate;
	}
	public void setLastMeasureDate(Date lastMeasureDate) {
		this.lastMeasureDate = lastMeasureDate;
	}
	public Date getFirstMeasureDate() {
		return firstMeasureDate;
	}
	public void setFirstMeasureDate(Date firstMeasureDate) {
		this.firstMeasureDate = firstMeasureDate;
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public long getRecords() {
		return records;
	}
	public void setRecords(long records) {
		this.records = records;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public int getQualifier() {
		return qualifier;
	}
	public void setQualifier(int qualifier) {
		this.qualifier = qualifier;
	}
	public Date getSince() {
		return since;
	}

	public void setSince(Date since) {
		this.since = since;
	}

}
