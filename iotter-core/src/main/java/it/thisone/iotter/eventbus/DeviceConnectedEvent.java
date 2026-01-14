package it.thisone.iotter.eventbus;

import org.springframework.context.ApplicationEvent;

public class DeviceConnectedEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final String serial;

	public DeviceConnectedEvent(Object source, String sn) {
		super(source);
		serial = sn;
	}
	public String getSerial() {
		return serial;
	}

}
