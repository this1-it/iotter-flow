package it.thisone.iotter.eventbus;

import org.springframework.context.ApplicationEvent;

public class DeviceDataMessageEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final String serial;
	private final String topic;
	private final byte[] message;

	public DeviceDataMessageEvent(Object source, String topic, byte[] payload) {
		super(source);
		this.message = payload;
		this.topic = topic;
		this.serial = topic.substring(topic.lastIndexOf("/") + 1);
	}

	public String getSerial() {
		return serial;
	}

	public String getTopic() {
		return topic;
	}

	public byte[] getMessage() {
		return message;
	}

}
