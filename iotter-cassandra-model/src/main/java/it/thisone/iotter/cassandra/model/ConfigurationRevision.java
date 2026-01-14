package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

public class ConfigurationRevision implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigurationRevision(Date date, int revision) {
		super();
		this.date = date;
		this.revision = revision;
	}
	
	private Date date;
	private int revision;
	
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
	
}
