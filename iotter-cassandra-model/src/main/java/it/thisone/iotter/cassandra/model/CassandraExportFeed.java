package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Range;

/**
 * light weight bean used for export
 * @author tisone
 *
 */
public class CassandraExportFeed implements Serializable, IFeedKey {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String identifier;
	private final String registerId;
	private final String key;
	private final String label;
	private final String serial;
	private final String device;
	private final boolean active;
	private final List<Range<Date>> validities;
	private final String measureFormat;
	private final int measureDecimals;
	private final FeedMeasureUnit measureUnit;
	private boolean selected;


	public CassandraExportFeed(String number, String key, String label, String serial, String device, boolean active,
			List<Range<Date>> validities, Float measureScale, Float measureOffset, String measureFormat, int measureDecimals) {
		super();
		this.identifier = number;
		this.registerId = number.indexOf(":") < 0 ? number : number.substring(number.indexOf(":") + 1);
		this.key = key;
		this.label = label;
		this.serial = serial;
		this.device = device;
		this.active = active;
		this.validities = validities;
		this.measureFormat = measureFormat;
		this.measureDecimals = measureDecimals;
		this.selected = true;
		this.measureUnit = new FeedMeasureUnit(measureScale, measureOffset);
	}


	public String getKey() {
		return key;
	}

	public String getLabel() {
		return label;
	}

	public String getSerial() {
		return serial;
	}

	public List<Range<Date>> getValidities() {
		return validities;
	}

	public FeedKey getFeedKey() {
		return new FeedKey(serial,key);
	}

	public String getDevice() {
		return device;
	}

	public boolean isActive() {
		return active;
	}




	public String getMeasureFormat() {
		return measureFormat;
	}


	public int getMeasureDecimals() {
		return measureDecimals;
	}


	public boolean isSelected() {
		return selected;
	}


	public void setSelected(boolean b) {
		selected = b;
	}


	public FeedMeasureUnit getMeasureUnit() {
		return measureUnit;
	}


	public String getRegisterId() {
		return registerId;
	}


	public String getIdentifier() {
		return identifier;
	}
	
}
