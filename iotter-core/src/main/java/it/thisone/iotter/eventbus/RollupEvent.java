package it.thisone.iotter.eventbus;

import java.util.Date;

public class RollupEvent {
	private String key;
	private Date latest;
	private Date earliest;
	
	public RollupEvent(String key, Date last, Date first) {
		this.key = key;
		this.latest = last;
		this.earliest = first;
	}
	public String getKey() {
		return key;
	}
	public Date getLatest() {
		return latest;
	}
	public Date getEarliest() {
		return earliest;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setLatest(Date latest) {
		this.latest = latest;
	}
	public void setEarliest(Date earliest) {
		this.earliest = earliest;
	}
}
