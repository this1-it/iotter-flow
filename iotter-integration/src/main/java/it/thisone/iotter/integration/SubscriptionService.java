package it.thisone.iotter.integration;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.cassandra.model.ConfigurationRegistry;
import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.FeedAlarmThresholds;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureAggregation;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.config.Initializator;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.enums.modbus.Format;
import it.thisone.iotter.enums.modbus.Signed;
import it.thisone.iotter.eventbus.DeviceConnectedEvent;
import it.thisone.iotter.eventbus.DeviceDataMessageEvent;
import it.thisone.iotter.eventbus.DeviceExclusiveVisualizationEvent;
import it.thisone.iotter.eventbus.DeviceMessageEvent;
import it.thisone.iotter.eventbus.DeviceUpdatedEvent;
import it.thisone.iotter.eventbus.ModbusProfileMessageEvent;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelAlarm;
import it.thisone.iotter.persistence.model.ChannelRemoteControl;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusConfiguration;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.DatabaseMessageSource;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.provisioning.ProvisioningEvent;
import it.thisone.iotter.quartz.RollupJob;
import it.thisone.iotter.rest.model.DataPoint;
import it.thisone.iotter.rest.model.DataResultSet;
import it.thisone.iotter.rest.model.DeviceOnlineStatus;
import it.thisone.iotter.rest.model.ModbusProvisioning;
import it.thisone.iotter.util.BacNet;
import it.thisone.iotter.util.Utils;
import net.sf.ehcache.Statistics;

// http://java.sys-con.com/node/2123093

@Service
public class SubscriptionService extends Initializator {

	@Autowired
	private DatabaseMessageSource databaseMessageSource;

	@Autowired
	private CassandraService cassandraService;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private AlarmService alarms;

	@Autowired
	private NetworkGroupService groupService;

	@Autowired
	private GroupWidgetService groupWidgetService;

	@Autowired
	private ModbusProfileService profileService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private MqttService mqttService;

	@Autowired
	private CacheManager cacheManager;

	public static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
	private static Logger notificationLogger = LoggerFactory.getLogger(Constants.Notifications.LOG4J_CATEGORY);
	private static Logger asyncLogger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);
	private static Logger rollupLogger = LoggerFactory.getLogger(Constants.RollUp.ROLL_UP_LOG4J_CATEGORY);
	private static Logger exporterLogger = LoggerFactory.getLogger(Constants.Exporter.LOG4J_CATEGORY);

	// @Override
	// public void destroy() throws Exception {
	// asyncLogger.debug("un-registering SubscriptionService on eventbus");
	// eventBus.unregister(this);
	// }

	@Override
	public void afterPropertiesSet() throws Exception {
//		try {
//			List<DataSink> all = cassandraService.getFeeds().getDataSinks();
//			List<DataSink> connected = all.stream()
//					.filter(o -> o.getStatus().equals(DeviceStatus.CONNECTED.name()) && o.getRecords() > 0)
//					.collect(Collectors.toList());
//			;
//			for (DataSink dataSink : connected) {
//				this.deviceService.findDeviceCacheable(dataSink.getSerial());
//			}
//			asyncLogger.info("SubscriptionService initially cached devices {}", connected.size());
//		} catch (Exception e) {
//		}
	}

	@Transactional
	public void provisioning(ProvisioningEvent event) throws BackendServiceException {
		Device master = event.getMaster();
		Map<String, String> mapSlaves = event.getMapSlaves();
		List<String> originalProfiles = event.getOriginalProfiles();
		List<ModbusProfile> profiles = event.getProfiles();
		Map<String, GroupWidget> mapWidgets = event.getMapWidgets();
		List<Device> created = new ArrayList<>();
		List<Device> updated = new ArrayList<>();
		List<GroupWidget> createdWidget = new ArrayList<>();
		List<GroupWidget> updatedWidget = new ArrayList<>();
		List<String> serials = new ArrayList<String>(mapSlaves.values());
		Map<String, NetworkGroup> groups = new HashMap<String, NetworkGroup>();
		Map<String, List<String>> selectedMap = new HashMap<>();

		try {
			for (ModbusProfile source : profiles) {
				String label = String.format("%s-%d", master.getSerial(), source.getConfiguration().getSlaveID());
				String description = String.format("%s-%s", source.getDisplayName(),
						source.getConfiguration().getSlaveName());
				String name = String.format("%s %s", label, description);
				if (originalProfiles.contains(source.getId())) {
					String serial = mapSlaves.get(source.getId());
					Device slave = deviceService.findBySerial(serial);
					if (!slave.getProfiles().isEmpty()) {
						ModbusProfile target = slave.getProfiles().iterator().next();
						// Feature #1885
						String originalId = target.getConfiguration().getSlaveID().toString();
						// Feature #1887
						// profileService.copy(source, target);
						profileService.commitChanges(source, target);
						
						String slaveId = target.getConfiguration().getSlaveID().toString();
						if (!slaveId.equals(originalId)) {
							slave.setLabel(label);
							slave.setDescription(description);
							slave.changeSlaveId(originalId, slaveId);
							cassandraService.getRegistry().deleteAllConfigurationRegistry(slave.getSerial());
						}
						slave = reconfigureChannels(slave, target);
					}

					updated.add(slave);
					mapSlaves.remove(source.getId());
					GroupWidget entity = mapWidgets.get(source.getId());
					if (entity != null) {
						if (entity.isNew()) {
							entity.setName(name);
							entity.setCreator(master.getOwner());
							entity.setOwner(master.getOwner());
							entity.setExternalId(source.getId());
							entity.setDevice(serial);
							createdWidget.add(entity);
						} else {
							updatedWidget.add(entity);
						}
					}
				} else {
					// new profile
					source.setCreationDate(new Date());
					Device slave = new Device();
					slave.setOwner(master.getOwner());
					String serial = String.format("%s-%d-%s", master.getSerial(),
							source.getConfiguration().getSlaveID(), RandomStringUtils.random(5, false, true));
					while (serials.contains(serial)) {
						serial = String.format("%s-%d-%s", master.getSerial(), source.getConfiguration().getSlaveID(),
								RandomStringUtils.random(5, false, true));
					}

					serials.add(serial);
					slave.setLabel(label);
					slave.setDescription(description);
					slave.setSerial(serial);
					slave.getProfiles().add(source);
					created.add(slave);
					GroupWidget entity = mapWidgets.get(source.getId());
					if (entity != null) {
						entity.setName(name);
						entity.setCreator(master.getOwner());
						entity.setOwner(master.getOwner());
						entity.setExternalId(source.getId());
						entity.setDevice(serial);
						GraphicWidget widget = entity.getWidgets().get(0);
						widget.setDevice(serial);
						createdWidget.add(entity);
					}
				}
			}

			for (GroupWidget entity : createdWidget) {
				entity = deviceService.createConnectedWidget(entity, master.getNetwork());
				NetworkGroup group = entity.getGroup();
				if (group != null) {
					groups.put(entity.getDevice(), groupService.findOne(group.getId()));
				}
			}

			for (GroupWidget entity : updatedWidget) {
				List<String> selected = new ArrayList<>();
				List<GraphicFeed> feeds = groupWidgetService.updateWithMaterializeFeeds(entity);
				for (GraphicFeed feed : feeds) {
					selected.add(feed.getMetaData());
				}
				selectedMap.put(entity.getDevice(), selected);
			}

			for (Device slave : created) {
				switch (master.getStatus()) {
				case CONNECTED:
					slave.setStatus(DeviceStatus.ACTIVATED);
					break;
				default:
					slave.setStatus(master.getStatus());
					break;
				}
				if (master.getNetwork() != null) {
					slave.addGroup(master.getNetwork().getDefaultGroup());
					NetworkGroup group = groups.get(slave.getSerial());
					if (group != null) {
						slave.addGroup(group);
					}
				}
				slave.setInactivityMinutes(Constants.Provisioning.INACTIVITY_MINUTES);
				slave.setPublishing(master.isPublishing());
				slave.setTracing(master.isTracing());
				slave.setMaster(master);
				slave.setProductionDate(new Date());
				deviceService.create(slave);
			}

			for (Device slave : updated) {
				NetworkGroup group = groups.get(slave.getSerial());
				if (group != null) {
					slave.addGroup(group);
				}
				List<String> selected = selectedMap.get(slave.getSerial());
				if (selected != null) {
					for (Channel chnl : slave.getChannels()) {
						if (selected.contains(chnl.getMetaData())) {
							chnl.getConfiguration().setExclusive(true);
							chnl.getConfiguration().setSelected(true);
						} else {
							if (chnl.getConfiguration().isExclusive()) {
								chnl.getConfiguration().setExclusive(false);
								chnl.getConfiguration().setSelected(false);
							}
						}
					}
				}
				deviceService.update(slave);
			}

			for (String slaveId : mapSlaves.values()) {
				Device slave = deviceService.findBySerial(slaveId);
				deviceService.disconnect(slave, true);
				deviceService.delete(slave);
				resetData(slave);
			}

		} catch (Throwable e) {
			deviceService.trace(master, TracingAction.ERROR_BACKEND, Utils.stackTrace(e), null, null);
			logger.error("provisioning", e);
			throw new BackendServiceException(e.getMessage(), e);
		}
	}

	/*
	 * eventually remove channels with duplicated modbus address
	 * #1887 manage crucial/critical register
	 */
	public Device reconfigureChannels(Device slave, ModbusProfile target) {
		String slaveId = target.getConfiguration().getSlaveID().toString();
		// registers to be configured
		Map<String, ModbusRegister> registers = new HashMap<String, ModbusRegister>();
		for (ModbusRegister register : target.getRegisters()) {
			String number = profileService.modbus_address(slaveId, register);
			registers.put(number, register);
		}

		//List<Channel> removed = slave.removeInactiveDuplicates();
		
		for (Iterator<Channel> iterator = slave.getChannels().iterator(); iterator.hasNext();) {
			Channel channel = iterator.next();
			// It is possible to have a duplicate channel because the crucial registers 
            // can be changed by address, qualifier (and other factors), 
            // resulting in a associated channel with a different key.			
            Optional<Channel> duplicateWithNewerActivationDate = slave.getChannels().stream()
                    .filter(Channel.hasSameNumberAndNewerActivationDate(channel))
                    .findFirst();

            if (duplicateWithNewerActivationDate.isPresent()) {
            	logger.info("{} {} {}", channel.getNumber(), channel.getKey(), duplicateWithNewerActivationDate.get().getKey());
            	channel.setOid(null);
            	channel.setMetaData(null);
                channel.deActivateChannel(new Date());
            }
            else {
			//if (channel.getConfiguration().isActive()) {
				ModbusRegister register = registers.get(channel.getNumber());
				if (register != null) {
					// Feature #1885
					channel.setOid(register.getId());
					channel.setMetaData(register.getMetaData());
					if (register.getActive()) {
						channel.activateChannel(new Date());
					} else {
						channel.deActivateChannel(new Date());
					}
					registers.remove(channel.getNumber());
				} else {
					// crucial register to be removed (Feature #1887)
					//logger.info("register has been removed {} ", channel.toString());
					iterator.remove();
				}				
			}

		}
		
//		if (!removed.isEmpty()) {
//			slave = deviceService.deleteChannels(slave, removed);
//		}
		return slave;
	}

	@Transactional
	public void deviceConnected(DeviceConnectedEvent event) {
		Device entity = deviceService.findBySerial(event.getSerial());
		if (entity != null) {
			try {
				List<String> selected = new ArrayList<>();
				List<GroupWidget> widgets = groupWidgetService.findExclusiveVisualizations(event.getSerial());
				for (GroupWidget widget : widgets) {
					List<GraphicFeed> feeds = groupWidgetService.updateWithMaterializeFeeds(widget);
					for (GraphicFeed feed : feeds) {
						selected.add(feed.getMetaIdentifier());
					}
				}
				for (Channel chnl : entity.getChannels()) {
					if (chnl.getConfiguration().isActive() && selected.contains(chnl.getMetaIdentifier())) {
						chnl.getConfiguration().setSelected(true);
						chnl.getConfiguration().setExclusive(true);
					}
				}
				entity.setStatus(DeviceStatus.CONNECTED);
				deviceService.update(entity);
				deviceService.trace(entity, TracingAction.DEVICE_CONNECT, entity.toString(), null, null);
				notificationService.deviceChanged(entity, TracingAction.DEVICE_CONNECT);
			} catch (BackendServiceException e) {
				deviceService.trace(entity, TracingAction.ERROR_BACKEND, Utils.stackTrace(e), null, null);
			}

		}
	}

	public void rollupDevice(String serial, boolean enableBreak) {
		Device device = deviceService.findBySerialCached(serial);
		if (device != null) {
			boolean full = true;
			scheduleRollupJob(device, null, enableBreak, full);
		}
	}

	// @Async
	public void postProcessConfig(Collection<Device> devices, boolean partial) {
		for (Device device : devices) {
			try {
				if (!partial) {
					String checkSum = cassandraService.getFeeds().getCheckSum(device.getSerial());
					asyncLogger.debug("postProcessConfig {} {} {}", device.getSerial(), device.getCheckSum(), checkSum);
					if (!device.getCheckSum().equals(checkSum)) {
						this.updateDataSinkOnConfig(device);
					}
				}
				if (device.getMaster() == null) {
					// Date lastContact = (device.getMaster() == null) ? new
					// Date() : null;
					cassandraService.getFeeds().updateLastContact(device.getSerial(), new Date());
					notificationService.deviceChanged(device, TracingAction.DEVICE_CONFIGURATION);
				}

			} catch (RuntimeException t) {
				asyncLogger.error(String.format("failure on postProcessConfig %s", device.getSerial()), t);
			}

		}
	}

    /**
     * Processes post-read data operations for a device.
     * <p>
     * This method performs a sequence of post-processing tasks after device data has been written or updated.
     * The processing is divided into three main parts:
     * <ul>
     *   <li>
     *     <b>Alarm Processing:</b> It processes any pending alarms based on the raw measurement events for each device.
     *   </li>
     *   <li>
     *     <b>Feeds Update:</b> It updates the last values for device feeds and publishes any changes. Additionally,
     *     it examines timestamp ticks and prepares DataPoint objects for publishing if the device is actively publishing
     *     via MQTT for integration with external applications
     *   </li>
     *   <li>
     *     <b>Device Updates:</b> It processes device status updates including managing inactivity or changes in the alarm state.
     *   </li>
     * </ul>
     * </p>
     *
     * @param device        the primary device object for which the post-processing is being performed
     * @param events        a map of raw measurement events (MeasureRaw) keyed by device serial numbers
     * @param devices       a map of devices involved in the operation, keyed by their serial numbers
     * @param lastValuesMap a map containing lists of Feed objects representing the latest feed values for each device
     * @param aggregations  a map of measure aggregations (MeasureAggregation) for roll-up calculations, keyed by device serial numbers
     * @param tickMap       a map of sets of timestamp ticks (as Date objects) for each device, used to track measurement intervals
     * @param dataPointMap  a map containing lists of DataPoint objects for each device, stored to facilitate further operations or data extraction (e.g., CSV or Excel export)
     */
	
	@Async
	public void postProcessData(Device device, Map<String, List<MeasureRaw>> events, Map<String, Device> devices,
			Map<String, List<Feed>> lastValuesMap, Map<String, List<MeasureAggregation>> aggregations,
			Map<String, Set<Date>> tickMap, Map<String, List<DataPoint>> dataPointMap) {
		try {
			processAlarms(events, devices);
		} catch (RuntimeException t) {
			asyncLogger.error(String.format("failure on processAlarms %s", device.getSerial()), t);
		}
		try {
			processFeeds(devices, lastValuesMap, tickMap, dataPointMap);
		} catch (RuntimeException t) {
			asyncLogger.error(String.format("failure on processFeeds %s", device.getSerial()), t);
		}
		try {
			processUpdates(devices);
		} catch (RuntimeException t) {
			asyncLogger.error(String.format("failure on processUpdates %s", device.getSerial()), t);
		}

	}

	// Bug #2035
	private void processAlarms(Map<String, List<MeasureRaw>> events, Map<String, Device> devices) {
		for (Map.Entry<String, Device> entry : devices.entrySet()) {
			List<MeasureRaw> measures = events.get(entry.getKey());
			if (measures != null) {
				boolean fired = alarms.processEvents(measures, entry.getValue());
				entry.getValue().setAlarmStatus(fired ? AlarmStatus.CHANGED : AlarmStatus.UNDEFINED);
			}
		}
	}
 
	// Bug #2035
	private void processUpdates(Map<String, Device> devices) {
		Date now = new Date();
		for (Device device : devices.values()) {
			Date lastContactDate = cassandraService.getFeeds().getLastContact(device.getSerial());
			cassandraService.getFeeds().updateLastContact(device.getSerial(), now);

			try {
				if (AlarmStatus.CHANGED.equals(device.getAlarmStatus())) {
					alarms.changedAlarms(device);
				}

			} catch (Throwable e) {
				logger.error("Mis-configured Alarm {} missing feed / thresholds", e);
			}
			
			
			if (device.checkInactive(lastContactDate)) {
				logger.debug("{} OFFLINE -> ONLINE", device.getSerial());
				alarms.analizeInactivity(device, now);
			}


			if (device.isPublishing()) {
				DeviceOnlineStatus data = new DeviceOnlineStatus();
				data.setLastContact(now.getTime() / 1000);
				data.setSerial(device.getSerial());
				data.setOnline(true);
				mqttService.publishOnlineStatus(device.getSerial(), data, true);
			}
		}
	}

	/**
	 * Processes feed updates for devices and publishes data point values via MQTT for integration with external applications.
	 * <p>
	 * This method iterates over the feed data for each device. For each device, it:
	 * <ul>
	 *   <li>Updates the last feed values in the database.</li>
	 *   <li>Determines the most recent timestamp from the provided set of ticks.</li>
	 *   <li>Builds a data result set based on the available data points.</li>
	 *   <li>If the device is configured for publishing, it transmits the data points via MQTT, enabling external applications
	 *       to easily integrate and consume real-time information.</li>
	 * </ul>
	 * </p>
	 *
	 * @param devices      a map of devices keyed by their serial numbers
	 * @param map          a map of lists of Feed objects representing the latest feed values for each device
	 * @param tickMap      a map of sets of Date objects (ticks) for each device, used to determine the latest measurement timestamp
	 * @param dataPointMap a map of lists of DataPoint objects for each device, representing the individual data points to be published
	 */
	private void processFeeds(Map<String, Device> devices, Map<String, List<Feed>> map,
			Map<String, Set<Date>> tickMap, Map<String, List<DataPoint>> dataPointMap) {
		for (Map.Entry<String, List<Feed>> entry : map.entrySet()) {
			Device device = devices.get(entry.getKey());
			List<Feed> feeds = entry.getValue();
			Set<Date> ticks = tickMap.get(entry.getKey());
			List<DataPoint> dataPoint = dataPointMap.get(entry.getKey());
			cassandraService.getFeeds().updateLastValuesBatch(feeds);
			
			long lastContact = 0;
			if (ticks != null && !ticks.isEmpty()) {
				long tick = ticks.iterator().next().getTime() / 1000;
				if (tick > lastContact) {
					lastContact = tick;
				}
			}
			
			if (device.isPublishing()) {
				
				DataResultSet data = CassandraService.publishingDataResultSet(device, dataPoint, lastContact);
				mqttService.publishLastValues(device.getSerial(), data, false);
				
//				List<DataPoint> changes = new ArrayList<>();
//				for (Feed feed : feeds) {
//					if (feed.isChanged() && feed.hasLastValue()) {
//						DataPoint dp = CassandraService.buildDataPoint(feed);
//						if (dp.getTs() > lastContact) {
//							lastContact = dp.getTs();
//						}
//						changes.add(dp);
//					}
//				}
//				DataResultSet data = cassandraService.findDataResultSetCacheable(device);
//				cassandraService.mergeDataResultSetValues(changes, data);
//				if (lastContact >= data.getLastContact()) {
//					data.setLastContact(lastContact);
//					mqttService.publishLastValues(device.getSerial(), data, false);
//				} else {
//					asyncLogger.debug("serial {} publishLastValues discarded at {}", data.getSerial(),
//							new Date(lastContact * 1000));
//				}
			}
		}
	}

	private boolean scheduleRollupJob(Device device, List<MeasureAggregation> measures, boolean enableBreak,
			boolean full) {
		Trigger trigger = newTrigger().withIdentity(device.getSerial() + "-roll_up", "roll_up").startNow().build();
		if (measures == null) {
			measures = new ArrayList<MeasureAggregation>();
			for (Channel channel : device.getChannels()) {
				if (channel.getConfiguration().isActive() || full) {
					MeasureAggregation measure = new MeasureAggregation();
					measure.setKey(channel.getKey());
					measure.setSerial(device.getSerial());
					measures.add(measure);
				}
			}
		}

		JobDataMap jobDataMap = trigger.getJobDataMap();
		jobDataMap.put(RollupJob.ENABLE_BREAK, enableBreak);
		jobDataMap.put(RollupJob.DEVICE, device.getSerial());
		jobDataMap.put(RollupJob.OWNER, device.getOwner());
		jobDataMap.put(RollupJob.MEASURES, measures);
		JobKey jobKey = new JobKey(trigger.getKey().getName(), trigger.getKey().getGroup());
		JobDetail jobDetail = newJob(RollupJob.class).withIdentity(jobKey).build();
		try {
			int size = measures.size();
			if (checkExists(jobKey, trigger, Constants.RollUp.GRACE_TIME_SECS * size)) {
				throw new ObjectAlreadyExistsException(jobDetail);
			}
			rollupLogger.debug("{} Rollup Job started", device.getSerial());
			scheduler.scheduleJob(jobDetail, trigger);
			return true;
		} catch (SchedulerException e) {
			if (e instanceof ObjectAlreadyExistsException) {
				rollupLogger.debug("{} Rollup Job unable to start, because one already exists with this identification",
						device.getSerial());
				rollupLogger.debug("{} Rollup Job delayed", device.getSerial());
			} else {
				rollupLogger.error("scheduleRollupJob - device: " + device.getSerial(), e);
			}
		}
		return false;
	}

	private boolean checkExists(JobKey jobKey, Trigger trigger, long seconds) throws SchedulerException {
		boolean result = false;
		if (scheduler.checkExists(jobKey)) {
			result = true;
			Trigger old = scheduler.getTrigger(trigger.getKey());
			if (old != null) {
				Date start = old.getFinalFireTime();
				Date now = new Date();
				long elapsed = now.getTime() - start.getTime();
				if (elapsed > seconds * 1000) {
					asyncLogger.debug("deleting Job : '{}', because it lasted more than {} seconds ", jobKey, seconds);
					scheduler.deleteJob(jobKey);
					result = false;
				} else {
					asyncLogger.debug("unable to start Job : '{}', because one already exists with this identification",
							jobKey);
				}
			}
		}
		return result;
	}

	@Async
	public void rollUpConnectedDevices() {
		DeviceCriteria criteria = new DeviceCriteria();
		criteria.setStatus(DeviceStatus.CONNECTED);
		int count = (int) deviceService.count(criteria);
		int size = 0;
		int offset = 0;
		int limit = 50;
		logger.debug("{} connected devices", count);
		long elapsed = System.currentTimeMillis();
		while (offset < count) {
			List<Device> devices = deviceService.search(criteria, offset, limit);
			offset = offset + limit;
			for (Device device : devices) {
				for (Channel channel : device.getChannels()) {
					Feed feed = cassandraService.buildFeed(channel);
					if (feed.isActive()) {
						cassandraService.getRollup().writeRollUp(feed, device.getOwner(), null, true);
						size++;
					}
				}
			}
		}
		elapsed = System.currentTimeMillis() - elapsed;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String message = String.format("%d connected devices, %d feed processed in %s", count, size,
				sdf.format(new Date(elapsed)));
		logger.debug(message);
	}

	// public Feed createUpdateFeedFull(Channel chnl) {
	// Feed feed = buildFeed(chnl);
	// if (feed.isValid()) {
	// cassandraService.getFeeds().update(feed);
	// return feed;
	// }
	// return null;
	// }

	public void disconnect(String deviceSerial, boolean remove) throws BackendServiceException {
		Device entity = deviceService.findBySerial(deviceSerial);
		if (entity == null)
			throw new BackendServiceException(deviceSerial + " not found");
		deviceService.disconnect(entity, remove);

	}

	public void factoryReset(String deviceSerial, String activationKey) throws BackendServiceException {
		Device device = deviceService.findBySerial(deviceSerial);
		if (device == null)
			throw new BackendServiceException(deviceSerial + " not found");
		if (!activationKey.equals(device.getActivationKey()))
			throw new BackendServiceException(deviceSerial + " invalid activation key");
		if (device.getNetwork() != null)
			throw new BackendServiceException(
					deviceSerial + " already assigned to network " + device.getNetwork().getName());
		resetData(device);

		List<Device> slaves = deviceService.findSlaves(device);
		asyncLogger.debug("factoryReset {} found slaves {}", device.getSerial(), slaves.size());

		for (Device slave : slaves) {
			asyncLogger.debug("factoryReset {} resetData {}", device.getSerial(), slave.getSerial());
			resetData(slave);
			if (slave.isPublishing()) {
				mqttService.publishLastValues(slave.getSerial(), null, false);
				mqttService.publishOnlineStatus(slave.getSerial(), null, false);
			}
		}

		deviceService.removeSlaves(device);
		device.setLabel(deviceSerial);
		device.setProfiles(new HashSet<ModbusProfile>());
		device.getHistory().setOwner(device.getOwner());
		device.getHistory().setStatus(device.getStatus());
		device.setOwner(Constants.ROLE_PRODUCTION.toLowerCase());
		device.setStatus(DeviceStatus.PRODUCED);
		device.setChannels(new HashSet<Channel>());
		device.setConfigurationDate(null);
		device.setCheckSum(null);
		device.setPublishing(false);
		device.setTracing(true);
		deviceService.update(device);

		if (device.isPublishing()) {
			mqttService.publishLastValues(device.getSerial(), null, device.isPublishing());
			mqttService.publishOnlineStatus(device.getSerial(), null, device.isPublishing());
		}

		deviceService.trace(device, TracingAction.DEVICE_RESET, "", null, null);

	}

	public void beforeDeviceRemoval(Device device) {
		Device master = null;
		deviceService.disconnect(device, true);
		resetData(device);

		List<Device> slaves = deviceService.findSlaves(device);
		for (Device slave : slaves) {
			resetData(slave);
		}
		deviceService.removeSlaves(device);
		if (device.getMaster() != null) {
			master = device.getMaster();
		} else if (!device.getProfiles().isEmpty()) {
			master = device;
		}
		if (master != null) {
			ProvisionedEvent provisioned = new ProvisionedEvent(master, null);
			try {
				mqttService.provisioned(provisioned);
			} catch (Exception e) {
				logger.error("beforeDeviceRemoval", e);
			}
		}
		deviceService.trace(device, TracingAction.DEVICE_REMOVE, "", null, null);
	}

	/**
	 * remove async device's data in cassandra
	 * 
	 * @param device
	 */
	@Async
	public void resetData(Device device) {
		asyncLogger.debug("start reset data {}", device.getSerial());
		this.notificationService.deviceChanged(device, TracingAction.DEVICE_RESET);
		this.resetDataSync(device.getSerial(), device.feedKeys());
		asyncLogger.debug("end reset data {}", device.getSerial());
	}

	private void resetDataSync(String sn, List<String> keys) {
		// asyncLogger.debug("start reset data {}", sn);
		if (keys == null) {
			keys = new ArrayList<>();
			List<Feed> feeds = cassandraService.getFeeds().getFeedsValues(sn, null);
			for (Feed feed : feeds) {
				keys.add(feed.getKey());
			}
		}

		cassandraService.getFeeds().delete(sn);
		// asyncLogger.debug("removed feeds data {}", sn);

		cassandraService.getRegistry().deleteAllConfigurationRegistry(sn);
		// asyncLogger.debug("removed registry data {}", sn);

		cassandraService.getRollup().resetRollUpStats(sn);
		// asyncLogger.debug("removed stats data {}", sn);

		cassandraService.getAlarms().deleteAlarms(sn);
		// asyncLogger.debug("removed alarm config data {}", sn);

		cassandraService.getAlarms().deleteThresholds(sn);
		// asyncLogger.debug("removed alarm thresholds data {}", sn);

		cassandraService.getAlarms().deleteEvents(sn);
		// asyncLogger.debug("removed alarm event data {}", sn);

		cassandraService.getMeasures().deleteMeasureTicks(sn);
		// asyncLogger.debug("removed measure ticks data {}", sn);
		
		cassandraService.getMeasures().deleteMeasuresSet(sn);
		
		if (keys != null) {
			for (String key : keys) {
				FeedKey feedKey = new FeedKey(sn, key);
				cassandraService.getRollup().deleteRollUp(feedKey);
				// asyncLogger.debug("removed rollup data {}", key);
			}
			for (String key : keys) {
				cassandraService.getMeasures().deleteMeasures(key);
				// asyncLogger.debug("removed measures data {}", key);
			}
		}
		// asyncLogger.debug("end reset data {}", sn);
	}

//	private void scheduleAlarmJob(String serial, List<MeasureRaw> measures) {
//		if (measures.isEmpty()) {
//			return;
//		}
//		Trigger trigger = newTrigger().withIdentity(serial + "-alarms", "alarms").startNow().build();
//		JobDataMap jobDataMap = trigger.getJobDataMap();
//		jobDataMap.put(AlarmJob.SERIAL, serial);
//		jobDataMap.put(AlarmJob.MEASURES, measures);
//		JobKey jobKey = new JobKey(trigger.getKey().getName(), trigger.getKey().getGroup());
//		JobDetail jobDetail = newJob(AlarmJob.class).withIdentity(jobKey).build();
//		try {
//			if (checkExists(jobKey, trigger, 10)) {
//				return;
//			}
//			scheduler.scheduleJob(jobDetail, trigger);
//		} catch (SchedulerException e) {
//			if (e instanceof ObjectAlreadyExistsException) {
//				asyncLogger.debug(
//						"unable to start AlarmJob Job : '{}', because one already exists with this identification",
//						jobKey);
//			} else {
//				asyncLogger.error("scheduleAlarmJob - device: " + serial, e);
//			}
//		}
//	}

	public void resetRollup(String sn) {
		cassandraService.getRollup().resetRollUpStats(sn);
	}

	@Transactional
	public void manageExclusiveVisualizations(DeviceExclusiveVisualizationEvent event) {
		Device device = deviceService.findBySerial(event.getSerial());
		if (device != null && device.isAvailableForVisualization()) {
			List<NetworkGroup> groups = new ArrayList<NetworkGroup>();
			List<GroupWidget> widgets = groupWidgetService.findExclusiveVisualizations(event.getSerial());
			for (GroupWidget widget : widgets) {
				NetworkGroup exclusiveGroup = null;
				logger.debug("manage visualization {} from device {}", widget.getExternalId(), widget.getDevice());
				if (device.getNetwork() != null) {
					exclusiveGroup = groupWidgetService.getExclusiveGroup(widget, device.getNetwork());
					groups.add(exclusiveGroup);
				}
				if (widget.getDevice().equals(widget.getExternalId())) {
					// groupWidgetService.manageDefaultVisualization(device.getSerial(),
					// null, null);
				} else {
					if (exclusiveGroup != null) {
						widget.setGroup(exclusiveGroup);
					}
					List<GraphicFeed> feeds = groupWidgetService.updateWithMaterializeFeeds(widget);
					for (GraphicFeed feed : feeds) {
						logger.debug("materialized feed {} from device {}", feed, widget.getDevice());
					}
				}
				if (device.getNetwork() != null) {
					for (NetworkGroup group : groups) {
						if (!device.getGroups().contains(group)) {
							device.addGroup(group);
						}
					}
					try {
						deviceService.update(device);
					} catch (BackendServiceException e) {
						deviceService.trace(device, TracingAction.ERROR_BACKEND, Utils.stackTrace(e), null, null);
					}
				}
			}
		}
	}

	public void manageConfigurationReset(Device device) throws BackendServiceException {
		if (!device.getChannels().isEmpty()) {
			int active = 0;
			int inactive = 0;
			for (Channel channel : device.getChannels()) {
				if (channel.getConfiguration().isActive()) {
					active++;
				} else {
					inactive++;
				}
				channel.deActivateChannel(new Date());
			}

			deviceService.update(device);
			// this.updateDataSinkOnConfig(device, new Date());
			this.cassandraService.getFeeds().updateLastContact(device.getSerial(), new Date());
			String message = String.format("before reset: active %d, inactive %d", active, inactive);
			deviceService.trace(device, TracingAction.DEVICE_CONFIGURATION_RESET, message, null, null);
		}
		List<Device> slaves = deviceService.findSlaves(device);
		for (Device slave : slaves) {
			manageConfigurationReset(slave);
		}

	}

	public void writeProvisioning(Device master, List<ModbusProvisioning> sources) throws BackendServiceException {
		Map<String, String> mapSlaves = new HashMap<>();
		List<String> originalProfiles = new ArrayList<>();
		Map<String, GroupWidget> mapWidgets = new HashMap<>();
		List<ModbusProfile> targets = new ArrayList<>();
		List<ModbusConfiguration> cfgs = new ArrayList<>();
		for (ModbusProvisioning source : sources) {
			ModbusProfile target = convertModbusProvisioning(source);
			if (!cfgs.contains(target.getConfiguration())) {
				targets.add(target);
				cfgs.add(target.getConfiguration());
			}
		}

		List<Device> slaves = deviceService.findSlaves(master);
		for (Device slave : slaves) {
			for (ModbusProfile profile : slave.getProfiles()) {
				mapSlaves.put(profile.getId(), slave.getSerial());
			}
		}
		ProvisioningEvent event = new ProvisioningEvent(master, targets, originalProfiles, mapSlaves, mapWidgets);
		provisioning(event);
	}

	public ModbusProvisioning convertModbusProfile(String deviceId, ModbusProfile entity, Locale locale) {
		ModbusProvisioning bean = new ModbusProvisioning();
		//
		bean.setProfileId(entity.getId());
		bean.setDeviceId(deviceId);
		bean.setSerialPortDataBits(entity.getConfiguration().getDataBits());
		bean.setSerialPortParity(entity.getConfiguration().getParity());
		bean.setSerialPortSpeed(entity.getConfiguration().getSpeed());
		bean.setSerialPortStopBits(entity.getConfiguration().getStopBits());
		bean.setSampleRate(entity.getConfiguration().getSampleRate());
		bean.setSlaveId(entity.getConfiguration().getSlaveID());
		bean.setSlaveName(entity.getConfiguration().getSlaveName());
		bean.setHost(entity.getConfiguration().getHost());
		bean.setPort(entity.getConfiguration().getPort());
		bean.setProtocol(entity.getConfiguration().getProtocol());

		//
		for (ModbusRegister register : entity.getRegisters()) {
			String label = profileService.translateDisplayName(register, locale);
			bean.getLabel().add(label);

			String priority = register.getPriority() != null ? register.getPriority().getShortName()
					: Priority.LOW.getShortName();
			String qualifier = register.getQualifier() != null ? register.getQualifier().getShortName()
					: it.thisone.iotter.enums.modbus.Qualifier.AVG.getShortName();

			Double max = register.getMax() != null ? register.getMax() : +32767d;
			Double min = register.getMin() != null ? register.getMin() : -32768d;
			Integer digits = register.getDecimalDigits() != null ? register.getDecimalDigits() : 0;
			Double deltaLogging = register.getDeltaLogging() != null ? register.getDeltaLogging() : 0;

			bean.getMax().add(max);
			bean.getMin().add(min);
			bean.getDecimalDigits().add(digits);
			bean.getDeltaLogging().add(deltaLogging);

			bean.getPriority().add(priority);
			bean.getQualifier().add(qualifier);
			bean.getFunctionCode().add(register.getFunctionCode().getShortName());
			bean.getPermission().add(register.getPermission().getShortName());
			bean.getTypeRead().add(register.getTypeRead().getShortName());
			bean.getTypeVar().add(register.getTypeVar().getShortName());
			bean.getActive().add(register.getActive());
			bean.getAddress().add(register.getAddress());
			bean.getOffset().add(register.getOffset());
			bean.getScaleMultiplier().add(register.getScaleMultiplier());
			bean.getUnit().add(register.getMeasureUnit());
			bean.getOid().add(register.getId());
			bean.getBitmask().add(register.getBitmask());

			if (register.getSigned() != null) {
				bean.getSigned().add(register.getSigned().getShortName());
			} else {
				bean.getSigned().add(Signed.NO.getShortName());
			}

			if (register.getFormat() != null) {
				bean.getFormat().add(register.getFormat().getShortName());
			} else {
				bean.getFormat().add(Format.BIT8.getShortName());
			}
		}

		return bean;
	}

	public ModbusProfile convertModbusProvisioning(ModbusProvisioning bean) {
		ModbusProfile entity = new ModbusProfile();

		if (bean.getProfileId() == null) {
			entity.setDisplayName("self-provisioned");
		} else {
			ModbusProfile profile = profileService.findOne(bean.getProfileId());
			if (profile != null) {
				entity.setDisplayName(profile.getDisplayName());
			}
		}

		/*
		 * bean.setProfileId(entity.getId()); bean.setDeviceId(deviceId);
		 * bean.setSerialPortDataBits(entity.getConfiguration().getDataBits());
		 * bean.setSerialPortParity(entity.getConfiguration().getParity());
		 * bean.setSerialPortSpeed(entity.getConfiguration().getSpeed());
		 * bean.setSerialPortStopBits(entity.getConfiguration().getStopBits());
		 * bean.setSampleRate(entity.getConfiguration().getSampleRate());
		 * bean.setSlaveId(entity.getConfiguration().getSlaveID());
		 * bean.setSlaveName(entity.getConfiguration().getSlaveName());
		 * bean.setHost(entity.getConfiguration().getHost());
		 * bean.setPort(entity.getConfiguration().getPort());
		 * bean.setProtocol(entity.getConfiguration().getProtocol());
		 */

		entity.getConfiguration().setSlaveID(bean.getSlaveId());
		entity.getConfiguration().setSlaveName(bean.getSlaveName());
		entity.getConfiguration().setSampleRate(bean.getSampleRate());

		for (int index = 0; index < bean.getAddress().size(); index++) {
			if (bean.getAddress().get(index) == null) {
				continue;
			}
			ModbusRegister register = new ModbusRegister();
			entity.addRegister(register);

			register.setCrucial(false);
			register.setPriority(Priority.LOW);
			register.setAddress(bean.getAddress().get(index));
			String address = String.format("%04d", register.getAddress());

			Boolean active = bean.getActive().get(index) != null ? bean.getActive().get(index) : false;
			String displayName = bean.getLabel().get(index) != null ? bean.getLabel().get(index) : address;
			Double max = bean.getMax().get(index) != null ? bean.getMax().get(index) : +32767d;
			Double min = bean.getMin().get(index) != null ? bean.getMin().get(index) : -32768d;
			Integer decimalDigits = bean.getDecimalDigits().get(index) != null ? bean.getDecimalDigits().get(index) : 0;
			Double deltaLogging = bean.getDeltaLogging().get(index) != null ? bean.getDeltaLogging().get(index) : 0d;
			Double offset = bean.getOffset().get(index) != null ? bean.getOffset().get(index) : 0d;
			Double scaleMultiplier = bean.getScaleMultiplier().get(index) != null ? bean.getScaleMultiplier().get(index)
					: 1d;
			Integer measureUnit = bean.getUnit().get(index) != null ? bean.getUnit().get(index) : BacNet.ADIM;
			String bitmask = bean.getBitmask().get(index) != null ? bean.getBitmask().get(index) : "";

			register.setActive(active);
			register.setDisplayName(displayName);
			register.setMax(max);
			register.setMin(min);
			register.setDecimalDigits(decimalDigits);
			register.setDeltaLogging(deltaLogging);
			register.setOffset(offset);
			register.setScaleMultiplier(scaleMultiplier);
			register.setMeasureUnit(measureUnit);
			register.setBitmask(bitmask);

			String metaData = bean.getOid().get(index) != null ? bean.getOid().get(index)
					: ModbusRegister.buildMetadata(index, register);
			register.setMetaData(metaData);

		}
		return entity;
	}

	public String getMetaData(String registerId) {
		return profileService.getRegisterMetadata(registerId);
	}

	@EventListener
	public void onModbusProfileMessage(ModbusProfileMessageEvent message) {
		ModbusProfile profile = profileService.findOne(message.getId());
		if (profile != null) {
			databaseMessageSource.reloadTemplate(profile.getTemplate());
		}
	}

	@EventListener
	public void onDeviceMessage(DeviceMessageEvent event) {
		if (event.getMessage().equalsIgnoreCase(TracingAction.DEVICE_UPDATE.name())) {
			logger.debug("evict cache for Device {} on update", event.getSerial());
			cacheManager.getCache(Constants.Cache.DEVICE).evict(event.getSerial());
			cacheManager.getCache(Constants.Cache.DATASINK).evict(event.getSerial());
		}
		if (event.getMessage().equalsIgnoreCase(TracingAction.ALARM.name())) {
			logger.debug("evict cache for datasink {} on alarm", event.getSerial());
			cacheManager.getCache(Constants.Cache.DATASINK).evict(event.getSerial());
		}
	}

	@EventListener
	public void onDeviceUpdate(DeviceUpdatedEvent event) {
		cacheManager.getCache(Constants.Cache.DEVICE).evict(event.getSerial());
		cacheManager.getCache(Constants.Cache.DATASINK).evict(event.getSerial());
		notificationService.deviceChanged(event.getOwner(), event.getNetworkId(), event.getSerial(),
				TracingAction.DEVICE_UPDATE);
	}

	@EventListener
	@Deprecated // Feature #2162
	public void onDeviceData(DeviceDataMessageEvent event) {
		DataResultSet data = mqttService.decodeLastValues(event.getTopic(), event.getMessage());
		if (data != null) {
			String serial = data.getSerial();
			long epoch = (System.currentTimeMillis() / 1000) - 600;
			boolean valid = data.getLastContact() > epoch;
			boolean publishing = false;
			Device device = deviceService.findDeviceCacheable(serial);
			if (device != null) {
				publishing = device.isPublishing();
			} else {
				valid = false;
			}
			if (valid && publishing) {
				cacheManager.getCache(Constants.Cache.DATAVALUES).put(serial, data);
			} else {
				if (!publishing) {
					mqttService.publishLastValues(serial, null, false);
				}
				cacheManager.getCache(Constants.Cache.DATAVALUES).evict(serial);
				asyncLogger.warn("onDeviceData {} received outdated message", serial);
			}
		}

	}

	public void cruft() {
		List<String> list = cassandraService.getFeeds().getDataSinkIds();
		for (String sn : list) {
			Device device = deviceService.findBySerialCached(sn);
			if (device == null) {
				this.resetDataSync(sn, null);
			}
		}
	}

	public void provisioned(ProvisionedEvent event) throws BackendServiceException {
		// Feature #1696
		cassandraService.getRollup().resetLockSink(event.getMaster().getSerial());
		mqttService.provisioned(event);
	}

	protected void updateDataSinkOnConfig(Device device) {
		Collection<Channel> channels = device.getChannels();
		DataSink sink = cassandraService.buildDataSink(device);
		List<Feed> feeds = new ArrayList<>();
		List<FeedAlarm> alarms = new ArrayList<>();
		List<FeedAlarmThresholds> thresholds = new ArrayList<>();
		List<ConfigurationRegistry> remotes = new ArrayList<>();
		boolean alarmed = !device.alarms().isEmpty();
		sink.setAlarmed(alarmed);

		for (Channel chnl : channels) {
			Feed feed = cassandraService.buildFeed(chnl);
			if (feed.isValid()) {
				feeds.add(feed);
			}
			ChannelAlarm alarm = chnl.getAlarm();
			if (alarm.isValid()) {
				// update unarmed alarm status to OFF
				if (!alarm.isArmed()) {
					FeedAlarm unarmed = new FeedAlarm(device.getSerial(), chnl.getKey());
					unarmed.setActive(false);
					unarmed.setDelayed(false);
					unarmed.setRepeated(false);
					unarmed.setStatus(AlarmStatus.OFF.name());
					alarms.add(unarmed);
				}
				// updates thresholds
				thresholds.add(this.alarms.buildFeedAlarmThresholds(chnl));
			}
			ChannelRemoteControl remote = chnl.getRemote();
			if (remote.isValid()) {
				remotes.add(cassandraService.buildConfigurationRegistry(chnl));
			}
		}
		try {
			cassandraService.getFeeds().upsert(sink);
			cassandraService.getFeeds().updateFeedBatch(feeds);
			cassandraService.getAlarms().updateThresholdsBatch(thresholds);
			cassandraService.getAlarms().updateAlarmsBatch(alarms);
			cassandraService.getRegistry().updateRegistryBatch(remotes);
			asyncLogger.debug("success updateDataSinkOnConfig serial {}", device.getSerial());
		} catch (RuntimeException e) {
			asyncLogger.error(String.format("failed updateDataSinkOnConfig %s", device.getSerial()), e);
		}
	}

	/*
	 * integrity checksum, populates last contact
	 */
	public List<Device> checkSumDevices() {
		DeviceCriteria criteria = new DeviceCriteria();
		criteria.setStatus(DeviceStatus.CONNECTED);
		List<Device> connected = deviceService.search(criteria, 0, 10000);
		criteria.setStatus(DeviceStatus.VERIFIED);
		List<Device> verified = deviceService.search(criteria, 0, 10000);

		List<Device> devices = Stream.concat(connected.stream(), verified.stream()).collect(Collectors.toList());
		for (Device device : devices) {
			checkSumDevice(device);
		}
		asyncLogger.info("Checked Connected Devices {}", devices.size());
		
//		long max = 0;
//		String serial = null;
//		for (Device device : devices) {
//			for (ModbusProfile profile : device.getProfiles()) {
//			    if (profile.getRegisters().size() > max) {
//			    	serial = device.getSerial();
//			    	max = profile.getRegisters().stream()
//		                      .filter(r -> Boolean.TRUE.equals(r.getActive()))
//		                      .count();;
//			    }
//			}
//		}
//		logger.debug("found {} max registers {}", serial, max);
		
		return devices;
	}

	public void checkSumDevice(Device device) {
		boolean needUpdate = false;
		if (device.getCheckSum() == null) {
			device.setCheckSum(device.calculateCheckSum());
			needUpdate = true;
		}
		boolean alarmed = !device.alarms().isEmpty();
		if (alarmed != device.isAlarmed()) {
			device.setAlarmed(alarmed);
			needUpdate = true;
		}

		try {
			// eventually creates datasink
			if (needUpdate) {
				// avoid conflict using fresh instance
				Device entity = deviceService.findOne(device.getId());
				if (entity.getConsistencyVersion() != device.getConsistencyVersion()) {
					asyncLogger.warn("Checked Connected Device {} found conflict in consistency version",
							device.getSerial());
				}
				entity.setAlarmed(device.isAlarmed());
				entity.setCheckSum(device.getCheckSum());
				deviceService.update(entity);
				this.updateDataSinkOnConfig(entity);
			}
		} catch (Throwable e) {
			asyncLogger.error(String.format("failed checkSumDevice %s", device.getSerial()), e);
		}

		DataSink sink = cassandraService.getFeeds().getDataSink(device.getSerial());
		if (sink != null) {
			device.setLastContactDate(sink.getLastContact());
		}
		cacheManager.getCache(Constants.Cache.DATASINK).evict(device.getSerial());

	}

	@Async
	@Transactional
	public void rebuildMasterDeviceAsync(String serial) {
		Device master = deviceService.findBySerial(serial);
		if (master.getMaster() == null) {
			List<Device> slaves = deviceService.findSlaves(master);
			for (Device slave : slaves) {
				rebuildSlaveDevice(slave.getSerial());
			}
		}
	}

	@Transactional
	public void rebuildSlaveDevice(String serial) {
		try {
			Device slave = deviceService.findBySerial(serial);
			if (slave != null && slave.getMaster() != null) {
				if (slave.getChannels().isEmpty()) {
					asyncLogger.info("{} rebuildSlaveDevice without params {}", serial, slave.getStatus());
					return;
				}
				ModbusProfile template = profileService.findCompatibleModbusProfile(slave);
				if (template != null) {
					profileService.rebuildSlaveProfile(slave, template);
					deviceService.update(slave);
					List<GroupWidget> widgets = groupWidgetService.findExclusiveVisualizations(slave.getSerial());
					for (GroupWidget widget : widgets) {
						groupWidgetService.delete(widget);
					}
					asyncLogger.info("{} rebuildSlaveDevice with profile {}", serial, template.getId());
				} else {
					asyncLogger.info("{} rebuildSlaveDevice profile NOT FOUND", serial);

				}

			}

		} catch (Throwable e) {
			asyncLogger.error(String.format("%s rebuildSlaveDevice failed", serial), e);
			// logger.error(String.format("%s rebuildSlaveDevice failed", serial), e);
		}

	}

	public String cacheStatistics(String mode) {
		
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("mode: %s \n", mode));

		try {

			/* get stats for all known caches */
			for (String cacheName : cacheManager.getCacheNames()) {

				EhCacheCache cache = (EhCacheCache) cacheManager.getCache(cacheName);

				Statistics stats = cache.getNativeCache().getStatistics();
				long count = stats.getObjectCount();
				
				if ("clear".equals(mode)) {
					cache.clear();
				}
				else {
					cache.getNativeCache().evictExpiredElements();
				}
				

				sb.append(String.format("%s: %s size \n", cacheName, count//
						 // nb element in heap tier
				));

			}

			sb.append(String.format("%s: %s size \n", "message bundles", //
					databaseMessageSource.clear()));

		} catch (Throwable e) {
			sb.append(e.getMessage());
		}

		return sb.toString();
	}

	@Transactional
	public void _fixSlaveRecovery(String serial) {
		Device slave = deviceService.findBySerial(serial);
		if (slave.getLabel().contains("RECOVERY") && slave.getDescription().isEmpty()) {
			rebuildSlaveDevice(serial);
//			for (ModbusProfile profile : slave.getProfiles()) {
//				slave.setDescription(profile.getDisplayName());
//			}
//			try {
//				deviceService.update(slave);
////				List<GroupWidget> widgets = groupWidgetService.findExclusiveVisualizations(serial);
////				for (GroupWidget widget : widgets) {
////					groupWidgetService.delete(widget);
////				}
//
//			} catch (BackendServiceException e) {
//			}			
		}

//
//		
//		Device slave = deviceService.findBySerial(serial);
//
//		
//		for (ModbusProfile source : slave.getProfiles()) {
//			GroupWidget groupWidget = new GroupWidget();
//			String label = String.format("%s-%d", slave.getSerial(),
//					source.getConfiguration().getSlaveID());
//			String description = String.format("%s-%s", source.getDisplayName(),
//					source.getConfiguration().getSlaveName());
//			String name = String.format("%s %s", label, description);						
//			GraphicWidget widget = new GraphicWidget();
//			widget.setLabel(source.getDisplayName());
//			widget.setType(GraphicWidgetType.CUSTOM);
//			widget.setProvider("AernetPro"); 
//			widget.getOptions().setScale(ChartScaleType.LINEAR);
//			widget.setDevice(slave.getSerial());
//			groupWidget.setName(name);
//			groupWidget.setCreator(slave.getOwner());
//			groupWidget.setOwner(slave.getOwner());
//			groupWidget.setExternalId(source.getId());
//			groupWidget.setDevice(slave.getSerial());
//			groupWidget.addGraphWidget(widget);		
//			groupWidget.getOptions().setRealTime(true);
//			groupWidget = deviceService.createConnectedWidget(groupWidget, slave.getNetwork());					
//		}

	}

}
