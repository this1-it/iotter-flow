package it.thisone.iotter.cassandra.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/*

DROP TABLE measures_export_tmp;
CREATE TABLE measures_export_tmp (
  qid varchar,sn varchar,exp timestamp,lower timestamp,upper timestamp,start bigint,total bigint,batch_size int,ascending boolean,interpolation varchar, keys list<varchar>, PRIMARY KEY (qid)
);  


CREATE TABLE upcoming_calendar ( year int, month int, events list<text>, PRIMARY KEY ( year, month) );

*/


public class ExportQuery implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -600605444987174106L;
	
	private final String qid;

	private String serial;	
	
	private List<String> keys;

	private Date expires;
	
	private Date to;
	
	private Date from;

	private Long total;
	
	private Long start;

	private Long next;
	
	private int batchSize;
	
	private boolean ascending;

	private String interpolation;
	
	
	public ExportQuery(String qid) {
		super();
		this.qid = qid;
	}	

	public String getQid() {
		return qid;
	}

	public List<String> getKeys() {
		return keys;
	}

	public Date getExpires() {
		return expires;
	}

	public Date getTo() {
		return to;
	}

	public Date getFrom() {
		return from;
	}

	public Long getTotal() {
		return total;
	}

	public Long getStart() {
		return start;
	}

	public boolean isAscending() {
		return ascending;
	}



	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(String interpolation) {
		this.interpolation = interpolation;
	}

	public Long getNext() {
		return next;
	}

	public void setNext(Long next) {
		this.next = next;
	}
	
	public boolean isExpired() {
		if (expires == null) return true;
		Date now = new Date(System.currentTimeMillis());
		return expires.before(now);
	}
	
	public boolean isValid() {
		return !this.isExpired() && !this.keys.isEmpty();
	}
	
	public boolean isSingleBatch() {
		long diff = this.to.getTime() - this.from.getTime();
		return diff <= 3600 * 6 * 1000;
	}
	
	
	
}
