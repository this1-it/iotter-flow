package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FeedMeasureUnit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6580567552219483801L;
	private final Float scale;
	private final Float offset;
	private final boolean transparent;
	
	
	public FeedMeasureUnit(Float scale, Float offset) {
		super();
		this.scale = scale;
		this.offset = offset;
		boolean zeroOffset = Float.compare(offset, 0f) == 0;
		boolean oneScale = Float.compare(scale, 1f) == 0;
		this.transparent = (zeroOffset && oneScale);
	}	

	
	public Float convert(Float raw) {
		if (transparent) {
			return raw;
		}
		if (raw == null) {
			return raw;
		}
		if (raw.isNaN()) {
			return raw;
		}
		BigDecimal value = new BigDecimal(raw);
		value.setScale(2, RoundingMode.HALF_UP);
		if (scale != null) {
			value = value.multiply(new BigDecimal(scale));
		}
		if (offset != null) {
			value = value.add(new BigDecimal(offset));
		}
		return value.floatValue();
	}
	
}
