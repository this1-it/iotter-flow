package it.thisone.iotter.eventbus;

import org.springframework.context.ApplicationEvent;

public class DeviceUpdatedEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final String serial;
	private final String owner;
	private final String networkId;

	public DeviceUpdatedEvent(Object source, String serial, String owner, String networkId) {
		super(source);
		this.serial = serial;
		this.owner = owner;
		this.networkId = networkId;
	}
	public String getSerial() {
		return serial;
	}
	public String getOwner() {
		return owner;
	}
	public String getNetworkId() {
		return networkId;
	}
}
