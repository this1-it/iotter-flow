package it.thisone.iotter.config;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@Conditional(DevelEnabledCondition.class)
@ComponentScan({ "it.thisone.iotter.integration" })
public class MqttDevelConfig {
	private static Logger logger = LoggerFactory.getLogger(Constants.MQTT.LOG4J_CATEGORY);
	@PostConstruct
	public void init() {
		logger.debug("MqttDevelConfig initialized");
	}
	@Bean(name = "develChannelAdapter")
	public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
		int qos = 2;
		UUID uuid = UUID.randomUUID();
		String shortUUID = Long.toString(uuid.getLeastSignificantBits(), Character.MAX_RADIX);
		String clientId = String.format("sub%s", shortUUID);
		MqttPahoMessageDrivenChannelAdapter mqtt = new MqttPahoMessageDrivenChannelAdapter(clientId, clientFactory());
		mqtt.setQos(qos);
		mqtt.setAutoStartup(false);
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("mqtt-devel-scheduler");
        scheduler.setPoolSize(10);
		DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
		converter.setPayloadAsBytes(true);

        mqtt.setConverter(converter);
        mqtt.setTaskScheduler(scheduler);
		return mqtt;
	}

	@Bean(name = "develClientFactory")
	public DefaultMqttPahoClientFactory clientFactory() {
		String username = "admin";
		String password = "USDc56nmUNUMnFYS";
		String url = String.format("tcp://%s:%s", "aernet.aermec.com", "1883");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMaxInflight(32000);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
		options.setServerURIs(new String[] { url });
		DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
		clientFactory.setConnectionOptions(options);
		return clientFactory;
	}

}
