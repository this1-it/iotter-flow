package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.google.common.collect.Range;

public class MeasureAggregation implements Serializable {

	public MeasureAggregation() {
		super();
		value = Float.NaN;
	}

	public MeasureAggregation(String id, Range<Date> interval) {
		super();
		key = id;
		value = Float.NaN;
		if (interval != null) {
			setDate(interval.lowerEndpoint());
			setInterval(interval);
		}
	}
	
	public boolean hasError() {
		return (error != null && !error.isEmpty());
	}

	@Override
	public String toString() {
		return String.format("key=%s ,count=%d, records=%d, min=%f, max=%f",
				key, count, records, minValue, maxValue);
	}


	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof MeasureAggregation == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final MeasureAggregation otherObject = (MeasureAggregation) obj;
		/*
		Bug #348 [Cassandra] rollup procedure is not correctly triggered if data is inserted in the past
		*/
		return new EqualsBuilder().append(getKey(), otherObject.getKey())
				.append(getCount(), otherObject.getCount())
				.append(getRecords(), otherObject.getRecords()).isEquals();
	}


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String key;
	private String serial;
	private Float value;
	private String error;
	private int count;
	private long records;

	private Float minValue;
	private Float maxValue;

	private Date date;
	private Date minDate;
	private Date maxDate;
	private Date errorDate;

	private boolean changed;
	private boolean alarmed;

	private Range<Date> interval;
	private int qualifier;

	public FeedKey getFeedKey() {
		return new FeedKey(serial, key);
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Float getMinValue() {
		return minValue;
	}

	public void setMinValue(Float minValue) {
		this.minValue = minValue;
	}

	public Float getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Float maxValue) {
		this.maxValue = maxValue;
	}

	public Date getDate() {
		return date;
	}
	
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Range<Date> getInterval() {
		return interval;
	}
	
	public void setInterval(Range<Date> interval) {
		this.interval = interval;
	}

	public int getQualifier() {
		return qualifier;
	}

	public void setQualifier(int qualifier) {
		this.qualifier = qualifier;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public long getRecords() {
		return records;
	}

	public void setRecords(long records) {
		this.records = records;
	}

	public Float getFrequency() {
		Float frequency = null;
		if (interval != null && records > 1) {
			Date begin = interval.lowerEndpoint();
			Date end = interval.upperEndpoint();
			float seconds = (end.getTime() - begin.getTime()) / 1000;
			frequency = seconds / (records - 1);
		}
		return frequency;
	}

	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}

	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}

	public Date getErrorDate() {
		return errorDate;
	}

	public void setErrorDate(Date errorDate) {
		this.errorDate = errorDate;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public boolean isAlarmed() {
		return alarmed;
	}

	public void setAlarmed(boolean alarmed) {
		this.alarmed = alarmed;
	}
	
	public Float getLastValue() {
		if (minDate != null && maxDate != null) {
			return minDate.after(maxDate) ? minValue : maxValue;
		}
		return value;
	}
	

}
