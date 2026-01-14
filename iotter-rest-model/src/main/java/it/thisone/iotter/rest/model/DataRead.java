package it.thisone.iotter.rest.model;

import java.io.Serializable;

public class DataRead implements Serializable {

	private static final long serialVersionUID = 1L;

	private String apiKey = "";

	private Boolean ascending = true;
	
	private String interpolation = "";

	private Object[] params;
	
	private long from;

	private long to;

	private String qid;
	
	private Integer start = -1;

	public String getApiKey() {
		return apiKey;
	}

	public long getFrom() {
		return from;
	}

	public long getTo() {
		return to;
	}

	public String getQid() {
		return qid;
	}

	public Integer getStart() {
		return start;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public void setQid(String qid) {
		this.qid = qid;
	}

	public String getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(String interpolation) {
		this.interpolation = interpolation;
	}

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public void setStart(Integer start) {
		this.start = start;
	}


	
	
}
