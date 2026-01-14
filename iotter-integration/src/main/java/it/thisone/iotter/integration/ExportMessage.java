package it.thisone.iotter.integration;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import it.thisone.iotter.persistence.model.ModbusProfile;



public class ExportMessage implements Serializable {
	private static final long serialVersionUID = -371917901215997528L;
	
	private String serial;
	private String network;
	private String owner;
	private ExportItem master;
	private List<ExportItem> slaves;
	private File[] attachments;
	private String[] emails;
	private Locale locale;	


	// Getters and Setters
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


	public ExportItem getMaster() {
		return master;
	}

	public void setMaster(ExportItem master) {
		this.master = master;
	}
	
	public List<ExportItem> getSlaves() {
		if (slaves == null) {
			slaves = new ArrayList<ExportItem>();
		}
		
		return slaves;
	}

	public void setSlaves(List<ExportItem> slaves) {
		this.slaves = slaves;
	}
	
	// Inner class definition
	public static class ExportItem {
		private String label;
		private String interval;

		// Constructor
		public ExportItem(String label, String interval) {
			this.label = label;
			this.interval = interval;
		}

		// Getters and Setters
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getInterval() {
			return interval;
		}

		public void setInterval(String interval) {
			this.interval = interval;
		}
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public File[] getAttachments() {
		return attachments;
	}

	public void setAttachments(File[] attachments) {
		this.attachments = attachments;
	}

	public String[] getEmails() {
		return emails;
	}

	public void setEmails(String[] emails) {
		this.emails = emails;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}




}
