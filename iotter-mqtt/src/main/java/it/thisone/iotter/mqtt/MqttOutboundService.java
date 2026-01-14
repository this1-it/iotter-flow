package it.thisone.iotter.mqtt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.util.EncryptUtils;

@Service
public class MqttOutboundService {
	private final static int QOS_0 = 0;
//	private final static int QOS_1 = 1;
//	private final static int QOS_2 = 2;
	//private static Logger logger = LoggerFactory.getLogger(Constants.MQTT.LOG4J_CATEGORY);
	private static Logger logger = LoggerFactory.getLogger(MqttOutboundService.class);

	@Autowired
	private MqttPahoMessageHandler mqtt;

	@Autowired
	@Qualifier("mqttProperties")
	private Properties properties;

	public void provisioning(String serial, String version) throws MqttServiceException {
		String topic = properties.getProperty("mqtt.provisioning", "mqtt.provisioning");
		topic = String.format(topic, serial);
		handleMessageString(topic, version, true, QOS_0);
	}

	public boolean handleLastValues(String serial, byte[] payload, boolean retained) {
		String topic = "";
		if (serial.contains("-")) {
			String[] parts = StringUtils.split(serial, "-");
			topic = String.format("%s/data/%s", parts[0], serial);
		} else {
			topic = String.format("%s/data/%s", serial, serial);
		}
		String application = properties.getProperty("mqtt.application", "TISONE");
		String name = Constants.MQTT.Topics.IOTTER_DATA.toString();
		topic = String.format("%s/%s/%s", application, name, topic);
		return handleMessage(topic, payload, retained, QOS_0);
	}

	public boolean handleDeviceOnlineStatus(String serial, byte[] payload, boolean retained) {
		String topic = "";
		if (serial.contains("-")) {
			String[] parts = StringUtils.split(serial, "-");
			topic = String.format("%s/data/%s/status", parts[0], serial);
		} else {
			topic = String.format("%s/status", serial);
		}
		String application = properties.getProperty("mqtt.application", "TISONE");
		String name = Constants.MQTT.Topics.IOTTER_DATA.toString();
		topic = String.format("%s/%s/%s", application, name, topic);
		return handleMessage(topic, payload, retained, QOS_0);
	}

	public void handleDeviceChanged(String owner, String network, String serial, String payload) {
		String application = properties.getProperty("mqtt.application", "TISONE");
		String name = Constants.MQTT.Topics.DEVICE.toString();
		// Bug #973
		owner = EncryptUtils.base64encode(owner);
		String topic = String.format("%s/%s/%s/%s/%s", application, name, owner, network, serial);
		handleMessageString(topic, payload, false, QOS_0);
	}

	public void handleModbusProfileChanged(String entityId, String action) {
		String application = properties.getProperty("mqtt.application", "TISONE");
		String name = Constants.MQTT.Topics.MODBUSPROFILE.toString();
		// Bug #973
		String id = EncryptUtils.base64encode(entityId);
		String topic = String.format("%s/%s/%s", application, name, id);
		String payload = action;
		handleMessageString(topic, payload, false, QOS_0);
	}

	private boolean handleMessageString(String topic, String payload, boolean retained, int qos) {
		logger.debug("handleMessage topic:{},  payload:{}, retained:{}", topic, payload, retained);
		return this.handleMessage(topic, payload.getBytes(StandardCharsets.US_ASCII), retained, qos);
	}

	private boolean handleMessage(String topic, byte[] payload, boolean retained, int qos) {
		try {
			Message<byte[]> message = MessageBuilder.withPayload(payload)
					.setHeader(MqttHeaders.TOPIC, topic) //
					.setHeader(MqttHeaders.RETAINED, new Boolean(retained)) //
					.setHeader(MqttHeaders.QOS, new Integer(qos)) //
					.build(); //
			mqtt.handleMessage(message);

		} catch (IllegalArgumentException | MessagingException e) {
			logger.error(topic, e);
			return false;
		}
		return true;
	}

	public void setValue(String topic, Number input) throws MqttServiceException {
		try {
			String value = numberToString(transformNumber(input));
			Message<byte[]> message = MessageBuilder.withPayload(value.toString().getBytes(StandardCharsets.US_ASCII))
					.setHeader(MqttHeaders.TOPIC, topic) //
					.setHeader(MqttHeaders.QOS, new Integer(QOS_0)) //
					.setHeader(MqttHeaders.RETAINED, new Boolean(false)) //
					.build();
			logger.debug("setValue {} {}", topic, value);
			mqtt.handleMessage(message);
		} catch (IllegalArgumentException | MessagingException e) {
			logger.error(topic, e);
			throw new MqttServiceException(topic, e);
		}
	}

	private String numberToString(Number n) {
		if (n == null) {
			throw new IllegalArgumentException("Null pointer");
		}
		testValidity(n);
		// Shave off trailing zeros and decimal point, if possible.
		String s = n.toString();
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while (s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	private void testValidity(Object o) {
		if (o != null) {
			if (o instanceof Double) {
				if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
					throw new IllegalArgumentException("not allow non-finite numbers");
				}
			} else if (o instanceof Float) {
				if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
					throw new IllegalArgumentException("not allow non-finite numbers.");
				}
			} else if (o instanceof BigDecimal || o instanceof BigInteger) {
				// ok
				return;
			}
		}
	}

	/*
	 * Transforms a Number into a valid javascript number. Float gets promoted to
	 * Double. Byte and Short get promoted to Integer. Long gets downgraded to
	 * Integer if possible.
	 */
	private Number transformNumber(Number input) {
		if (input instanceof Float) {
			return new Double(input.toString());
		} else if (input instanceof Short) {
			return new Integer(input.intValue());
		} else if (input instanceof Byte) {
			return new Integer(input.intValue());
		} else if (input instanceof Long) {
			Long max = new Long(Integer.MAX_VALUE);
			if (input.longValue() <= max.longValue() && input.longValue() >= Integer.MIN_VALUE) {
				return new Integer(input.intValue());
			}
		}
		return input;
	}

}
