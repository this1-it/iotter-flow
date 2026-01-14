package it.thisone.iotter.eventbus;

public class DeviceConfiguredEvent {
	private final String serial;
	public DeviceConfiguredEvent(String sn) {
		serial = sn;
	}
	public String getSerial() {
		return serial;
	}

}
