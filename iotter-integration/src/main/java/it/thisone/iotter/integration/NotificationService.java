package it.thisone.iotter.integration;



import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.util.Utils;

@Service
public class NotificationService {

	private static Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
//	private static Logger logger = LoggerFactory
//			.getLogger(Constants.Notifications.LOG4J_CATEGORY);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private TracingService tracingService;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private MqttOutboundService outbound;

	public void modbusProfileChanged(ModbusProfile entity, String action) {
		outbound.handleModbusProfileChanged(entity.getId(), action);
	}
	
	public void deviceChanged(Device entity, TracingAction action) {
		String owner = entity.getOwner();
		String networkId = entity.getNetwork() != null ? entity.getNetwork().getId() : "";
		String serial = entity.getSerial();
		this.deviceChanged(owner, networkId, serial, action);
	}
	
	public void deviceChanged(String owner, String networkId, String serial, TracingAction action) {
		outbound.handleDeviceChanged(owner, networkId, serial, action.name());		
	}


	@Async
	public void forwardWeeklyExport(ExportMessage message) {
		String messageId = null;
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("device", message.getMaster().getLabel());
			params.put("interval", message.getMaster().getInterval());
			params.put("slaves", message.getSlaves());
			messageId = emailService.forwardWeeklyExport(message.getEmails(), message.getLocale(), message.getAttachments(), params);
			logger.debug("forwardWeeklyExport {} {} {}", message.getMaster().getLabel(), message.getMaster().getInterval(), messageId);
			deviceService.updateLastExportDate(message.getSerial());
		} catch (Throwable e) {
			messageId = logStackTrace(e);
			logger.error("forwardWeeklyExport {} {}", message.getMaster().getLabel(), messageId);
		}
		tracingService.trace(TracingAction.DEVICE_EXPORT, null, message.getOwner(), message.getNetwork(), message.getSerial(), messageId);
	}
	
	
	@Async
	public void forwardVisualization(String to, Locale locale, File exported, String visualization) {
		try {
			Map<String, Object> params = new HashMap<>();
			// {0} -> $visualization
			params.put(Constants.Notifications.VISUALIZATION, visualization);
			String messageId = emailService.forwardVisualization(to, locale, exported, params);
			logger.debug("forwardVisualization {} {} {}", to, visualization, messageId);
		} catch (Throwable e) {
			logger.error("forwardVisualization {} {}", to, logStackTrace(e));
		}
	}

	@Async
	public void successResetPassword(String to, Locale locale, String fullname) {
		try {
			Map<String, Object> params = new HashMap<>();
			// {0} -> $fullname
			params.put(Constants.Notifications.FULLNAME, fullname);
			String messageId = emailService.successResetPassword(to, locale, params);
			logger.debug("successResetPassword {} {}", to, fullname);
		} catch (Throwable e) {
			logger.error("successResetPassword {} {}", to, logStackTrace(e));
		}
	}
	

	@Async
	public void registration(String to, Locale locale, String fullname, String login, String url, String token) {
		try {
			Map<String, Object> params = new HashMap<>();
			// {0} -> $fullname
			// {1} -> $login
			// {2} -> $url
			// {3} -> $token

			params.put(Constants.Notifications.FULLNAME, fullname);
			params.put(Constants.Notifications.LOGIN, login);
			params.put(Constants.Notifications.URL, url);
			params.put(Constants.Notifications.TOKEN, token);
			String messageId = emailService.registration(to, locale, params);
			logger.debug("registration {} {} {}", to, fullname,messageId);
		} catch (Throwable e) {
			logger.error("registration {} {}", to, logStackTrace(e));
		}

	}


	@Async
	public void resetPassword(String to, Locale locale, String fullname, String login, String url, String token) {
		try {
			Map<String, Object> params = new HashMap<>();
			// {0} -> $fullname
			// {1} -> $login
			// {2} -> $url

			params.put(Constants.Notifications.FULLNAME, fullname);
			params.put(Constants.Notifications.LOGIN, login);
			params.put(Constants.Notifications.URL, url);
			params.put(Constants.Notifications.TOKEN, token);
			String messageId = emailService.resetPassword(to, locale, params);
			logger.debug("resetPassword {} {} {}", to, fullname,messageId);

		} catch (Throwable e) {
			logger.error("resetPassword {} {}", to, logStackTrace(e));
		}
	}
	
	
	@Async
	public void alarmNotification(String[] emails, Priority priority, Locale locale, AlarmMessage alarm) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put(Constants.Notifications.ALARM, alarm);
			String messageId = null;
			if (alarm.getStatus().equals(AlarmStatus.RESET.name())) {
				messageId = emailService.alarmReset(emails, priority, locale, params);
			}
			else {
				messageId = emailService.alarmNotification(emails, priority, locale, params);
			}
			logger.info("alarmNotification {} {} {}", alarm, emails, messageId);
		} catch (Throwable e) {
			logger.error("alarmNotification {} {}", alarm, logStackTrace(e));
		}
	}

	@Async
	public void alarmInactivity(String[] to, Priority priority, Locale locale, AlarmMessage alarm) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put(Constants.Notifications.ALARM, alarm);
			String messageId = emailService.alarmInactivity(to, priority, locale, params);
			logger.info("alarmInactivity {} {}", alarm, messageId);
		} catch (Throwable e) {
			logger.error("alarmInactivity {} {}", alarm, logStackTrace(e));
		}
		
	}
	
	private String logStackTrace(Throwable throwable) {
		return Utils.stackTrace(throwable).replaceAll(System.lineSeparator(), " ");
	}





}
