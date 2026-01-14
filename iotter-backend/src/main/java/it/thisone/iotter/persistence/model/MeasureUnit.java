package it.thisone.iotter.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class MeasureUnit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name = "TYPE")
	private Integer type;

	@NotNull
	@Column(name = "SCALE")
	private Float scale;

	@Column(name = "OFFEST")
	private Float offset;

	@Column(name = "FORMAT")
	private String format;

	@Transient
	private Boolean transparent;

	public MeasureUnit() {
		super();
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Float getScale() {
		return scale;
	}

	public void setScale(Float scale) {
		this.scale = scale;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" u:");
		sb.append(getType());
		sb.append(" m:");
		sb.append(getScale());
		sb.append(" q:");
		sb.append(getOffset());
		sb.append(" f:");
		sb.append(getFormat());
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37). //
				append(getType()). //
				append(getScale()). //
				append(getOffset()). //
				append(getFormat()). //
				toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof MeasureUnit == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		final MeasureUnit otherObject = (MeasureUnit) obj;
		return new EqualsBuilder(). //
				append(getType(), otherObject.getType()). //
				append(getScale(), otherObject.getScale()). //
				append(getOffset(), otherObject.getOffset()). //
				append(getFormat(), otherObject.getFormat()). //
				isEquals();
	}

	public Float getOffset() {
		return offset;
	}

	public void setOffset(Float offset) {
		this.offset = offset;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	/*
	 * Basically, a 0 means "always show the digit in this position", where a #
	 * means "show the digit in this position unless it's zero".
	 */

	@Transient
	public String normalizedFormat() {
		return getFormat().replaceAll("#\\.", "0\\.");
	}

	/*
	 * "precision" is the total number of significant digits in a number.
	 * "scale" is the number of digits to the right of the decimal point.
	 */
	@Transient
	public int getDecimals() {
		if (format == null) {
			return 0;
		}
		int counter = 0;
		for (int i = 0; i < format.length(); i++) {
			if (format.charAt(i) == '0') {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * check if listOne is contained in listTwo
	 * 
	 * @param listOne
	 * @param listTwo
	 * @return
	 */
	public static boolean compatibleMeasures(Collection<MeasureUnit> listOne, Collection<MeasureUnit> listTwo) {
		Collection<MeasureUnit> similar = new HashSet<MeasureUnit>(listOne);
		Collection<MeasureUnit> different = new HashSet<MeasureUnit>();
		different.addAll(listOne);
		different.addAll(listTwo);

		// Retains only the elements in this collection that are contained in
		// the specified collection
		similar.retainAll(listTwo);

		if (similar.size() == listOne.size() && similar.size() == listTwo.size()) {
			// elements in listTwo are totally different
			return true;
		}

		// Removes all of this collection's elements that are also contained in
		// the specified collection.
		different.removeAll(similar);
		if (different.size() != 0) {
			// listOne and listTwo are not contained each other
			return false;
		}

		// listOne is contained in listTwo and can be replaced
		return (listOne.size() <= listTwo.size());

	}

	@Transient
	public Float convert(Float raw) {
		if (transparent == null) {
			boolean zeroOffset = Float.compare(offset, 0f) == 0;
			boolean oneScale = Float.compare(scale, 1f) == 0;
			this.transparent = (zeroOffset && oneScale);
		}


		if (raw == null) {
			return raw;
		}
		if (raw.isNaN()) {
			return raw;
		}
		BigDecimal value = new BigDecimal(raw);
		int precision = getDecimals();
		value = value.setScale(precision, RoundingMode.HALF_UP);
		
		if (transparent) {
			return value.floatValue();
		}
		
		if (scale != null) {
			value = value.multiply(new BigDecimal(scale));
		}
		if (offset != null) {
			value = value.add(new BigDecimal(offset));
		}
		return value.floatValue();

	}

	@Transient
	public BigDecimal calculateRaw(Float set) {
		int precision = getDecimals();
		BigDecimal value = new BigDecimal(set);
		value = value.setScale(precision, RoundingMode.HALF_UP);
		if (offset != null) {
			value = value.add(new BigDecimal(offset * -1));
		}
		if (scale != null) {
			value = value.divide(new BigDecimal(scale), precision, RoundingMode.HALF_UP);
		}
		return value;

	}

}
