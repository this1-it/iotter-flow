package it.thisone.iotter.cassandra.model;

import java.io.Serializable;

/*
 
CREATE TABLE iotter.feed_alarm_thresholds (
  sn varchar,
  key varchar,
  armed boolean,
  ntf boolean,
  prt varchar,
  lowlow float,
  low float,
  high float,
  highhigh float,
  dlymins int,
  rptmins int,
  PRIMARY KEY ((sn), key)
);  

sn varchar,  key varchar,  armed boolean,  ntf boolean,  prt varchar,  lowlow float,  low float,  high float,  highhigh float,  dlymins int,  rptmins int,
*/

//Feature #382 Feed Alarms Thresholds

public class FeedAlarmThresholds implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeedAlarmThresholds(String serial, String key) {
		super();
		this.serial = serial;
		this.key = key;
	}
	private String serial;
	private String key;
	private boolean armed;
	private boolean notify;
	private String priority;
	private Float lowLow;
	private Float low;
	private Float high;
	private Float highHigh;
	private int delayMinutes;
	private int repeatMinutes;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public boolean isArmed() {
		return armed;
	}
	public void setArmed(boolean armed) {
		this.armed = armed;
	}
	public boolean isNotify() {
		return notify;
	}
	public void setNotify(boolean notify) {
		this.notify = notify;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public Float getLowLow() {
		return lowLow;
	}
	public void setLowLow(Float lowLow) {
		this.lowLow = lowLow;
	}
	public Float getLow() {
		return low;
	}
	public void setLow(Float low) {
		this.low = low;
	}
	public Float getHigh() {
		return high;
	}
	public void setHigh(Float high) {
		this.high = high;
	}
	public Float getHighHigh() {
		return highHigh;
	}
	public void setHighHigh(Float highHigh) {
		this.highHigh = highHigh;
	}

	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public int getDelayMinutes() {
		return delayMinutes;
	}
	public void setDelayMinutes(int delayMinutes) {
		this.delayMinutes = delayMinutes;
	}
	public int getRepeatMinutes() {
		return repeatMinutes;
	}
	public void setRepeatMinutes(int repeatMinutes) {
		this.repeatMinutes = repeatMinutes;
	}

}
