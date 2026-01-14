package it.thisone.iotter.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AlarmStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserCriteria;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.GroupWidgetService;
import it.thisone.iotter.persistence.service.ModbusProfileService;
import it.thisone.iotter.persistence.service.NetworkGroupService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.util.EncryptUtils;

// Feature #1696
@Service
public class RecoveryService {
	//public static Logger logger = LoggerFactory.getLogger(Constants.Migration.LOG4J_CATEGORY);
	public static Logger logger = LoggerFactory.getLogger(RecoveryService.class);
	@Autowired
	private UserService userService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private NetworkService networkService;

	@Autowired
	private NetworkGroupService groupService;

	@Autowired
	private GroupWidgetService groupWidgetService;

	@Autowired
	private ModbusProfileService profileService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private CassandraService cassandraService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private NotificationService notificationService;

	/*
	 * Create missing slave during device configuration
	 */

	public Device createRecoverySlave(String slaveSerial, String serial) {

		Device master = deviceService.findBySerial(serial);
		if (master != null) {
			Device slave = new Device();
			slave.setSerial(slaveSerial);
			slave.setLabel(slaveSerial + " RECOVERY");
			slave.setDescription("");
			slave.setOwner(master.getOwner());
			slave.setMaster(master);
			slave.setPublishing(master.isPublishing());
			slave.setTracing(master.isTracing());
			slave.setInactivityMinutes(Constants.Provisioning.INACTIVITY_MINUTES);
			slave.setProductionDate(new Date());
			slave.setWriteApikey(EncryptUtils.createWriteApiKey(slaveSerial));
			if (master.getNetwork() != null) {
				slave.addGroup(master.getNetwork().getDefaultGroup());
			}
			try {
				deviceService.create(slave);
			} catch (RuntimeException e) {
			}
			return slave;
		}

		return null;
	}

	@Transactional
	public int aernetpro(String serial) {
		Device slave = deviceService.findBySerial(serial);
		if (slave == null) {
			return 1;
		}
		if (!slave.getStatus().equals(DeviceStatus.CONNECTED)) {
			return 2;
		}
		
		Set<Channel> channels = slave.getChannels();
		if (slave.getProfiles().isEmpty()) {
			return 2;
		}

		ModbusProfile profile = slave.getProfiles().iterator().next();
		List<ModbusRegister> registers = profile.getRegisters();
		Integer slaveId = profile.getConfiguration().getSlaveID();

		GroupWidget groupWidget = groupWidgetService.findByExternalId(profile.getId(), serial);
		if (groupWidget == null || groupWidget.getWidgets().isEmpty()) {
			return 3;
		}

		boolean missing = false;
		for (Channel chnl : channels) {
			if (chnl.getMetaIdentifier() == null || chnl.getOid() == null) {
				logger.error("serial {} channel {} missing register metadata", serial, chnl.getNumber() );
				missing=true;
			}
		}

		if (!missing) {
			return 4;
		}
		
		Map<String, ModbusRegister> mapReg = new HashMap<String, ModbusRegister>();
		Map<String, Channel> mapChnl = new HashMap<String, Channel>();
		for (ModbusRegister register : registers) {
			String number = String.format("%s:%s:%s", slaveId.toString(), register.getAddress().toString(),
					register.getTypeRead().getShortName().toLowerCase());
			Optional<Channel> match = channels.stream().filter(o -> o.getNumber().equals(number) && o.getConfiguration().isActive()).findFirst();
			if (match.isPresent()) {
				match.get().setOid(register.getId());
				match.get().setMetaData(register.getMetaData());
				// logger.error("channel meta '{}'", match.get().getMetaIdentifier() );
				mapChnl.put(match.get().getMetaIdentifier(), match.get());
				mapReg.put(match.get().getMetaIdentifier(), register);
			}
		}

		
		
		
		GraphicWidget widget = groupWidget.getWidgets().get(0);
		List<GraphicFeed> feeds = widget.getFeeds();
		



		for (GraphicFeed feed : feeds) {
			if (feed.getChannel() == null) {
				logger.error("serial {} feed {} missing channel", serial, feed.getMetaIdentifier() );
			}
			if (mapChnl.containsKey(feed.getMetaIdentifier())) {
				Channel channel = mapChnl.get(feed.getMetaIdentifier());
				feed.setChannel(channel);
				feed.setMetaData(channel.getMetaData());
				feed.setOid(mapReg.get(feed.getMetaIdentifier()).getId());
			}
		}

		try {
			deviceService.update(slave);
			groupWidgetService.update(groupWidget);
		} catch (BackendServiceException e) {
			logger.error("error {} {}", serial, e.getMessage());
			return -1;
		}

		return -1;

	}

	// Feature #1884
	@Async
	public void _fix_users_visualizations() {
		UserCriteria criteria = new UserCriteria();
		criteria.setRole(Constants.ROLE_ADMINISTRATOR);
		List<User> admins = userService.search(criteria, 0, 1000);
		for (User admin : admins) {
			List<Network> networks = networkService.findByOwner(admin.getOwner());
			for (Network network : networks) {
				List<User> users = userService.findByNetwork(network);
				// List<NetworkGroup> groups = groupService.findByNetwork(network);
				List<GroupWidget> widgets = groupWidgetService.findByNetwork(network);
				Set<NetworkGroup> exclusiveGroups = new HashSet<NetworkGroup>();
				Set<NetworkGroup> groups = new HashSet<NetworkGroup>();
				for (GroupWidget widget : widgets) {
					if (widget.getAssociatedDevices().size() > 0) {
						NetworkGroup group = widget.getGroup();
						if (group != null && group.getGroupType().equals(NetworkGroupType.GROUP_WIDGET)) {
							if (widget.isExclusive()) {
								exclusiveGroups.add(group);
							} else {
								groups.add(group);
							}
						}
					} else {
						groupWidgetService.deleteById(widget.getId());
					}
				}
				logger.info("admin:{}, network:{}, visualizations:{}, aernetpro:{}", admin.getOwner(), network.getName(), groups.size(), exclusiveGroups.size());
				for (User user : users) {
					for (NetworkGroup group : exclusiveGroups) {
						if (!user.hasGroup(group)) {
							userService.addUserToGroup(user, group);
							logger.info("{} added to aernetpro {}", user.getUsername(),  group.getName());
						}
					}
					if (user.hasRole(Constants.ROLE_SUPERUSER)) {
						for (NetworkGroup group : groups) {
							if (!user.hasGroup(group)) {
								userService.addUserToGroup(user, group);
								logger.info("{} added to visualization {}", user.getUsername(), group.getName());
							}
						}
					}

				}
			}
		}

	}
	
	
	// Feature #1884
	@Async
	public void fix_users_visualizations() {
		UserCriteria criteria = new UserCriteria();
		criteria.setRole(Constants.ROLE_ADMINISTRATOR);
		List<User> admins = userService.search(criteria, 0, 1000);
		for (User admin : admins) {
			List<Network> networks = networkService.findByOwner(admin.getOwner());
			for (Network network : networks) {
				List<User> users = userService.findByNetwork(network);

				for (User user : users) {
					List<String> groups = new ArrayList<String>();
					if (user.hasRole(Constants.ROLE_USER)) {
						for (NetworkGroup group : user.getGroups()) {
							if (group.getGroupType()!=null && group.getGroupType().equals(NetworkGroupType.GROUP_WIDGET) ) {
								groups.add(group.getId());
							}
						}
						logger.error("{}={}", user.getUsername(),  String.join(",", groups));						
					}
				}
			}
		}
	}
	

	@Async
	public void fix_alarmed() {
		List<Device> devices = deviceService.findAll();
		for (Device device : devices) {
			boolean alarmed = !device.alarms().isEmpty();
			if (alarmed != device.isAlarmed()) {
				logger.error("update DEVICE set ALARMED = {} where SERIAL = '{}';", alarmed, device.getSerial());
				try {
					device.setAlarmed(alarmed);
					deviceService.update(device);
					cacheManager.getCache(Constants.Cache.DEVICE).evict(device.getSerial());
					cacheManager.getCache(Constants.Cache.DATASINK).evict(device.getSerial());
				} catch (BackendServiceException e) {
				}
			}
		}
	}
	
	@Async
	public void fix_aernetpro() {
		List<Device> devices = deviceService.findAll();
		for (Device device : devices) {
			int result = aernetpro(device.getSerial());
			if (result < 0) {
				logger.error("{} {}", result, device.getSerial());
			}
		}
	}
	
	

	@Async
	public void all_connected() {
		DeviceCriteria criteria = new DeviceCriteria();
		criteria.setStatus(DeviceStatus.CONNECTED);
		List<Device> connected = deviceService.search(criteria, 0, 10000);
		for (Device device : connected) {
			cassandraService.getFeeds().updateLastContact(device.getSerial(), new Date());
			cacheManager.getCache(Constants.Cache.DATASINK).evict(device.getSerial());

		}
	}

	public void restore_user_visualizations(String username, List<String> groups) {
		try {
			userService.restoreUserVisualizations(username,groups);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fix_alarm_status(String owner) {
		Date now =  new Date();
		List<Device> devices = new ArrayList<Device>();
		if (owner!=null && !owner.isEmpty()) {
			devices = deviceService.findByOwner(owner);
		}
		else {
			devices = deviceService.findAll();
		}
		for (Device device : devices) {
			if (device.getStatus().equals(DeviceStatus.CONNECTED)) {
				//cassandraService.getFeeds().updateLastContact(device.getSerial(), now);
				String serial = device.getSerial();
				int count = cassandraService.getAlarms().countActiveAlarms(serial);
				cassandraService.getFeeds().updateDataSinkActiveAlarms(serial, count > 0);
				cacheManager.getCache(Constants.Cache.DATASINK).evict(device.getSerial());
				notificationService.deviceChanged(device, TracingAction.ALARM);
			}
			
		}
			
	
	}
	

}
