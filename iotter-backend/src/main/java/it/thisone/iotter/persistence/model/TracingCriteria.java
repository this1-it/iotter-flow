package it.thisone.iotter.persistence.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.google.common.collect.Range;

import it.thisone.iotter.enums.TracingAction;

public class TracingCriteria implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TracingCriteria() {
	}

	private Range<Date> interval;
	
	private String network;
	
	private String administrator;

	private String owner;

	private String device;

	private Collection<TracingAction> actions;

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}

	public Collection<TracingAction> getActions() {
		if (actions == null) {
			actions = new HashSet<TracingAction>();
		}
		return actions;
	}

	public void setActions(Collection<TracingAction> actions) {
		this.actions = actions;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public Range<Date> getInterval() {
		return interval;
	}

	public void setInterval(Range<Date> interval) {
		this.interval = interval;
	}
	
}
