package it.thisone.iotter.eventbus;

public class DeviceActivatedEvent {
	private final String serial;
	public DeviceActivatedEvent(String sn) {
		serial = sn;
	}
	public String getSerial() {
		return serial;
	}

}
