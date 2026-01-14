package it.thisone.iotter.integration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.mqtt.MqttServiceException;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.rest.model.DataResultSet;
import it.thisone.iotter.rest.model.DeviceOnlineStatus;
import it.thisone.iotter.util.Utils;

@Service
public class MqttService {
	private static Logger logger = LoggerFactory.getLogger(Constants.MQTT.LOG4J_CATEGORY);

	@Autowired
	@Qualifier("gzipMapper")
	private ObjectMapper gzipMapper;

	@Autowired
	@Qualifier("mapper")
	private ObjectMapper mapper;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private MqttOutboundService mqttOutboundService;

	public void provisioned(ProvisionedEvent event) throws BackendServiceException {
		Device master = event.getMaster();
		String checksum = deviceService.provisioningChecksum(master);
		try {
			mqttOutboundService.provisioning(master.getSerial(), checksum);
		} catch (MqttServiceException e) {
			deviceService.trace(master, TracingAction.ERROR_BACKEND, Utils.stackTrace(e), null, null);
			throw new BackendServiceException(e);
		}
		deviceService.trace(master, TracingAction.MQTT_PROVISIONED, checksum, null, null);

	}

	public void publishLastValues(String serial, DataResultSet data, boolean retained) {
		try {
			byte[] payload = new byte[0];
			int size = 0;
			Date date = null;
			if (data != null) {
				date = new Date(data.getLastContact() * 1000);
				size = data.getValues().size();
				payload = gzipMapper.writeValueAsBytes(data);
			}
			if (mqttOutboundService.handleLastValues(serial, payload, retained)) {
				logger.debug("published last values: serial {}, size {}, date {}, bytes {}, retained {}", serial, size,
						date, payload.length, retained);
			}
		} catch (Throwable e) {
			logger.error(String.format("publishLastValues %s",serial), e);
		}
	}

	public void publishOnlineStatus(String serial, DeviceOnlineStatus data, boolean retained) {
		try {
			byte[] payload = new byte[0];
			boolean online = false;
			if (data != null) {
				payload = gzipMapper.writeValueAsBytes(data);
				online = data.isOnline();
			}
			if (mqttOutboundService.handleDeviceOnlineStatus(serial, payload, retained)) {
				logger.debug("published status: serial {}, online {}, retained {}", serial, online, retained);
			}
		} catch (JsonProcessingException e) {
		}
	}

	@Deprecated // Feature #2162
	public DataResultSet decodeLastValues(String topic, byte[] payload) {
		DataResultSet value = null;
		try {
			if (payload != null && payload.length > 0) {
				String json = new String(decompress(payload));
				Reader reader = new StringReader(json);
				// value = gzipMapper.readValue(payload, DataResultSet.class);
				value = mapper.readValue(reader, DataResultSet.class);				
			}

		} catch (IOException e) {
			logger.error(String.format("decodeLastValues %s",topic), e);
		}
		return value;
	}

	public static byte[] decompress(byte[] payload) throws IOException {
		java.io.ByteArrayInputStream bytein = new java.io.ByteArrayInputStream(payload);
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

}
