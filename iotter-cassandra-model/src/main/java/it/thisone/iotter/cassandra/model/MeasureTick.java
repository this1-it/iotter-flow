package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

public class MeasureTick implements Serializable, IMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date date;
	private String key;
	
	public MeasureTick(String key, Date date) {
		super();
		this.date = date;
		this.key = key;
	}


	public Date getDate() {
		return date;
	}


	public String getKey() {
		return key;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public void setKey(String key) {
		this.key = key;
	}
	
	
}
