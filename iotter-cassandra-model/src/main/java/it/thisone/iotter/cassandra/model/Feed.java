package it.thisone.iotter.cassandra.model;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;

/*

CREATE TABLE feeds (
    sn varchar,
	key varchar,
	pid varchar,
	mu varchar,
	lbl varchar,
	qual int,
	act boolean,
	offset float,
	scale float,
	PRIMARY KEY ((sn), key)
);

ALTER TABLE feeds ADD since timestamp;
ALTER TABLE feeds ADD ttl int;
ALTER TABLE feeds ADD mini varchar;

ALTER TABLE feeds ADD sel boolean;
ALTER TABLE feeds ADD aval float;
ALTER TABLE feeds ADD typ varchar;


 */

public class Feed implements Serializable, IFeedKey {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Feed(String serial, String key) {
		super();
		this.serial = serial;
		this.key = key;
	}
	
	public boolean isValid() {
		return (serial != null && !serial.isEmpty()) &&
				(key != null && !key.isEmpty()) &&
				(identifier != null && !identifier.isEmpty());
	}
	
	public boolean hasLastValue() {
		return (date != null && value != null);
	}

	@Override
	public String toString() {
		return String.format("key=%s ,sn=%s, id=%s, active=%b",
				key, serial, identifier, active);
	}
	
	@Transient
	public boolean isChanged() {
		return changed;
	}

	@Transient
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	private boolean changed;
	
	private boolean active;

	private boolean selected;
	
	private boolean alarmed;
	
	private String serial;

	private String key;

	private String label;
	
	private String unit;
	
	private String identifier;
	
	private int qualifier;

	private Float offset;

	private Float scale;
	
	private Date date;

	private Float value;
	
	private Float aggregation;
	
	private Date since;

	private int ttl;

	private String interpolation;
	
	private String error;

	private FeedMeasureUnit measureUnit;
	
	private String typeVar;

	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public int getQualifier() {
		return qualifier;
	}

	public void setQualifier(int qualifier) {
		this.qualifier = qualifier;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Float getOffset() {
		return offset;
	}

	public void setOffset(Float offset) {
		this.offset = offset;
	}

	public Float getScale() {
		return scale;
	}

	public void setScale(Float scale) {
		this.scale = scale;
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

	public boolean isAlarmed() {
		return alarmed;
	}

	public void setAlarmed(boolean alarmed) {
		this.alarmed = alarmed;
	}


	public Date getSince() {
		return since;
	}

	public int getTtl() {
		return ttl;
	}

	public String getInterpolation() {
		return interpolation;
	}

	public void setSince(Date since) {
		this.since = since;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public void setInterpolation(String interpolation) {
		this.interpolation = interpolation;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}


	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Feed == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final Feed otherObject = (Feed) obj;
		return otherObject.getKey().equals(getKey());
	}

	@Transient
	public FeedMeasureUnit getMeasureUnit() {
		if (measureUnit == null) {
			measureUnit = new FeedMeasureUnit(scale, offset);
		}
		return measureUnit;
	}

	public Float getAggregation() {
		return aggregation;
	}

	public void setAggregation(Float aggregation) {
		this.aggregation = aggregation;
	}

	public String getTypeVar() {
		return typeVar;
	}

	public void setTypeVar(String typeVar) {
		this.typeVar = typeVar;
	}

	
	@Transient
	public String getRegisterId() {
		int index = this.getIdentifier() != null ? this.getIdentifier().indexOf(":") : -1 ;
		String id = index > 0 ? this.getIdentifier().substring(index + 1) : this.getIdentifier();
		return id;
	}
}
