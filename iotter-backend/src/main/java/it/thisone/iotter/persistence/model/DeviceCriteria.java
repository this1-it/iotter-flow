package it.thisone.iotter.persistence.model;

import java.io.Serializable;

import it.thisone.iotter.enums.DeviceStatus;

public class DeviceCriteria implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceCriteria() {
	}

	private String owner;

	private Boolean activated;
	
	private Boolean master;
	
	private Boolean publishing;

	private Boolean exporting;

	
	private DeviceStatus status;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public Boolean isActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
	}

	public Boolean isMaster() {
		return master;
	}

	public void setMaster(Boolean master) {
		this.master = master;
	}

	public Boolean isPublishing() {
		return publishing;
	}

	public void setPublishing(Boolean mqttPublishing) {
		this.publishing = mqttPublishing;
	}

	public Boolean isExporting() {
		return exporting;
	}

	public void setExporting(Boolean exporting) {
		this.exporting = exporting;
	}

}
