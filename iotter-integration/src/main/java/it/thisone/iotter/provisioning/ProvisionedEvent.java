package it.thisone.iotter.provisioning;

import it.thisone.iotter.persistence.model.Device;

public class ProvisionedEvent {
	
	final private Device master;
	final private String originalChecksum;
	
	public ProvisionedEvent(final Device master, final String checksum) {
		super();
		this.master = master;
		this.originalChecksum = checksum;

	}
	public Device getMaster() {
		return master;
	}
	public String getOriginalChecksum() {
		return originalChecksum;
	}

}
