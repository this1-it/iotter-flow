package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

/*
CREATE TABLE iotter.feed_alarms (
  sn varchar,
  key varchar,
  active boolean,
  st varchar,
  val float,
  ts timestamp,
  PRIMARY KEY ((sn), key)
);
sn varchar,key varchar,active boolean,st varchar,val float, ts timestamp,

ALTER TABLE feed_alarms ADD upd timestamp;

*/

//Feature #382 Feed Alarms

public class FeedAlarm implements Serializable, IFeedAlarm {
	public FeedAlarm(String serial, String key) {
		super();
		this.serial = serial;
		this.key = key;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private String serial;
	private boolean active;
	private boolean delayed;
	private boolean repeated;
	private String status;
	private Float value;
	private Float threshold;
	private Date timestamp;
	private Date updated;
	
	@Override
	public String toString() {
		return String.format("sn=%s, key=%s, status=%s ,value=%f",
				serial, key, status, value);
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public String getSerial() {
		return serial;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	@Override
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public Float getValue() {
		return value;
	}
	
	public void setValue(Float value) {
		this.value = value;
	}
	
	@Override
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public Float getThreshold() {
		return threshold;
	}

	public void setThreshold(Float threshold) {
		this.threshold = threshold;
	}


	public boolean isDelayed() {
		return delayed;
	}


	public boolean isRepeated() {
		return repeated;
	}


	public void setDelayed(boolean delayed) {
		this.delayed = delayed;
	}


	public void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}


	public Date getUpdated() {
		return updated;
	}


	public void setUpdated(Date updated) {
		this.updated = updated;
	}

}
