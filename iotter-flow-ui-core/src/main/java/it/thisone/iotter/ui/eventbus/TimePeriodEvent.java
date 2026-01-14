package it.thisone.iotter.ui.eventbus;

import java.io.Serializable;

import it.thisone.iotter.ui.model.TimePeriod;

public class TimePeriodEvent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final TimePeriod period;
	private final String source;
	
	public TimePeriodEvent(String source, TimePeriod period) {
		this.period = period;
		this.source = source;
	}

	public TimePeriod getPeriod() {
		return period;
	}

	public String getSource() {
		return source;
	}
}
