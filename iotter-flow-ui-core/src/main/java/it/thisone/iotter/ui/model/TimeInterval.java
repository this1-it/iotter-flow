package it.thisone.iotter.ui.model;

import java.util.Date;

import it.thisone.iotter.persistence.model.ValidityInterval;

/**
 * 
 * @author tisone
 *
 */
public class TimeInterval extends ValidityInterval {

	private static final long serialVersionUID = 3251996339684735726L;
	private Boolean extremes = true;
	
	public TimeInterval(Date startDate, Date endDate) {
		super(startDate,endDate);
	}

	public TimeInterval() {
		super();
	}


	public void setExtremes(Boolean extremes) {
		this.extremes = extremes;
	}


	public Boolean getExtremes() {
		return extremes;
	}
	
	
}
