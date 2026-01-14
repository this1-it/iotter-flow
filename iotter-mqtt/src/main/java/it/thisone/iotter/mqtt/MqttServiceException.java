package it.thisone.iotter.mqtt;

public class MqttServiceException extends Exception {
	private static final long serialVersionUID = 1L;
	public MqttServiceException(String message) {
		super(message);
	}

	public MqttServiceException(String message, Throwable cause) {
		super(message, cause);
	}



}
