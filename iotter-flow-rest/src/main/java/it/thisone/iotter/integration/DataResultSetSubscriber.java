package it.thisone.iotter.integration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;


import com.fasterxml.jackson.databind.ObjectMapper;

import it.thisone.iotter.config.DevelEnabledCondition;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.rest.DeviceDataService;
import it.thisone.iotter.rest.model.DataPoint;
import it.thisone.iotter.rest.model.DataResultSet;
import it.thisone.iotter.rest.model.DataWrite;
import it.thisone.iotter.rest.model.DataWriteSet;
import it.thisone.iotter.rest.model.DeviceData;

@Service
@Conditional(DevelEnabledCondition.class)
public class DataResultSetSubscriber implements InitializingBean, DisposableBean {

	private static Logger logger = LoggerFactory.getLogger(DataResultSetSubscriber.class);

	@Autowired
	@Qualifier("mapper")
	public ObjectMapper mapper;

	@Autowired
	@Qualifier("develChannelAdapter")
	private MqttPahoMessageDrivenChannelAdapter mqtt;

//	private static String serverURI = "tcp://aernet.aermec.com:1883";
//	private static String userName = "admin";
//	private static String password = "USDc56nmUNUMnFYS";

	private static String TOPIC = "aernet/iotter-data/#";


//	@Autowired
//	private CassandraService cassandraService;

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private DeviceDataService dataService;

	@Override
	public void destroy() throws Exception {
		//subscriber.close();

	}

	// private MqttClient subscriber;
//	@Override
//	public void afterPropertiesSet() throws Exception {
//
//
//		String subscriberId = UUID.randomUUID().toString();
//		subscriber = new MqttClient(serverURI, subscriberId);
//
//		MqttConnectOptions options = new MqttConnectOptions();
//		options.setUserName(userName);
//		options.setPassword(password.toCharArray());
//		options.setAutomaticReconnect(true);
//		options.setCleanSession(true);
//		options.setConnectionTimeout(10);
//
//		subscriber.connect(options);
//
//		subscriber.subscribe(topic, (topic, msg) -> {
//
//			handleMessage(topic, msg);
//
//		});
//
//		logger.debug("MqttDataResultSetListener initialized");
//
//	}

	@Override
	public void afterPropertiesSet() throws Exception {

		MessageHandler messageHandler = new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC) != null
						? message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString()
						: "?";
				//logger.info(message.toString());
				if (topic.contains("data") && !topic.endsWith("status")) {
					if (message.getPayload() != null && message.getPayload() instanceof byte[]) {
						byte[] payload = (byte[]) message.getPayload();
//						String[] parts = StringUtils.split(topic, "\\/");
//						String serial = parts[parts.length - 1];
						handleDataResultSet(topic, payload);
					}
				}
			}
		};

		PublishSubscribeChannel psc = new PublishSubscribeChannel();
		psc.subscribe(messageHandler);
		mqtt.setOutputChannel(psc);

		mqtt.addTopic(TOPIC);

		if (!mqtt.isRunning()) {
			mqtt.start();
		}
		logger.info("DataResultSetSubscriber initialized");
		subscriptionService.checkSumDevices();

	}

	public void handleDataResultSet(String topic, byte[] payload) {
		// if (payload.length == 0 || !topic.contains("7c1guq1901R2-1-79329")) {
		// 	return;
		// }
		try {
			byte[] uncompressed = decompress(payload);
			String json = new String(uncompressed);
			Reader reader = new StringReader(json);
			DataResultSet result = mapper.readValue(reader, DataResultSet.class);
			Device device = deviceService.findDeviceCacheable(result.getSerial());
			logger.info(topic);
			if (device != null) {
				DataWrite data = dataWrite(result, device);
				Device master = device.getMaster();
				data.setApi_key(master.getWriteApikey());
				dataService.writeData(master.getSerial(), data);
			}
		} catch (Exception e) {
			logger.error("DataResultSetSubscriber " + topic, e);
		}
	}

	public static byte[] decompress(byte[] gzip) throws IOException {
		java.io.ByteArrayInputStream bytein = new java.io.ByteArrayInputStream(gzip);
		java.util.zip.GZIPInputStream gzin = new java.util.zip.GZIPInputStream(bytein);
		java.io.ByteArrayOutputStream byteout = new java.io.ByteArrayOutputStream();

		int res = 0;
		byte buf[] = new byte[1024];
		while (res >= 0) {
			res = gzin.read(buf, 0, buf.length);
			if (res > 0) {
				byteout.write(buf, 0, res);
			}
		}
		return byteout.toByteArray();
	}

//	public void insertDataResultSet(DataResultSet data) throws JsonProcessingException {
//		List<DataPoint> changes = new ArrayList<>();
//		for (DataPoint dp : data.getValues()) {
//			DataPoint value = new DataPoint();
//			value.setId(dp.getId());
//			value.setValue(dp.getValue());
//			changes.add(value);
//		}
//		Device device = deviceService.findDeviceCacheable(data.getSerial());
//		cassandraService.insertDataPoints(device, new Date(data.getLastContact() * 1000), changes);
//	}

	public DataWrite dataWrite(DataResultSet result, Device device) {
		String serial = result.getSerial();
		DataWrite data = new DataWrite();
		DataWriteSet ds = new DataWriteSet();
		ds.setTimestamp(result.getLastContact());
		List<DataPoint> dataPoints = result.getValues();
		String[] parts = StringUtils.split(serial, "-");
		String slaveId = parts[1];
		DeviceData dd = new DeviceData();
		dd.setSerial(serial);
		String[] idArray = dataPoints.stream().map(dp -> slaveId + ":" + dp.getId()).toArray(String[]::new);
		// Extract value array as primitive float[]
		float[] valueArray = new float[dataPoints.size()];
		for (int i = 0; i < dataPoints.size(); i++) {
			String number = idArray[i];
			Optional<Channel> match = device.getChannels().stream().filter(o -> o.getNumber().equals(number)).findFirst();
			if (match.isPresent()) {
				BigDecimal value = match.get().getDefaultMeasure().calculateRaw(dataPoints.get(i).getValue().floatValue());
				valueArray[i] = value.floatValue();
			}
			//valueArray[i] = dataPoints.get(i).getValue().floatValue();
		}
		dd.setIds(idArray);
		dd.setValue(valueArray);
		ds.getValues().add(dd);
		data.getData().add(ds);
		return data;

	}

}
