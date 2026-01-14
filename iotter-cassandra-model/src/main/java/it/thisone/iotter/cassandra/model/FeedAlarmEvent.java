package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

/*

DROP TABLE iotter.feed_alarm_events;
CREATE TABLE iotter.feed_alarm_events (
  sn varchar,
  key varchar,
  ts timestamp,
  nty boolean,
  st varchar,
  val float,
  ths float,
  dt timestamp,
  PRIMARY KEY ((sn), key, ts)
);  
  sn varchar,key varchar,ts timestamp,nty boolean,st varchar,val float,ths float,dt timestamp,
*/

//Feature #382 Feed Alarms

public class FeedAlarmEvent implements Serializable, IFeedAlarm {
	public FeedAlarmEvent(String serial, String key, Date timestamp, Date created) {
		super();
		this.serial = serial;
		this.key = key;
		this.timestamp = timestamp;
		this.created = created;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String key;
	private String serial;
	private boolean notify;
	private String status;
	private Float value;
	private Float threshold;
	private Date timestamp;
	private Date created;
	private String operator;
	private String members;
	
	
	@Override
	public String toString() {
		return String.format("sn=%s, key=%s, created=%s, status=%s ,value=%f",
				serial, key, created, status, value);
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isNotify() {
		return notify;
	}
	public void setNotify(boolean notify) {
		this.notify = notify;
	}
	public Float getThreshold() {
		return threshold;
	}
	public void setThreshold(Float threshold) {
		this.threshold = threshold;
	}
	
	public Date getCreated() {
		return created;
	}



	public void setCreated(Date created) {
		this.created = created;
	}



	public String getOperator() {
		return operator;
	}



	public String getMembers() {
		return members;
	}



	public void setOperator(String operator) {
		this.operator = operator;
	}



	public void setMembers(String members) {
		this.members = members;
	}


}
