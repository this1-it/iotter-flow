package it.thisone.iotter.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.eventbus.DeviceDataMessageEvent;
import it.thisone.iotter.eventbus.DeviceMessageEvent;
import it.thisone.iotter.eventbus.ModbusProfileMessageEvent;
import it.thisone.iotter.util.EncryptUtils;

/*
 * mosquitto_sub -h aernet.aermec.com  -u admin -P USDc56nmUNUMnFYS -t "aernet-tp/MODBUSPROFILE/#" -v
 * 
 */
@Service
public class MqttInboundService implements InitializingBean, DisposableBean {
	private static Logger logger = LoggerFactory.getLogger(Constants.MQTT.LOG4J_CATEGORY);

	
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	@Qualifier("mqttInbound")
	private MqttPahoMessageDrivenChannelAdapter adapter;

	@Autowired
	@Qualifier("mqttProperties")
	private Properties properties;

	public void stop() {
		if (adapter != null && adapter.isRunning()) {
			adapter.stop();
		}
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		MessageHandler messageHandler = new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				String topic = message.getHeaders().get(MqttHeaders.TOPIC) != null
						? message.getHeaders().get(MqttHeaders.TOPIC).toString()
						: "";
				try {

					int index = topic.lastIndexOf("/");
					String id = index > 0 ? topic.substring(index + 1) : topic;
					if (topic.contains(Constants.MQTT.Topics.DEVICE.toString())) {
						// String payload = message.getPayload() != null ? message.getPayload().toString() : "";
						String payload = message.getPayload() != null ? new String((byte[])message.getPayload(), StandardCharsets.UTF_8) : "";
						logger.debug("{} received Device Message with serial :{}, payload:{}",topic,  id, payload);
						eventPublisher.publishEvent(new DeviceMessageEvent(this, topic, payload));
						
					} else if (topic.contains(Constants.MQTT.Topics.MODBUSPROFILE.toString())) {
						// Bug #973
						//String payload = message.getPayload() != null ? message.getPayload().toString() : "";
						String payload = message.getPayload() != null ? new String((byte[])message.getPayload(), StandardCharsets.UTF_8) : "";
						String entityId = EncryptUtils.base64decode(id);
						logger.debug("{} received ModbusProfile Message with id :{}, payload:{}", topic, id, payload);
						eventPublisher.publishEvent(new ModbusProfileMessageEvent(this, entityId, topic, payload));
					}
					else if (topic.contains(Constants.MQTT.Topics.IOTTER_DATA.toString()) && !topic.endsWith("status")) {
						// see converter.setPayloadAsBytes(true);
						byte[] payload = ((byte[])message.getPayload());
						logger.debug("{} received DeviceDataMessageEvent Message with id :{}, payload:{}", topic, id, payload.length);
						eventPublisher.publishEvent(new DeviceDataMessageEvent(this, topic, payload));
					}
				} catch (Throwable t) {
					logger.error(topic, t);
				}
			}
		};

		PublishSubscribeChannel psc = new PublishSubscribeChannel();
		psc.subscribe(messageHandler);
		adapter.setOutputChannel(psc);

		
		String application = properties.getProperty("mqtt.application", "TISONE");
		adapter.addTopic(String.format("%s/%s/#", application, Constants.MQTT.Topics.DEVICE.toString()));
		adapter.addTopic(String.format("%s/%s/#", application, Constants.MQTT.Topics.MODBUSPROFILE.toString()));
		
		
		// Feature #2162
		// adapter.addTopic(String.format("%s/%s/#", application, Constants.MQTT.Topics.IOTTER_DATA.toString()));

		
//		for (Topics topics : Constants.MQTT.Topics.values()) {
//			String name = String.format("%s/%s/#", application, topics.toString());
//			adapter.addTopic(name);
//			logger.debug("added topic {}", name);
//		}

		if (!adapter.isRunning()) {
			adapter.start();
		}

	}

}
