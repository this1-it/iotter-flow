package it.thisone.iotter.persistence.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class ValidityInterval implements Serializable {

	private transient static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss zzz");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * start is set to now
	 * end is null and interval is high-open [start, end=now)
	 */
	public ValidityInterval() {
		super();
		this.startDate = new Date();
	}

	/**
	 * 
	 * @param startDate must be not null
	 * @param endDate if null, interval is high-open [start, end=now)
	 */
	public ValidityInterval(Date startDate, Date endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		setStartDate(startDate);
		setEndDate(endDate);
	}


	@Temporal(TemporalType.TIMESTAMP)
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		if (startDate == null) {
			throw new IllegalArgumentException("start date cannot be null");
		}
		if (getEndDate().before(startDate)) {
			throw new IllegalArgumentException("end date before start date");
		}
		this.startDate = startDate;
	}

	/**
	 * 
	 * @return end date or now if null
	 */
	public Date getEndDate() {
		if (endDate == null)
			return new Date();
		return endDate;
	}

	public void setEndDate(Date endDate) {
		if (getStartDate().after(endDate)) {
			throw new IllegalArgumentException("ValidityRange end before start");
		}
		this.endDate = endDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getStartDate().hashCode();
		result = prime * result + getEndDate().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ValidityInterval)) {
			return false;
		}
		ValidityInterval other = (ValidityInterval) obj;
		if (!getStartDate().equals(other.getStartDate())) {
			return false;
		}
		if (!getEndDate().equals(other.getEndDate())) {
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
	public boolean overlaps(ValidityInterval other) {
		Date start1 = getStartDate();
		Date end1 = getEndDate();
		Date start2 = other.getStartDate();
		Date end2 = other.getEndDate();
		return ((start1.before(end2) || start1.equals(end2)) && (start2.before(end1) || start2.equals(end1)));
	}

	public boolean isOpen() {
		return (endDate == null);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(sdf.format(getStartDate()));
		sb.append(" - ");
		sb.append(sdf.format(getEndDate()));
		if (isOpen()) {
			sb.append(")");
		}
		else {
			sb.append("]");
		}
		return sb.toString();
	}
	
    public boolean isNull() {
        return startDate == null && endDate == null;
    }
    
    public long getTime() {
    	return getEndDate().getTime() - getStartDate().getTime();
    }
    
//    public void noFuture() {
//		if (startDate.after(new Date())) {
//			startDate = new Date();
//		}
//		if (endDate.after(new Date())) {
//			endDate = new Date();
//		}
//    }

    
    
    
}
