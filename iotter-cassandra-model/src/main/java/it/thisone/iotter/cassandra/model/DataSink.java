package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.thisone.iotter.enums.DeviceStatus;



/*

CREATE TABLE datasinks (
  sn varchar,
  msn varchar,
  lbl varchar,
  own varchar,
  wak varchar,
  st varchar,
  tz varchar,
  prt varchar,
  bl int,
  im int,
  al boolean,
  lc timestamp,
  lr timestamp,
  rec bigint,

  PRIMARY KEY (sn)
);  

ALTER TABLE datasinks ADD msn varchar;
ALTER TABLE datasinks ADD lr timestamp;
ALTER TABLE datasinks ADD rec bigint;


ALTER TABLE datasinks ADD cks varchar;
ALTER TABLE datasinks ADD pub boolean;
ALTER TABLE datasinks ADD trc boolean;
ALTER TABLE datasinks ADD aca boolean;


*/



public class DataSink implements Serializable {

	public DataSink(String serial) {
		super();
		this.serial = serial;
		this.activeAlarms = false;
	}

	public boolean notActive() {
		if (getStatus() == null)
			return true;
		return (getStatus().equals(DeviceStatus.PRODUCED.name()) || getStatus().equals(DeviceStatus.DEACTIVATED.name()));
	}	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String serial;

	private String master;

	private String label;
	
	private String owner;

	private String writeApiKey;
	
	private String readApiKey;

	private String status;

	private String timeZone;
	
	private String protocol;

	private int batteryLevel;
	
	private boolean alarmed;

	private boolean publishing;

	private boolean tracing;

	private int inactivityMinutes;

	private Date lastContact;
	
	private Date lastRollup;
	
	private long records;
	
	private String checkSum;	

	private Boolean activeAlarms;


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getWriteApiKey() {
		return writeApiKey;
	}

	public void setWriteApiKey(String apiKey) {
		this.writeApiKey = apiKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public String getReadApiKey() {
		return readApiKey;
	}

	public void setReadApiKey(String readApiKey) {
		this.readApiKey = readApiKey;
	}

	public boolean isAlarmed() {
		return alarmed;
	}

	public void setAlarmed(boolean alarmed) {
		this.alarmed = alarmed;
	}

	public int getInactivityMinutes() {
		return inactivityMinutes;
	}

	public void setInactivityMinutes(int inactivityMinutes) {
		this.inactivityMinutes = inactivityMinutes;
	}

	public Date getLastContact() {
		return lastContact;
	}

	public void setLastContact(Date lastContact) {
		this.lastContact = lastContact;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Date getLastRollup() {
		return lastRollup;
	}

	public long getRecords() {
		return records;
	}

	public void setLastRollup(Date lastRollup) {
		this.lastRollup = lastRollup;
	}

	public void setRecords(long records) {
		this.records = records;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}


	public boolean isPublishing() {
		return publishing;
	}

	public void setPublishing(boolean publishing) {
		this.publishing = publishing;
	}

	public boolean isTracing() {
		return tracing;
	}

	public void setTracing(boolean tracing) {
		this.tracing = tracing;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	public Boolean hasActiveAlarms() {
		return activeAlarms;
	}

	public void setActiveAlarms(Boolean activeAlarms) {
		this.activeAlarms = activeAlarms;
	}

	@Override
	public String toString() {
		return String.format("DataSink [serial=%s, owner=%s, status=%s, lastContact=%s, alarmed=%s, activeAlarms=%s]",
				serial, owner, status, lastContact, alarmed, activeAlarms);
	}


}
