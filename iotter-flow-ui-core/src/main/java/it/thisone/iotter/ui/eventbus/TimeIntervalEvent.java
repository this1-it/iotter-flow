package it.thisone.iotter.ui.eventbus;

import it.thisone.iotter.ui.model.TimeInterval;


/*
 * used to update chart time interval
 */
public class TimeIntervalEvent {
    private final String source;
    private final TimeInterval interval;
    public TimeIntervalEvent(String parent, TimeInterval interval) {
    	this.source = parent;
        this.interval = interval;
    }
	public TimeInterval getInterval() {
		return interval;
	}
	public String getSource() {
		return source;
	}
}
