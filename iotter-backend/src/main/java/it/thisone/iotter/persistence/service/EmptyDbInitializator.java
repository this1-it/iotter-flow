package it.thisone.iotter.persistence.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.config.Initializator;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.ChartScaleType;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.GraphicWidgetType;
import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.enums.Protocol;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChartPlotOptions;
import it.thisone.iotter.persistence.model.ChartThreshold;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.GraphicWidget;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.MeasureSensorType;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.MeasureUnitType;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.GeoLocationUtil;

@Component
public class EmptyDbInitializator extends Initializator {
	private static Logger logger = LoggerFactory.getLogger(Constants.AsyncExecutor.LOG4J_CATEGORY);

	
	private static final String UNIT_OF_MEASURE_PROPERTIES = "unit-of-measure.properties";

	private static final String SENSORS_PROPERTIES = "sensor.properties";

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private NetworkService networkService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private NetworkGroupService groupService;

	@Autowired
	private DeviceModelService deviceModelService;

	@Autowired
	private GroupWidgetService groupWidgetService;

	@Autowired
	private MeasureUnitTypeService measureUnitTypeService;

	@Autowired
	private MeasureSensorTypeService measureSensorTypeService;

	@Autowired
	private ModbusProfileService modbusProfileService;


	@Override
	public void afterPropertiesSet() throws Exception {
		String clusterRole = System.getProperty(Constants.CLUSTER_ROLE, "master");
		boolean isMaster = clusterRole.trim().equalsIgnoreCase("master");

		if (isMaster) {
			logger.info("Initializing Database Default Contents");
			try {
				Properties props = bootstrapProperties();
				bootstrapModels(props);
				bootstrapUsers(props);
				bootstrapMeasureUnits();
				bootstrapMeasureSensors();
			} catch (Exception e) {
				logger.info("Failed database initialization", e);
				throw e;
			}
			// migration
			// fixChannelMetadata();
			// fixModbusProfiles();
			// removeGroupWidgetsFromDevice();
			// fixAutomaticGroupWidgets();
			// fixChannelUniqueKey();
		}
	}

	private void fixChannelMetadata() {
		List<Device> devices = deviceService.findAll();
		for (Device device : devices) {
			boolean fixed = false;
			for (Channel channel : device.getChannels()) {
				String meta = channel.getMetaData();
				
				if (meta != null) {

					String[] parts = StringUtils.split(meta, "|");
					if (parts.length > 2 ) {
						String oid = channel.getOid();
						String origin = parts[parts.length-1];
						if (oid.equals(origin)) {
							oid = parts[parts.length-2];
							ModbusRegister register = deviceService.getModbusRegister(oid);
							if (register != null) {
								fixed = true;
								channel.setOid(oid);
							}
							
						}
						
					}
				}
				
//				if (channel.getRemote() != null && channel.getRemote().getPermission() == null) {
//					String oid = channel.getOid();
//					if (oid == null) {
//						String meta = channel.getMetaData();
//						if (meta != null) {
//							String[] parts = StringUtils.split(meta, "|");
//							if (parts.length > 2 ) {
//								oid = parts[parts.length-2];
//							}
//						}
//					}
//					ModbusRegister register = deviceService.getModbusRegister(oid);
//					if (register != null) {
//						fixed = true;
//						channel.setOid(oid);
//						channel.getRemote().setPermission(register.getPermission().getShortName().toLowerCase());
//					}
//				}
				
				
				
//				if (channel.getRemote() != null && channel.getRemote().getPermission() == null) {
//					String oid = channel.getOid();
//					if (oid == null) {
//						String meta = channel.getMetaData();
//						if (meta != null) {
//							String[] parts = StringUtils.split(meta, "|");
//							if (parts.length > 2 ) {
//								oid = parts[parts.length-2];
//							}
//						}
//					}
//					ModbusRegister register = deviceService.getModbusRegister(oid);
//					if (register != null) {
//						fixed = true;
//						channel.setOid(oid);
//						channel.getRemote().setPermission(register.getPermission().getShortName().toLowerCase());
//					}
//				}
			}
			if (fixed) {
				try {
					deviceService.update(device);
				} catch (BackendServiceException e) {
				}
			}
		}
	}

	private void fixModbusProfiles() {
		List<ModbusProfile> profiles = modbusProfileService.findAll();
		for (ModbusProfile profile : profiles) {
			boolean fixed = false;
			for (ModbusRegister register : profile.getRegisters()) {
				String sectionId = register.getAdditionalProperties().get("aernetpro");
				if (sectionId != null) {
					if (sectionId.contains("reset")) {
						if (!register.getPermission().equals(Permission.WRITE)) {
							register.setPermission(Permission.WRITE);
							fixed = true;
						}
					}
				}
			}
			if (fixed) {
				modbusProfileService.update(profile);
			}
		}

	}

	private void removeGroupWidgetsFromDevice() {
		List<Device> devices = deviceService.findAll();
		for (Device device : devices) {
			if (device.getSerial() != null && device.getSerial().startsWith("_")) {
				List<GroupWidget> widgets = groupWidgetService.findExclusiveVisualizations(device.getSerial());
				for (GroupWidget widget : widgets) {
					groupWidgetService.delete(widget);
				}
				deviceService.delete(device);
			}
		}
	}

	// private void fixGroupWidgets() {
	// List<GroupWidget> widgets = groupWidgetService.findAll();
	// for (GroupWidget widget : widgets) {
	// if (widget.getDevice() == null && widget.getGroup() != null) {
	// if (!widget.getGroup().isExclusive()) {
	// NetworkGroup group = new NetworkGroup();
	// group.setName(widget.getName());
	// group.setExclusive(true);
	// group.setNetwork(widget.getGroup().getNetwork());
	// widget.setGroup(group);
	// groupWidgetService.update(widget);
	// }
	// }
	// }
	// }

	// private void fixAutomaticGroupWidgets() {
	// List<GroupWidget> widgets = groupWidgetService.findAll();
	// for (GroupWidget widget : widgets) {
	// if (widget.isExclusive() && widget.getGroup() != null) {
	// if (!widget.getGroup().isExclusive()) {
	// try {
	// NetworkGroup group = groupService.findByName(
	// widget.getDevice(), widget.getGroup().getNetwork());
	// if (group == null) {
	// group = new NetworkGroup();
	// group.setName(widget.getDevice());
	// group.setExclusive(true);
	// group.setNetwork(widget.getGroup().getNetwork());
	// }
	// widget.setGroup(group);
	// groupWidgetService.update(widget);
	// } catch (BackendServiceException e) {
	// }
	// } else {
	// if (widget.isAutomaticDefault()) {
	// widget.setName(widget.getDevice());
	// groupWidgetService.update(widget);
	// }
	// }
	// }
	// }
	// }

	// private void fixChannelUniqueKey() {
	// List<Device> devices = deviceService.findAll();
	// for (Device device : devices) {
	// boolean changed = false;
	// for (Channel channel : device.getChannels()) {
	// if (channel.getUniqueKey() == null
	// || channel.getUniqueKey().isEmpty()) {
	// changed = true;
	// channel.setUniqueKey(UUID.randomUUID().toString());
	// }
	// }
	// if (changed) {
	// deviceService.update(device);
	// }
	// }
	// }

	private Map<String, Role> bootstrapRoles() {
		Map<String, Role> map = new HashMap<String, Role>();
		for (int i = 0; i < Constants.ALL_ROLES.length; i++) {
			Role role = roleService.safeCreate(Constants.ALL_ROLES[i], "");
			assert role != null;
			map.put(Constants.ALL_ROLES[i], role);
		}
		return map;
	}

	private void bootstrapUsers(Properties props) {
		Map<String, Role> map = bootstrapRoles();
		try {
			String username = props.getProperty("supervisor.user");
			assert username != null;
			String password = props.getProperty("supervisor.pass");
			assert password != null;
			String email = props.getProperty("supervisor.email");
			assert email != null;
			Role role = map.get(Constants.ROLE_SUPERVISOR);
			String owner = username;
			userService.safeCreateUser(username, password, email, AccountStatus.ACTIVE, role.getName(), "", role, null, owner);
			userService.safeCreateUser(Constants.ROLE_FINANCE.toLowerCase(), password, "", AccountStatus.ACTIVE, Constants.ROLE_FINANCE, "",
					map.get(Constants.ROLE_FINANCE), null, owner);
			userService.safeCreateUser(Constants.ROLE_PRODUCTION.toLowerCase(), password, "", AccountStatus.ACTIVE, Constants.ROLE_PRODUCTION,
					"", map.get(Constants.ROLE_PRODUCTION), null, owner);

			int administrators = Integer.parseInt(props.getProperty("mock.administrators", "0"));

			createMockAdministrators(administrators, map, password);

		} catch (Exception e) {
			logger.error("bootstrapUsers", e);
		}

	}

	private void bootstrapMeasureUnits() {
		List<MeasureUnitType> types = measureUnitTypeService.findAll();
		if (!types.isEmpty()) {
			return;
		}
		try {
			String resourceName = System.getProperty(UNIT_OF_MEASURE_PROPERTIES, UNIT_OF_MEASURE_PROPERTIES);
			Resource resource = new ClassPathResource(resourceName);
			if (!resource.exists()) {
				resource = new ClassPathResource("unit-of-measure.default.properties");
			}
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			for (Entry<Object, Object> entry : properties.entrySet()) {
				try {
					MeasureUnitType entity = new MeasureUnitType((String) entry.getValue(),
							new Integer((String) entry.getKey()));
					measureUnitTypeService.create(entity);
				} catch (Exception e) {
					logger.error(entry.toString(), e);
				}
			}
		} catch (IOException e) {
			logger.error("bootstrapMeasureUnits", e);
		}
	}

	private void bootstrapMeasureSensors() {
		List<MeasureSensorType> types = measureSensorTypeService.findAll();
		if (!types.isEmpty()) {
			return;
		}
		try {
			String resourceName = System.getProperty(SENSORS_PROPERTIES, SENSORS_PROPERTIES);
			Resource resource = new ClassPathResource(resourceName);

			if (!resource.exists()) {
				resource = new ClassPathResource("sensor.default.properties");
			}
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			for (Entry<Object, Object> entry : properties.entrySet()) {
				try {
					MeasureSensorType entity = new MeasureSensorType((String) entry.getValue(),
							new Integer((String) entry.getKey()));
					measureSensorTypeService.create(entity);
				} catch (Exception e) {
					logger.error(entry.toString(), e);
				}
			}
		} catch (IOException e) {
			logger.error("bootstrapMeasureSensors", e);
		}
	}

	private void createMockAdministrators(int administrators, Map<String, Role> map, String password) {
		for (int i = 0; i < administrators; i++) {
			String administrator = Constants.ROLE_ADMINISTRATOR.toLowerCase() + i;
			String superuser = Constants.ROLE_SUPERUSER.toLowerCase() + i;
			String user = Constants.ROLE_USER.toLowerCase() + i;
			User entity = userService.safeCreateUser(administrator, password, "", AccountStatus.ACTIVE,Constants.ROLE_ADMINISTRATOR,
					administrator, map.get(Constants.ROLE_ADMINISTRATOR), null, administrator);
			entity.addRole(map.get(Constants.ROLE_DEMO));
			userService.update(entity);

			userService.safeCreateUser(superuser, password, "", AccountStatus.ACTIVE, Constants.ROLE_SUPERUSER, superuser,
					map.get(Constants.ROLE_SUPERUSER), null, administrator);
			userService.safeCreateUser(user, password, "", AccountStatus.ACTIVE, Constants.ROLE_USER, user, map.get(Constants.ROLE_USER),
					null, administrator);
			createMockNetworks(entity, 2);
		}

	}

	private void createMockNetworks(User user, int size) {
		List<DeviceModel> models = deviceModelService.findAll();
		String owner = user.getUsername();
		if (user.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			String index = owner.substring(owner.length() - 1);
			for (int i = 0; i < size; i++) {
				boolean defaultNetwork = false;
				String networkName = "Network " + i;
				if (i == 0) {
					networkName = Constants.DEFAULT_NETWORK;
					defaultNetwork = true;
				}
				try {
					safeCreateNetwork(index + i, networkName, owner, defaultNetwork, NetworkType.GEOGRAPHIC, models);
				} catch (BackendServiceException e) {
				}
			}
		}

	}

	private static List<MeasureUnit> createMockMeasureUnit() {
		List<MeasureUnit> values = new ArrayList<MeasureUnit>();
		// "m/s", "km/h", "mph", "knot"
		// values.add(new MeasureUnit(0,1f));
		// values.add(new MeasureUnit(1,3.6f));
		// values.add(new MeasureUnit(2,2.23694f));
		// values.add(new MeasureUnit(3,1.94384f));
		MeasureUnit measureunit = new MeasureUnit();
		measureunit.setType((int) (Math.random() * 63));
		// measureunit.setScale((float)Math.random());
		// measureunit.setOffset((float)Math.random());
		measureunit.setScale(1f);
		measureunit.setOffset(0f);
		measureunit.setFormat("###.##");
		values.add(measureunit);
		return values;
	}

	private Network safeCreateNetwork(String index, String name, String owner, boolean defaultNetwork, NetworkType type,
			List<DeviceModel> models) throws BackendServiceException {
		int max_groups = 1;
		int max_users = 5;
		int max_devices = 2;
		int max_params = 20;
		Network network = null;
		try {
			network = networkService.findByName(name, owner);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
			return null;
		}
		List<DeviceModel> routers = new ArrayList<DeviceModel>();
		List<DeviceModel> profiled = new ArrayList<DeviceModel>();
		List<DeviceModel> generic = new ArrayList<DeviceModel>();

		for (DeviceModel model : models) {
			switch (model.getProtocol()) {
			case NATIVE:
				routers.add(model);
				break;
			default:
				generic.add(model);
				break;
			}
		}

		Role role = roleService.findByName(Constants.ROLE_USER);
		if (role == null) {
			return null;
		}

		if (network == null) {
			List<NetworkGroup> groups = new ArrayList<NetworkGroup>();
			network = new Network(name, lorem.substring(0, 128));
			// network.setHeader(lorem.substring(0, 512));
			network.setDefaultNetwork(defaultNetwork);
			network.setOwner(owner);
			network.setNetworkType(type);
			network.setAnonymous(defaultNetwork);
			networkService.create(network);
			groups.add(network.getDefaultGroup());

			for (int i = 0; i < max_groups; i++) {
				NetworkGroup group = new NetworkGroup();
				group.setName("group" + i + RandomStringUtils.random(3, false, true));
				group.setOwner(owner);
				group.setNetwork(network);
				group.setDescription(lorem.substring(0, 128));
				groupService.create(group);
				groups.add(group);
			}

			for (int i = 0; i < max_users; i++) {
				String firstName = randomFirstName();
				String lastName = randomLastName();
				String username = firstName + "." + lastName + "." + index + "@" + owner + ".com";
				String password = RandomStringUtils.random(8, true, true);
				password = username;
				User user = userService.safeCreateUser(username.toLowerCase(), password, username.toLowerCase(), AccountStatus.ACTIVE,
						firstName, lastName, role, network.getDefaultGroup(), owner);
				user.setAccountStatus(randomAccountStatus());
				for (int j = 0; j < (int) (Math.random() * 3) + 1; j++) {
					user.addGroup(randomGroup(groups));
				}
				userService.update(user);
			}

			if (!generic.isEmpty()) {
				for (int i = 0; i < max_devices; i++) {
					safeCreateDevice("_" + index, owner, randomModel(generic), new HashSet<NetworkGroup>(groups),
							DeviceStatus.ACTIVATED, max_params);
				}
				for (NetworkGroup networkGroup : groups) {
					if (!networkGroup.isDefaultGroup()) {
						createMockGroupWidget(networkGroup);
					}
				}
			}

			network.setGroups(new HashSet<NetworkGroup>(groups));
			networkService.update(network);

		}
		return network;
	}

	private void createMockGroupWidget(NetworkGroup group) {
		GroupWidget groupWidget = new GroupWidget();
		groupWidget.setGroup(group);
		groupWidget.setName(group.getName() + " widget");
		groupWidget.setOwner(group.getOwner());

		int max_params = 4;
		float aspectRatio = 4 / 3f;
		int canvasHeight = (int) (1600 / aspectRatio);
		int canvasWidth = 1600;
		int margin = 1;
		int heigth = 550;
		int width = canvasWidth;

		for (int counter = 0; counter < 4; counter++) {
			Device device = randomDevice(deviceService.findByGroup(group));
			if (device != null) {
				GraphicWidget widget = new GraphicWidget();
				widget.setLabel("Chart-" + randomLabel(device.toString()));
				widget.setType(GraphicWidgetType.MULTI_TRACE);
				widget.getOptions().setScale(ChartScaleType.LINEAR);
				widget.getOptions().setShowGrid(true);
				widget.getOptions().setRealTime(false);
				widget.setOwner(device.getOwner());

				widget.setX(margin / (float) canvasWidth);
				widget.setY((margin + counter * (heigth + margin)) / (float) canvasHeight);

				widget.setWidth(width / (float) canvasWidth);
				widget.setHeight(heigth / (float) canvasHeight);

				for (Channel channel : device.getChannels()) {
					GraphicFeed feed = new GraphicFeed();
					feed.setChannel(channel);
					feed.setWidget(widget);
					ChartPlotOptions options = new ChartPlotOptions();
					options.setChartType("spline");
					feed.setOptions(options);
					feed.setMeasure(channel.getMeasures().iterator().next());
					feed.getThresholds().add(randomThreshold(feed, group.getOwner()));
					widget.addFeed(feed);

					if (widget.getFeeds().size() >= max_params) {
						break;
					}

				}
				groupWidget.addGraphWidget(widget);
			}
		}
		if (!groupWidget.getWidgets().isEmpty()) {
			groupWidgetService.create(groupWidget);
		}

	}

	private ChartThreshold randomThreshold(GraphicFeed feed, String owner) {
		ChartThreshold threshold = new ChartThreshold();
		threshold.setLabel(randomLabel("threshold"));
		threshold.setValue((float) Math.random());
		threshold.setOwner(owner);
		threshold.setFeed(feed);
		return threshold;
	}

	private Device safeCreateDevice(String index, String owner, DeviceModel model, Set<NetworkGroup> groups,
			DeviceStatus status, int size) {
		String serial = index + RandomStringUtils.random(8, false, true);
		Device entity = deviceService.findBySerial(serial);
		if (entity == null) {
			entity = new Device();
			entity.setSerial(serial);
			entity.setStatus(status);
			entity.setLabel(randomLabel("Dev"));
			entity.setOwner(owner);
			entity.setActivationKey(RandomStringUtils.random(8, true, true));
			entity.setModel(model);
			entity.setWriteApikey(EncryptUtils.createWriteApiKey(serial));
			entity.setReadApikey(new StringBuilder(entity.getWriteApikey()).reverse().toString());
			entity.setLocation(randomGeoLocation());
			if (status.equals(DeviceStatus.PRODUCED)) {
				entity.setOwner(Constants.ROLE_PRODUCTION.toLowerCase());
			}
			for (Channel channel : channels(size)) {
				entity.addChannel(channel);
			}
			if (groups != null && status.equals(DeviceStatus.ACTIVATED)) {
				entity.setGroups(groups);
			}
			deviceService.create(entity);
			groupWidgetService.manageDefaultVisualization(serial, null, null);

		}
		return entity;
	}

	private void bootstrapModels(Properties props) {
		String[] models = StringUtils.split(props.getProperty("models", ""), ";");
		for (int i = 0; i < models.length; i++) {
			String[] model = StringUtils.split(models[i], ":");
			if (model.length == 2) {
				try {
					DeviceModel entity = deviceModelService.findByName(model[0]);
					if (entity == null) {
						entity = new DeviceModel();
						entity.setName(model[0]);
						try {
							entity.setProtocol(Protocol.valueOf(model[1]));
						} catch (Throwable e) {
						}
						entity.setOwner(Constants.ROLE_PRODUCTION.toLowerCase());
						deviceModelService.create(entity);
					}
				} catch (BackendServiceException e1) {
				}
			}
		}
	}

	private static String randomLabel(String value) {
		return value + "-" + RandomStringUtils.random(3, false, true);
	}

	private NetworkGroup randomGroup(List<NetworkGroup> groups) {
		int index = (int) (Math.random() * groups.size());
		return groups.get(index);
	}

	private DeviceModel randomModel(List<DeviceModel> models) {
		int index = (int) (Math.random() * models.size());
		return models.get(index);
	}

	private Device randomDevice(List<Device> values) {
		if (values.isEmpty()) {
			return null;
		}
		int index = (int) (Math.random() * values.size());
		return values.get(index);
	}

	private static Protocol randomProtocol() {
		Protocol[] protocol = { Protocol.FTP, Protocol.HTTP };
		return protocol[(int) (Math.random() * protocol.length)];
	}

	private static GeoLocation randomGeoLocation() {
		GeoLocation[] locations = { //
				new GeoLocation(45.4153333f, 11.9520370f), // Noventa Padovana
				new GeoLocation(45.3893676f, 11.7894607f), // Selvazzano
				new GeoLocation(45.3662592f, 11.9859629f), // Saonara
				new GeoLocation(45.3484444f, 11.8682962f), // Albignasego
				new GeoLocation(45.4315470f, 11.9670400f), // Vigonza
				new GeoLocation(45.4095683f, 11.8765886f) // Padova
		};

		int i = (int) (Math.random() * locations.length);
		GeoLocation location = locations[i];
		int distance = (int) ((Math.random() * locations.length) + 1) * 1000;

		if ((i & 1) == 0) {
			// even...
			return GeoLocationUtil.addDistanceNorth((float) location.getLatitude(), (float) location.getLongitude(),
					distance);
		} else {
			// odd...
			return GeoLocationUtil.addDistanceWest((float) location.getLatitude(), (float) location.getLongitude(),
					distance);
		}

	}

	private static AccountStatus randomAccountStatus() {
		AccountStatus[] status = { AccountStatus.ACTIVE, AccountStatus.LOCKED, AccountStatus.EXPIRED };
		return status[(int) (Math.random() * status.length)];
	}

	private static List<Channel> channels(int size) {
		List<Channel> channels = new ArrayList<Channel>();
		// String[] type = { "Type 0", "Type 1", "Type 2", "Type 3" };
		// String[] unit = { "Unit 0", "Unit 1", "Unit 2", "Unit 3" };
		//
		// type[(int) (Math.random() * type.length)];
		// unit[(int) (Math.random() * unit.length)];

		for (int i = 0; i < size; i++) {
			Channel channel = new Channel();
			channel.setNumber("0" + i);
			channel.setUniqueKey(UUID.randomUUID().toString());
			channel.getConfiguration().setLabel(randomLabel("Par"));
			channel.getConfiguration().setHideNumber(true);
			channel.getConfiguration().setSubLabel(RandomStringUtils.random(2, false, true));
			channel.setMeasures(createMockMeasureUnit());
			channel.getConfiguration().setActivationDate(new Date());
			channels.add(channel);
		}
		return channels;

	}

	private static String randomFormat() {
		String[] names = { "###.##", "###.###", "###.####" };
		return names[(int) (Math.random() * names.length)];
	}

	private static String randomFirstName() {
		String[] names = { "Dave", "Mike", "Katherine", "Jonas", "Linus", "Bob", "Anne", "Minna", "Elisa", "George",
				"Mathias", "Pekka", "Fredrik", "Kate", "Teppo", "Kim", "Samatha", "Sam", "Linda", "Jo", "Sarah", "Ray",
				"Michael", "Steve" };
		return names[(int) (Math.random() * names.length)];
	}

	private static String randomLastName() {
		String[] names = { "Smith", "Lehtinen", "Chandler", "Hewlett", "Packard", "Jobs", "Buffet", "Reagan", "Carthy",
				"Wu", "Johnson", "Williams", "Jones", "Brown", "Davis", "Moore", "Wilson", "Taylor", "Anderson",
				"Jackson", "White", "Harris", "Martin", "King", "Lee", "Walker", "Wright", "Clark", "Robinson",
				"Garcia", "Thomas", "Hall", "Lopez", "Scott", "Adams", "Barker", "Morris", "Cook", "Rogers", "Rivera",
				"Gray", "Price", "Perry", "Powell", "Russell", "Diaz" };
		return names[(int) (Math.random() * names.length)];
	}

	private static final String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut ut massa eget erat dapibus sollicitudin. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Pellentesque a augue. Praesent non elit. Duis sapien dolor, cursus eget, pulvinar eget, eleifend a, est. Integer in nunc. Vivamus consequat ipsum id sapien. Duis eu elit vel libero posuere luctus. Aliquam ac turpis. Aenean vitae justo in sem iaculis pulvinar. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Aliquam sit amet mi. "
			+ "\n"
			+ "Aenean auctor, mi sit amet ultricies pulvinar, dui urna adipiscing odio, ut faucibus odio mauris eget justo. Mauris quis magna quis augue interdum porttitor. Sed interdum, tortor laoreet tincidunt ullamcorper, metus velit hendrerit nunc, id laoreet mauris arcu vitae est. Nulla nec nisl. Mauris orci nibh, tempor nec, sollicitudin ac, venenatis sed, lorem. Quisque dignissim tempus erat. Maecenas molestie, pede ac ultrices interdum, felis neque vulputate quam, in sodales felis odio quis mi. Aliquam massa pede, pharetra quis, tincidunt quis, fringilla at, mauris. Vestibulum a massa. Vestibulum luctus odio ut quam. Maecenas congue convallis diam. Cras urna arcu, vestibulum vitae, blandit ut, laoreet id, risus. Ut condimentum, arcu sit amet placerat blandit, augue nibh pretium nunc, in tempus sem dolor non leo. Etiam fringilla mauris a odio. Nunc lorem diam, interdum eget, lacinia in, scelerisque sit amet, purus. Nam ornare. "
			+ "\n"
			+ "Donec placerat dui ut orci. Phasellus quis lacus at nisl elementum cursus. Cras bibendum egestas nulla. Phasellus pulvinar ullamcorper odio. Etiam ipsum. Proin tincidunt. Aliquam aliquet. Etiam purus odio, commodo sed, feugiat volutpat, scelerisque molestie, velit. Aenean sed sem sit amet libero sodales ultrices. Donec dictum, arcu sed iaculis porttitor, est mauris pulvinar purus, sit amet porta purus neque in risus. Mauris libero. Maecenas rhoncus. Morbi quis nisl. "
			+ "\n"
			+ "Vestibulum laoreet tortor eu elit. Cras euismod nulla eu sapien. Sed imperdiet. Maecenas vel sapien. Nulla at purus eu diam auctor lobortis. Donec pede eros, lacinia tincidunt, tempus eu, molestie nec, velit. Nullam ipsum odio, euismod non, aliquet nec, consequat ac, felis. Duis fermentum mauris sed justo. Suspendisse potenti. Praesent at libero sit amet ipsum imperdiet fermentum. Aliquam enim nisl, dictum id, lacinia sit amet, elementum posuere, ipsum. Integer luctus dictum libero. Pellentesque sed pede sed nisl bibendum porttitor. Phasellus tempor interdum nisi. Mauris nec magna. Phasellus massa pede, vehicula sed, ornare at, ullamcorper ut, nisl. Sed turpis nisl, hendrerit sit amet, consequat id, auctor nec, arcu. Quisque fringilla tincidunt massa. In eleifend, nulla sed mollis vestibulum, mauris orci facilisis ante, id pharetra dolor ipsum vitae sem. Integer dictum. "
			+ "\n"
			+ "Nunc ut odio. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec mauris tellus, dapibus vel, hendrerit vel, sollicitudin ut, ligula. Ut justo metus, accumsan placerat, ultrices sit amet, congue at, nulla. Integer in quam. Cras sollicitudin mattis magna. Vestibulum neque eros, egestas ut, tincidunt vel, ullamcorper non, ligula. Vivamus eu lacus. Donec rhoncus metus et odio. Donec est. Nulla facilisi. Suspendisse potenti. Etiam tempor pede nec ante. Vestibulum adipiscing velit vel neque. "
			+ "\n"
			+ "Quisque ornare erat rhoncus lectus. Donec vitae ante at enim mollis egestas. Mauris convallis. Fusce convallis, nisl eu sagittis suscipit, risus ligula aliquam libero, in imperdiet neque mi non risus. Aenean dictum ultricies risus. Praesent ut ligula vitae purus ornare auctor. Cras tellus mauris, adipiscing ac, dignissim auctor, faucibus in, sem. Cras mauris libero, pharetra sit amet, lacinia eu, vehicula eleifend, sapien. Donec ac tellus. Sed eros dui, vulputate vel, auctor pharetra, tincidunt at, ipsum. Duis at dolor ac leo condimentum pulvinar. Donec molestie, dolor et fringilla elementum, nibh nibh iaculis orci, eu elementum odio turpis et odio. Phasellus fermentum, justo id placerat egestas, arcu nunc molestie ante, non imperdiet ligula lectus sed erat. Quisque sed ligula. Sed ac nulla. Nullam massa. "
			+ "\n"
			+ "Sed a purus. Mauris non nibh blandit neque cursus scelerisque. Quisque ultrices sem nec dolor. Donec non diam ut dui consequat venenatis. Nullam risus massa, egestas in, facilisis tristique, molestie sed, mi. Duis euismod turpis sit amet quam. Vestibulum ornare felis eget dolor. Phasellus ac urna vel sapien lacinia adipiscing. Donec egestas felis id mi. Sed erat. Vestibulum porta vulputate neque. Maecenas scelerisque, sem id sodales pretium, sem mauris rhoncus magna, at scelerisque tortor mauris nec dui. Nullam blandit rhoncus velit. Nam accumsan, enim id vestibulum feugiat, lorem nibh placerat urna, eget laoreet diam tortor at lorem. Suspendisse imperdiet consectetur dolor. ";

}
