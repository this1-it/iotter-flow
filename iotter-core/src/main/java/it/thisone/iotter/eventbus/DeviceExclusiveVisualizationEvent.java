package it.thisone.iotter.eventbus;

import java.util.List;

import org.springframework.context.ApplicationEvent;

public class DeviceExclusiveVisualizationEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final String serial;
	private final String externalId;
	private final List<String> params;

	public DeviceExclusiveVisualizationEvent(Object source, String serial, String externalId,
			List<String> params) {
		super(source);
		this.serial = serial;
		this.externalId = externalId;
		this.params = params;
	}

	public String getSerial() {
		return serial;
	}

	public String getExternalId() {
		return externalId;
	}

	public List<String> getParams() {
		return params;
	}

}
