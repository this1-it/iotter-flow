package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MeasureRange implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5208421000794065207L;

	@Column(name = "UPPER")
	private Float upper;

	@Column(name = "LOWER")
	private Float lower;

	public Float getUpper() {
		return upper;
	}

	public void setUpper(Float upper) {
		this.upper = upper;
	}

	public Float getLower() {
		return lower;
	}

	public void setLower(Float lower) {
		this.lower = lower;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MeasureRange)) {
			return false;
		}
		MeasureRange other = (MeasureRange) obj;
		
		if (other.isNull()) {
			return this.isNull();
		}
		if (this.isNull()) {
			return other.isNull();
		}
		
		if (!getUpper().equals(other.getUpper())) {
			return false;
		}
		
		if (!getLower().equals(other.getLower())) {
			return false;
		}
		return true;
	}

	/**
	 * check ( start1 <= end2 and start2 <= end1 ) assumption ( start1 <= end1
	 * and start2 <= end2 )
	 * 
	 * @param other
	 * @return
	 */
	public boolean overlaps(MeasureRange other) {
		Float start1 = getLower();
		Float end1 = getUpper();
		Float start2 = other.getLower();
		Float end2 = other.getUpper();
		return ((start1 <= end2) && (start2 <= end1));
	}
	
	public boolean isValid() {
		if (isNull()) {
			return false;
		}
		return lower < upper;
	}

	
    public boolean isNull() {
        return lower == null || upper == null;
    }

	@Override
	public String toString() {
		return String.format("[%f - %f]", lower, upper);
	}

}
