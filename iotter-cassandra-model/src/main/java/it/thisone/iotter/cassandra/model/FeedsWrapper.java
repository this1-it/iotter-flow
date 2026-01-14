package it.thisone.iotter.cassandra.model;

import java.util.List;

public class FeedsWrapper {
	
	public FeedsWrapper(List<Feed> values, long timestamp) {
		super();
		this.values = values;
		this.timestamp = timestamp;
	}
	
	private List<Feed> values;
	private long timestamp;
	
	public List<Feed> getValues() {
		return values;
	}
	public void setValues(List<Feed> values) {
		this.values = values;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
