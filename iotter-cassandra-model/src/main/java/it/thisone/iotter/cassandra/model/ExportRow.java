package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class ExportRow implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5568385077107339174L;
	
	private final Date timestamp;
	private final Float[] values;
	
	public ExportRow(Date id, int size) {
		super();
		this.timestamp = id;
		this.values = new Float[size];
	}
	public void set(int index, Float element) {
			values[index]= element;		
	}
	public Date getTimestamp() {
		return timestamp;
	}

	public List<Float> getValues() {
		return Arrays.asList(values);
	}
	
	

}
