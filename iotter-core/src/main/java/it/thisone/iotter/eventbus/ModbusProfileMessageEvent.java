package it.thisone.iotter.eventbus;

import org.springframework.context.ApplicationEvent;

public class ModbusProfileMessageEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String action;
	private final String topic;

	public ModbusProfileMessageEvent(Object source, String id, String topic, String action) {
		super(source);
		this.id = id;
		this.topic = topic;
		this.action = action;
	}
	public String getId() {
		return id;
	}
	public String getTopic() {
		return topic;
	}
	public String getAction() {
		return action;
	}

}
