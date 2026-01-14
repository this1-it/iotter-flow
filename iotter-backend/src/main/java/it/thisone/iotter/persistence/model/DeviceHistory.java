package it.thisone.iotter.persistence.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import it.thisone.iotter.enums.DeviceStatus;
/*
 * Feature #1192
 */
@Embeddable
public class DeviceHistory {

	@Enumerated(EnumType.STRING)
	@Column(name = "HIST_STATUS")
	private DeviceStatus status;	

	@Column(name = "HIST_OWNER")
	private String owner;

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}	
	
}
