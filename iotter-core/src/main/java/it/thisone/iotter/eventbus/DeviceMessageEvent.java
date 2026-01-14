package it.thisone.iotter.eventbus;

import org.springframework.context.ApplicationEvent;

public class DeviceMessageEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final String serial;
	private final String topic;
	private final String message;

	public DeviceMessageEvent(Object source, String topic, String payload) {
		super(source);
		this.message = payload;
		// owner path is base64 encoded
		this.topic = topic;
		this.serial = topic.substring(topic.lastIndexOf("/") + 1);
	}

	public String getSerial() {
		return serial;
	}

	public String getTopic() {
		return topic;
	}

	public String getMessage() {
		return message;
	}

}
