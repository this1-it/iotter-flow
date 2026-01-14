package it.thisone.iotter.config;

import java.net.InetAddress;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@ComponentScan({ "it.thisone.iotter.mqtt" })
public class MqttConfig {

	private static Logger logger = LoggerFactory.getLogger(Constants.MQTT.LOG4J_CATEGORY);

	@PostConstruct
	public void init() {
		Properties properties = mqttProperties();
		logger.info("MqttConfig initialized tcp://{}:{}", properties.getProperty("mqtt.host"), properties.getProperty("mqtt.port", "1883"));
	}

	@Bean
	public MqttPahoMessageHandler mqttMessageHandler() {
		Properties properties = mqttProperties();
		String applicationName = properties.getProperty("mqtt.application", "TISONE");
		int qos = 0;
		try {
			qos = Integer.parseInt(properties.getProperty("mqtt.qos", "0"));
		} catch (NumberFormatException e) {
		}

        String clientId = String.format("%s-%s", applicationName, MqttAsyncClient.generateClientId());
		MqttPahoMessageHandler mqtt = new MqttPahoMessageHandler(clientId, clientFactory());
		mqtt.setDefaultQos(qos);
		mqtt.setDefaultRetained(false);
		// mqtt.setDefaultRetained(true);
		// mqtt.setAsync(true);
		// mqtt.setAsyncEvents(true);
		return mqtt;
	}

	@Bean(name = "mqttInbound")
	public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
		Properties properties = mqttProperties();
		int qos = 2;
		try {
			qos = Integer.parseInt(properties.getProperty("mqtt.qos", "2"));
		} catch (NumberFormatException e) {
		}
		String applicationName = properties.getProperty("mqtt.application", "TISONE");
		String clientId = String.format("%s-%s", applicationName, MqttAsyncClient.generateClientId());
		MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, clientFactory());
		adapter.setQos(qos);
		adapter.setAutoStartup(false);
		DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
		converter.setPayloadAsBytes(true);
		adapter.setConverter(converter);
		adapter.setTaskScheduler(scheduler());

		return adapter;
	}

	@Bean(name = "clientFactory")
	public DefaultMqttPahoClientFactory clientFactory() {
		Properties properties = mqttProperties();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMaxInflight(32000);
		DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
		String username = properties.getProperty("mqtt.username", "");
		if (!username.isEmpty()) {
			options.setUserName(username);
			options.setPassword(properties.getProperty("mqtt.password").toCharArray());
		}
		String url = String.format("tcp://%s:%s", properties.getProperty("mqtt.host", "localhost"),
				properties.getProperty("mqtt.port", "1883"));
		options.setServerURIs(new String[] { url });
		clientFactory.setConnectionOptions(options);
		
		return clientFactory;
	}

	@Bean
	public TaskScheduler scheduler() {
		Properties properties = mqttProperties();

		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("mqtt-scheduler");
		int poolsize = 10;
		try {
			poolsize = Integer.parseInt(properties.getProperty("mqtt.poolsize", "10"));
		} catch (NumberFormatException e) {
		}		
		scheduler.setPoolSize(poolsize);
		//scheduler.setThreadPriority(Thread.MIN_PRIORITY);
		return scheduler;
	}

	@Bean
	public PublishSubscribeChannel outbound() {
		PublishSubscribeChannel psc = new PublishSubscribeChannel();
		psc.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				logger.debug(message.toString());
			}
		});
		return psc;
	}

	@Bean
	@Qualifier("mqttProperties")
	public Properties mqttProperties() {
		Properties properties = null;
		try {
			Resource resource = new ClassPathResource("mqtt.properties");
			if (!resource.exists()) {
				resource = new ClassPathResource("mqtt.default.properties");
			}
			properties = PropertiesLoaderUtils.loadProperties(resource);
			InetAddress ip = InetAddress.getLocalHost();
			properties.put("hostname", ip.getHostName());

		} catch (Exception e) {
			logger.error("unable to retrieve mqtt.properties", e);
		}
		return properties;
	}

}
