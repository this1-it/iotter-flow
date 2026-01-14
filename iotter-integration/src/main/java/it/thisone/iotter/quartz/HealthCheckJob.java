package it.thisone.iotter.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.integration.AlarmService;
import it.thisone.iotter.integration.MqttService;
import it.thisone.iotter.integration.RecoveryService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.rest.model.DeviceOnlineStatus;

@Service
@DisallowConcurrentExecution
public class HealthCheckJob implements Job, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// private static Logger logger =
	// LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);
	private static Logger logger = LoggerFactory.getLogger(HealthCheckJob.class);
	public static final String HEALTH_CHECK = "healthcheck-job";
	public static final String HEALTH_CHECK_GROUP = "healthcheck-cron";

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private MqttService mqttService;

	@Autowired
	private SubscriptionService subscriptionService;

//	@Autowired
//	private GroupWidgetService groupWidgetService;
//	
//	@Autowired
//	private CacheManager cacheManager;

	@Override
	public void execute(final JobExecutionContext ctx) throws JobExecutionException {
		logger.info("Executing Job {} ", ctx.getJobDetail());
		alarmService.notifyDelayedAlarms();
		alarmService.notifyRepeatedAlarms();
		List<Device> connected = subscriptionService.checkSumDevices();

		List<Device> alarmed = alarmService.notifyInactivityAlarms(connected);
		for (Device device : alarmed) {
			logger.debug("{} ONLINE -> OFFLINE ", device.getSerial());

			if (device.isPublishing()) {
				DeviceOnlineStatus data = new DeviceOnlineStatus();
				data.setLastContact(System.currentTimeMillis() / 1000);
				data.setSerial(device.getSerial());
				data.setOnline(false);
				mqttService.publishOnlineStatus(device.getSerial(), data, device.isPublishing());
			}
		}

		// Feature #1696
//		for (Device device : connected) {
//			List<Device> slaves = deviceService.findSlaves(device);
//			for (Device slave : slaves) {
//				if (slave.getLabel().contains("RECOVERY")) {
//					if (slave.getProfiles().isEmpty()) {
//						subscriptionService.rebuildSlaveDevice(slave.getSerial());
//					}
//					else {
//						ModbusProfile profile = slave.getProfiles().iterator().next();
//						GroupWidget groupWidget = groupWidgetService.findByExternalId(profile.getId(), slave.getSerial());
//						if (groupWidget == null || groupWidget.getWidgets().isEmpty()) {
//							logger.info("{} missing visualization ", slave.getSerial());
//						}
//
//					}					
//				}
//			}
//		}
		
		
//		Date activation = new Date();
//		for (Device device : connected) {
//			List<Device> slaves = deviceService.findSlaves(device);
//			for (Device slave : slaves) {
//				for (Iterator<Channel> iterator = slave.getChannels().iterator(); iterator.hasNext();) {
//					Channel channel = iterator.next();
//
//					Optional<Channel> duplicateWithNewerActivationDate = slave.getChannels().stream()
//							.filter(Channel.hasSameNumberAndNewerActivationDate(channel)).findFirst();
//
//					if (duplicateWithNewerActivationDate.isPresent()) {
//						logger.debug("{} duplicate {} {}", slave.getSerial(), duplicateWithNewerActivationDate.get().getNumber(), duplicateWithNewerActivationDate.get().getConfiguration().getActivationDate() );
//						if (duplicateWithNewerActivationDate.get().getConfiguration().getActivationDate().before(activation)) {
//							activation = duplicateWithNewerActivationDate.get().getConfiguration().getActivationDate();
//						}
//					}
//				}
//			}
//
//		}
//		logger.info("{}  activation {}", ctx.getJobDetail(), activation);


		// subscriptionService.cruft();

	}

}