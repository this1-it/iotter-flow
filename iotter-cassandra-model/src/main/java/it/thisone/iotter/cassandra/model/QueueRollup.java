package it.thisone.iotter.cassandra.model;


import java.util.Date;

import com.google.common.collect.Range;

/*
 
create table iotter.roll_up_queue
(sn varchar, key varchar, first timestamp, last timestamp, PRIMARY KEY ((sn), key, first) ) 
WITH CLUSTERING ORDER BY (first ASC);

 
 */

@Deprecated
public class QueueRollup {
	
	public QueueRollup(FeedKey feed, Range<Date> interval) {
		super();
		this.serial = feed.getSerial();
		this.key = feed.getKey();
		this.last = interval.upperEndpoint();
		this.first = interval.lowerEndpoint();
	}

	public QueueRollup() {
		super();
	}

	private String serial;
	private String key;
	private Date last;
	private Date first;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getLast() {
		return last;
	}

	public void setLast(Date last) {
		this.last = last;
	}

	public Date getFirst() {
		return first;
	}

	public void setFirst(Date first) {
		this.first = first;
	}

}
