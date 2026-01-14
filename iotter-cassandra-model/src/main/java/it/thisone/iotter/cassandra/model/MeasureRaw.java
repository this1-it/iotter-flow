package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MeasureRaw implements Serializable, IMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;




	private String key;
	private Float value;
	private Date date;
	private Date received;
	private String error;
	private boolean valid;

	/**
	 * used for inserting data
	 * 
	 */
	public MeasureRaw(String key, Float value, Date date, Date received, String error) {
		super();
		this.key = key;
		this.value = value;
		this.date = date;
		this.received = received;
		this.error = error;
	}
	
	/**
	 * used for retrieving data for charts
	 * 
	 * @param date
	 * @param value
	 * @param error
	 */
	public MeasureRaw(Date date, Float value, String error) {
		super();
		this.date = date;
		this.value = value;
		this.error = error;
		validate();
	}
	
	private void validate() {
		valid = true;
		if (this.date == null)
			valid = false;
		
		if (!hasError()) {
			if (this.value == null)
				valid = false;
			if (this.value != null && this.value.equals(Float.NaN)) {
				value = null;
				valid = false;
			}
		}
		
	}

	public MeasureRaw(String key, Date ts) {
		super();
		this.key = key;
		this.date = ts;
		validate();
	}
	
	public MeasureRaw(String key, Date ts, float value) {
		super();
		this.key = key;
		this.date = ts;
		this.value = value;
		validate();
	}

	public MeasureRaw() {
		super();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}


	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getKey()).append(getDate())
				.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof MeasureRaw == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final MeasureRaw otherObject = (MeasureRaw) obj;
		return new EqualsBuilder().append(getKey(), otherObject.getKey())
				.append(getValue(), otherObject.getValue())
				.append(getDate(), otherObject.getDate()).isEquals();
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean hasError() {
		return (error != null && !error.isEmpty());
	}

	public Date getReceived() {
		return received;
	}

	public void setReceived(Date received) {
		this.received = received;
	}


	
	@Override
	public String toString() {
		String ts = "null";
		if (date != null) {
			ts = date.toString();
		}
		return String.format("key=%s, value=%f, error=%s, date=%s", key, value, error, ts);
	}

	
}
