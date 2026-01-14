package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

@Deprecated
public class ConfigurationRaw implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String key;
	private boolean owner;
	private Date date;
	private int revision;
	private String payload;
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public boolean getOwner() {
		return owner;
	}
	
	public void setOwner(boolean owner) {
		this.owner = owner;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public int getRevision() {
		return revision;
	}
	
	public void setRevision(int revision) {
		this.revision = revision;
	}


	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}

}
