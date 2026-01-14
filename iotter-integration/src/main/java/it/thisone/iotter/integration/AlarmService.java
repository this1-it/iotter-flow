package it.thisone.iotter.integration;

import java.math.RoundingMode;
import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Service;

import it.thisone.iotter.cassandra.CassandraAlarms;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedAlarm;
import it.thisone.iotter.cassandra.model.FeedAlarmEvent;
import it.thisone.iotter.cassandra.model.FeedAlarmThresholds;
import it.thisone.iotter.cassandra.model.IFeedAlarm;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserCriteria;
import it.thisone.iotter.persistence.service.DatabaseMessageSource;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.util.BacNet;
import it.thisone.iotter.util.Utils;

@Service
public class AlarmService {
	private static Logger logger = LoggerFactory.getLogger(Constants.Notifications.LOG4J_CATEGORY);

	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss ZZZ";

	@Autowired
	private CassandraFeeds cassandraFeeds;

	@Autowired
	private CassandraAlarms cassandraAlarms;

	@Autowired
	private TracingService tracingService;

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private DeviceService deviceService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;

	@Autowired
	private DatabaseMessageSource messageSource;

	@Autowired
	private CacheManager cacheManager; 	

	public void registerAlarms(Device device) {
		boolean alarmed = false;
		for (Channel chnl : device.getChannels()) {
			if (!chnl.getAlarm().isEmpty()) {
				FeedAlarmThresholds target = buildFeedAlarmThresholds(chnl);
				cassandraAlarms.updateAlarmThresholds(target);
				Feed feed = new Feed(chnl.getDevice().getSerial(), chnl.getKey());
				feed.setAlarmed(chnl.getAlarm().isArmed());
				cassandraFeeds.updateOnAlarmArmed(feed);
				if (!target.isArmed()) {
					FeedAlarm alarm = cassandraAlarms.getAlarm(device.getSerial(), chnl.getKey());
					if (alarm != null) {
						alarm.setActive(false);
						alarm.setDelayed(false);
						alarm.setRepeated(false);
						alarm.setStatus(AlarmStatus.OFF.name());
						cassandraAlarms.updateAlarm(alarm);
					}
				}
			}
			if (chnl.getAlarm().isArmed()) {
				alarmed = true;
			}
		}
		// Bug #1650 OK
		device.setAlarmed(alarmed);
	}

	public boolean hasActiveAlarms(String serial) {
		return cassandraAlarms.countActiveAlarms(serial) > 0;
	}

	public void notifyAlarmReset(String serial, String operator) {
		List<FeedAlarm> alarms = cassandraAlarms.findActiveAlarms(serial);
		for (FeedAlarm alarm : alarms) {
			FeedAlarmEvent event = new FeedAlarmEvent(alarm.getSerial(), alarm.getKey(), alarm.getTimestamp(),
					new Date());
			event.setOperator(operator);
			event.setValue(alarm.getValue());
			event.setThreshold(alarm.getThreshold());
			event.setStatus(AlarmStatus.RESET.name());
			event.setNotify( true );
			try {
				cassandraAlarms.updateAlarmEvent(event);
				notifyAlarm(event);
			} catch (RuntimeException e) {
				logger.error(String.format("Processing Alarm Reset %s", event.toString()), e);
			}
		}
	}

	public void setUpFiredAlarms(Device entity) {
		List<String> entries = new ArrayList<String>();
		Map<String, Channel> channels = new HashMap<String, Channel>();
		for (Channel chnl : entity.getChannels()) {
			if (chnl.getAlarm().isArmed()) {
				channels.put(chnl.getKey(), chnl);
			}
		}
		List<FeedAlarm> alarms = cassandraAlarms.findActiveAlarms(entity.getSerial());
		for (IFeedAlarm alarm : alarms) {
			if (channels.containsKey(alarm.getKey())) {
				Channel channel = channels.get(alarm.getKey());
				// transient value
				channel.getAlarm().setFired(true);

				AlarmStatus status = AlarmStatus.valueOf(alarm.getStatus());
				switch (status) {
				case FIRE_UP:
					status = AlarmStatus.ON;
					break;
				case FIRE_DOWN:
					status = AlarmStatus.ON;
					break;
				default:
					break;
				}

				MeasureUnit measureUnit = channel.getDefaultMeasure();
				ChoiceFormat cf = enumChoiceFormat(channel.getMetaData());
				String measure = formatMeasure(alarm.getValue(), cf, measureUnit);
				String entry = String.format("[%s] %s,  %s", status.name(), channel.toString(), measure);
				entries.add(entry);
			}
		}
	}

	public void notifyDelayedAlarms() {
		Map<String, Device> devices = new HashMap<String, Device>();
		Map<String, Channel> channels = new HashMap<String, Channel>();
		Map<String, List<String>> admins = new HashMap<String, List<String>>();
		Map<String, List<String>> users = new HashMap<String, List<String>>();
		List<FeedAlarm> alarms = cassandraAlarms.retrieveDelayedAlarms();
		for (FeedAlarm alarm : alarms) {
			if (!devices.containsKey(alarm.getSerial())) {
				Device device = deviceService.findBySerialCached(alarm.getSerial());
				if (device == null) {
					continue;
				}
				for (Channel chnl : device.getChannels()) {
					if (chnl.getAlarm().isArmed()) {
						channels.put(chnl.getKey(), chnl);
					}
				}
				admins.put(device.getSerial(), findAdmins(device));
				users.put(device.getSerial(), findUsers(device));
			}
			if (channels.containsKey(alarm.getKey())) {
				FeedAlarmThresholds thresholds = cassandraAlarms.getAlarmThresholds(alarm.getSerial(), alarm.getKey());
				if (thresholds == null) {
					continue;
				}

				long delay = alarm.getTimestamp().getTime() + thresholds.getDelayMinutes() * 60 * 1000;
				if (delay < System.currentTimeMillis()) {
					alarm.setDelayed(false);
					
					Calendar now = Calendar.getInstance();
					now.set(Calendar.SECOND, 0);
					now.set(Calendar.MILLISECOND, 0);
					alarm.setUpdated(now.getTime());			
					
					cassandraAlarms.updateAlarm(alarm);
					FeedAlarmEvent event = cassandraAlarms.createAlarmEvent(alarm, thresholds.isNotify(), alarm.getTimestamp());
					if (thresholds.isNotify()) {
						Set<String> emails = new HashSet<String>();
						emails.addAll(users.get(alarm.getSerial()));
						emails.addAll(admins.get(alarm.getSerial()));
						notifyChannelAlarm(event, channels.get(alarm.getKey()), emails);
					}
				}
			}
		}
	}

	public void notifyRepeatedAlarms() {
		Map<String, Device> devices = new HashMap<String, Device>();
		Map<String, Channel> channels = new HashMap<String, Channel>();
		Map<String, List<String>> admins = new HashMap<String, List<String>>();
		Map<String, List<String>> users = new HashMap<String, List<String>>();
		List<FeedAlarm> alarms = cassandraAlarms.retrieveRepeatedAlarms();
		for (FeedAlarm alarm : alarms) {
			if (!alarm.isActive()) {
				continue;
			}
			
			if (!devices.containsKey(alarm.getSerial())) {
				Device device = deviceService.findBySerialCached(alarm.getSerial());
				if (device == null) {
					continue;
				}
				for (Channel chnl : device.getChannels()) {
					if (chnl.getAlarm().isArmed()) {
						channels.put(chnl.getKey(), chnl);
					}
				}
				admins.put(device.getSerial(), findAdmins(device));
				users.put(device.getSerial(), findUsers(device));
			}
			if (channels.containsKey(alarm.getKey())) {
				FeedAlarmThresholds thresholds = cassandraAlarms.getAlarmThresholds(alarm.getSerial(), alarm.getKey());
				if (thresholds == null) {
					continue;
				}
				
				// first notification
				FeedAlarmEvent event = cassandraAlarms.getFeedAlarmEvent(alarm.getSerial(), alarm.getKey(), alarm.getTimestamp());
				
				if (event == null) {
					continue;
				}
				if (!event.isNotify()) {
					continue;
				}

				long delay = alarm.getUpdated().getTime() + thresholds.getRepeatMinutes() * 60 * 1000;
				if (delay <= System.currentTimeMillis()) {
					Calendar now = Calendar.getInstance();
					now.set(Calendar.SECOND, 0);
					now.set(Calendar.MILLISECOND, 0);
					alarm.setUpdated(now.getTime());			
					cassandraAlarms.updateAlarm(alarm);
					event = cassandraAlarms.createAlarmEvent(alarm, thresholds.isNotify(), alarm.getTimestamp());
					if (thresholds.isNotify()) {
						Set<String> emails = new HashSet<String>();
						emails.addAll(users.get(alarm.getSerial()));
						emails.addAll(admins.get(alarm.getSerial()));
						notifyChannelAlarm(event, channels.get(alarm.getKey()), emails);
					}
				}
			}
		}
	}

	
	// Bug #1715
	private List<String> findUsers(Device device) {
		List<String> emails = new ArrayList<String>();
		NetworkGroup group = deviceService.getDeviceAlarmGroup(device);
		if (group != null) {
			List<User> users = userService.findByGroup(group);
			for (User user : users) {
				if (user.getAccountStatus().equals(AccountStatus.ACTIVE) ||user.getAccountStatus().equals(AccountStatus.HIDDEN)) {
					emails.add(user.getEmail());
				}
			}
		}
		return emails;
	}

	// Bug #1715
	private List<String> findAdmins(Device device) {
		Set<User> users = new HashSet<User>();
		List<String> emails = new ArrayList<String>();
		if (device.getNetwork() != null) {
			NetworkGroup defaultGroup = device.getNetwork().getDefaultGroup();
			UserCriteria criteria = new UserCriteria();
			criteria.setRole(Constants.ROLE_SUPERUSER);
			criteria.setGroup(defaultGroup.getId());
			criteria.setStatus(AccountStatus.ACTIVE);
			users.addAll(userService.search(criteria, 0, 100));
			// add owner
			users.add(userService.findByName(device.getOwner()));
			for (User user : users) {
				emails.add(user.getEmail());
			}
		}
		return emails;
	}

	private void notifyChannelAlarm(FeedAlarmEvent event, Channel channel, Collection<String> emails) {
		AlarmMessage alarm = alarmParams(event, channel);
		if (event.isNotify()) {
			if (!emails.isEmpty()) {
				event.setMembers(StringUtils.join(emails, ","));
				cassandraAlarms.updateAlarmEvent(event);
				String[] bcc = emails.toArray(new String[0]);
				notificationService.alarmNotification(bcc, channel.getAlarm().getPriority(), Locale.getDefault(),
						alarm);
			}
		}
		
		tracingService.traceAlarm(new Date(), channel.getOwner(), alarm.getNetwork(), alarm.getSerial(),
				alarm.toString());
	}

	private AlarmMessage alarmParams(FeedAlarmEvent event, Channel channel) {
		AlarmMessage alarm = new AlarmMessage();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String network = "";
		if (channel.getDevice().getNetwork() != null) {
			network = channel.getDevice().getNetwork().getName();
		}
		AlarmStatus status = AlarmStatus.valueOf(event.getStatus());
		MeasureUnit measureUnit = channel.getDefaultMeasure();
		ChoiceFormat cf = enumChoiceFormat(channel.getMetaData());
		String value = formatMeasure(event.getValue(), cf, measureUnit);
		String threshold = "";
		switch (status) {
		case FIRE_DOWN:
		case FIRE_UP:
			status = AlarmStatus.ON;
			threshold = formatMeasure(event.getThreshold(), cf, measureUnit);
			break;
		default:
			break;
		}
		Device device = channel.getDevice();
		alarm.setDevice(device.getLabel());
		alarm.setLabel(displayName(channel));
		alarm.setMembers(event.getMembers());
		alarm.setNetwork(network);
		alarm.setOperator(event.getOperator());
		alarm.setSerial(device.getSerial());
		alarm.setStatus(status.name());
		alarm.setThreshold(threshold);
		alarm.setTimestamp(sdf.format(event.getTimestamp()));
		alarm.setCreated(sdf.format(event.getCreated()));
		alarm.setValue(value);
		for (Channel chnl : device.getChannels()) {
			if (chnl.getConfiguration().isSelected()) {
				Feed feed = cassandraFeeds.getFeedValue(device.getSerial(), chnl.getKey());
				measureUnit = chnl.getDefaultMeasure();
				cf = enumChoiceFormat(chnl.getMetaData());
				Float measure = calculateMeasure(feed.getValue(), measureUnit);
				alarm.addMeasure(displayName(chnl), formatMeasure(measure, cf, measureUnit));
			}
		}
		return alarm;
	}

	private Float calculateMeasure(Float value, MeasureUnit measureUnit) {
		return measureUnit.convert(value);
	}

	private String formatMeasure(Float value, ChoiceFormat cf, MeasureUnit measureUnit) {
		if (value == null) {
			return "";
		}
		if (cf != null) {
			try {
				return cf.format(new Double(value));
			} catch (IllegalArgumentException e) {
			}
			return String.valueOf(value);
		}
		if (measureUnit == null) {
			return String.valueOf(value);
		}
		if (measureUnit.getFormat() == null || measureUnit.getFormat().isEmpty()) {
			return String.valueOf(value);
		}
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
		decimalFormat.applyPattern(measureUnit.getFormat());
		decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);
		decimalFormat.setDecimalSeparatorAlwaysShown(true);
		String label = deviceService.getUnitOfMeasureName(measureUnit.getType());
		return String.format("%s [%s]", decimalFormat.format(value), label);
	}

	public FeedAlarmEvent analizeAlarm(MeasureRaw measure, Feed feed, FeedAlarmThresholds thresholds) {
		return cassandraAlarms.analizeAlarmThresholds(measure, feed, thresholds);
	}

	public void notifyAlarm(FeedAlarmEvent event) {
		Device device = deviceService.findBySerialCached(event.getSerial());
		if (device == null)
			return;
		Channel channel = findChannel(device, event.getKey());
		if (channel == null)
			return;
		Set<String> emails = new HashSet<String>();
		emails.addAll(findAdmins(device));
		emails.addAll(findUsers(device));
		notifyChannelAlarm(event, channel, emails);
	}

	private Channel findChannel(Device device, String key) {
		for (Channel channel : device.getChannels()) {
			if (channel.getKey().equals(key)) {
				return channel;
			}
		}
		return null;
	}

	public List<Device> notifyInactivityAlarms(List<Device> connected) {
		List<Device> alarmed = new ArrayList<Device>();
		for (Device device : connected) {
			String sn = device.getSerial();
			// previously populated
			Date lastContactDate = device.getLastContactDate();
			if (lastContactDate == null) {
				lastContactDate = cassandraFeeds.getLastContact(sn);
			}
			if (lastContactDate != null) {
				if (analizeInactivity(device, lastContactDate)) {
					alarmed.add(device);
				}
			}
		}
		return alarmed;
	}

	public boolean analizeInactivity(Device device, Date lastContactDate) {
		boolean inactive = device.checkInactive(lastContactDate);
		boolean notify = device.getInactivityMinutes() > 0;
		FeedAlarmEvent event = cassandraAlarms.fireInactivityAlarm(device.getSerial(), 
				inactive,
				notify,
				lastContactDate);

		if (event != null ) {
			logger.debug("Activity alarm {} {} {}", device.getSerial(), device.getInactivityMinutes(), device.isInactive());
			notifyInactivityAlarm(device, event);
			return true;
		}
		return false;
	}

	private void notifyInactivityAlarm(Device device, FeedAlarmEvent event) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String message = "";
		AlarmStatus status = AlarmStatus.valueOf(event.getStatus());
		switch (status) {
		case ON:
			message = AlarmStatus.ONLINE.name() + "->" + AlarmStatus.OFFLINE.name();
			break;
		case REENTER:
			message = AlarmStatus.OFFLINE.name() + "->" + AlarmStatus.ONLINE.name();
			break;
		default:
			break;
		}
		String network = "";
		if (device.getNetwork() != null) {
			network = device.getNetwork().getName();
		}
		AlarmMessage alarm = new AlarmMessage();
		alarm.setDevice(device.getLabel());
		alarm.setSerial(device.getSerial());
		alarm.setMessage(message);
		alarm.setStatus(status.name());
		alarm.setNetwork(network);
		alarm.setTimestamp(sdf.format(event.getTimestamp()));
		alarm.setCreated(sdf.format(event.getCreated()));

		if (event.isNotify()) {
			Set<String> emails = new HashSet<String>();
			emails.addAll(findAdmins(device));
			emails.addAll(findUsers(device));
			if (!emails.isEmpty()) {
				event.setMembers(StringUtils.join(emails, ","));
				cassandraAlarms.updateAlarmEvent(event);
				alarm.setMembers(event.getMembers());
				String[] bcc = emails.toArray(new String[0]);
				notificationService.alarmInactivity(bcc, Priority.HIGH, Locale.getDefault(), alarm);
			}
		}
		cacheManager.getCache(Constants.Cache.DATASINK).evict(event.getSerial());
		notificationService.deviceChanged(device,TracingAction.ALARM);
		tracingService.traceAlarm(new Date(), device.getOwner(), network, device.getSerial(), alarm.toString());
	}

	private String displayName(Channel channel) {
		String label = channel.getConfiguration().getLabel();
		String bundleId = Utils.messageBundleId(channel.getMetaData());
		if (bundleId != null) {
			String msg = messageSource.getDatabaseMessage(bundleId, label, Locale.ENGLISH);
			if (msg != null && ! msg.isEmpty()) {
				label = msg;
			}
		}
		return label;
	}

	private ChoiceFormat enumChoiceFormat(String metadata) {
		String bundleId = Utils.messageBundleId(metadata);
		if (bundleId != null) {
			String code = bundleId + Constants.Provisioning.META_ENUM;
			String pattern = messageSource.getDatabaseMessage(code, Locale.ENGLISH);
			if (pattern == null) {
				return null;
			}
			if (pattern.trim().isEmpty()) {
				return null;
			}
			pattern = pattern.replaceAll("<", "");
			pattern = pattern.replaceAll("#", "");
			pattern = pattern.replaceAll("\u2264", "");
			pattern = pattern.replaceAll("=", "#");
			pattern = pattern.replaceAll(";", "|");

			try {
				return new ChoiceFormat(pattern);
			} catch (Throwable e) {
			}
		}
		return null;
	}

	
	public boolean processEvents(List<MeasureRaw> measures, Device device) {
		List<Feed> feeds = activeFeeds(device);
		List<FeedAlarmThresholds> thresholds = thresholds(device);
		boolean fired = false;
		for (MeasureRaw measure : measures) {
			Optional<Feed> feed = feeds.stream().filter(o -> measure.getKey().equals(o.getKey())).findFirst();
			Optional<FeedAlarmThresholds> threshold = thresholds.stream().filter(o -> measure.getKey().equals(o.getKey())).findFirst();
			if (feed.isPresent() && threshold.isPresent()) {
				FeedAlarmEvent event = new FeedAlarmEvent(device.getSerial(), measure.getKey(), measure.getDate(), measure.getReceived());
				try {
					logger.debug("Analize Alarm: {} {}", feed.get().getKey(), feed.get().getLabel());
					event = cassandraAlarms.analizeAlarmThresholds(measure, feed.get(), threshold.get());
					if (event != null && event.isNotify()) {
						fired = true;
						this.notifyAlarm(event);
						logger.debug("Notified Alarm {}", event);
					}
				} catch (RuntimeException e) {
					logger.error(String.format("Processing Alarm Event %s", event.toString()), e);
				}
			}
			else {
				logger.error("processEvents updating datasink error", measure.getKey());				
			}
		}
		return fired;

	}

	// // Bug #2035
	public void changedAlarms(Device device) {
		int count = cassandraAlarms.countActiveAlarms(device.getSerial());
		cassandraFeeds.updateDataSinkActiveAlarms(device.getSerial(), count > 0);
		cacheManager.getCache(Constants.Cache.DATASINK).evict(device.getSerial());
		notificationService.deviceChanged(device, TracingAction.ALARM);
	}

	public FeedAlarmThresholds buildFeedAlarmThresholds(Channel chnl) {
		FeedAlarmThresholds target = new FeedAlarmThresholds(chnl.getDevice().getSerial(), chnl.getKey());
		BeanUtils.copyProperties(chnl.getAlarm(), target, new String[] { "priority" });
		target.setPriority(chnl.getAlarm().getPriority().name());
		return target;
	}
	
	public List<FeedAlarmThresholds> thresholds(Device device) {
		return device.getChannels().stream().filter(o -> o.getAlarm().isArmed()).map(o -> buildFeedAlarmThresholds(o))
				.collect(Collectors.toList());
	}
	
	public List<Feed> activeFeeds(Device device) {
		return device.getChannels().stream().filter(o -> o.getConfiguration().isActive()).map(o -> buildFeed(o))
				.collect(Collectors.toList());
	}
	
	public Feed buildFeed(Channel chnl) {
		Feed feed = new Feed(chnl.getDevice().getSerial(), chnl.getKey());
		feed.setActive(chnl.getConfiguration().isActive());
		feed.setIdentifier(chnl.getNumber());
		feed.setSelected(chnl.getConfiguration().isSelected());
		feed.setLabel(chnl.getConfiguration().getLabel());
		feed.setQualifier(chnl.getConfiguration().getQualifier());
		feed.setUnit(BacNet.lookUp(chnl.getDefaultMeasure().getType()));
		feed.setOffset(chnl.getDefaultMeasure().getOffset());
		feed.setScale(chnl.getDefaultMeasure().getScale());
		feed.setAlarmed(chnl.getAlarm().isArmed());
		feed.setSince(chnl.getConfiguration().getActivationDate());
		feed.setTypeVar(Channel.getTypeVar(chnl.getMetaData()));
		return feed;
	}

	
}
