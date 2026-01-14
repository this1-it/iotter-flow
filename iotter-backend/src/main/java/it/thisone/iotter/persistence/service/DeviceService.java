package it.thisone.iotter.persistence.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.EventBus;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.FtpType;
import it.thisone.iotter.enums.MeasureQualifier;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.eventbus.DeviceUpdatedEvent;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IChannelDao;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.ifc.IDeviceModelDao;
import it.thisone.iotter.persistence.ifc.IGroupWidgetDao;
import it.thisone.iotter.persistence.ifc.IMeasureSensorTypeDao;
import it.thisone.iotter.persistence.ifc.IMeasureUnitTypeDao;
import it.thisone.iotter.persistence.ifc.IModbusRegisterDao;
import it.thisone.iotter.persistence.ifc.INetworkDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.ifc.ITracingDao;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelAlarm;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.DeviceCriteria;
import it.thisone.iotter.persistence.model.DeviceModel;
import it.thisone.iotter.persistence.model.FtpAccess;
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
import it.thisone.iotter.util.BacNet;
import it.thisone.iotter.util.EncryptUtils;

@Service
public class DeviceService implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8053562725519220039L;

	public static Logger logger = LoggerFactory.getLogger(DeviceService.class);

	// @Autowired
	// private EventBus eventBus;

	@Autowired
	private INetworkDao networkDao;

	@Autowired
	private INetworkGroupDao groupDao;

	@Autowired
	private IDeviceDao deviceDao;

	@Autowired
	private IGroupWidgetDao groupWidgetDao;

	@Autowired
	private ITracingDao tracingDao;

	@Autowired
	private IDeviceModelDao deviceModelDao;

	@Autowired
	private IChannelDao channelDao;

	@Autowired
	private IMeasureUnitTypeDao measureUnitTypeDao;

	@Autowired
	private IMeasureSensorTypeDao measureSensorTypeDao;

	@Autowired
	private IModbusRegisterDao modbusRegisterDao;


	public DeviceService() {
		super();
	}
	
	@Cacheable(value = Constants.Cache.DEVICE, key="#serial", unless="#result == null")
	public Device findDeviceCacheable(String serial) {
		Device result = findBySerial(serial);
		if (result == null) {
			return null;
		}
		Device cached = new Device();
		BeanUtils.copyProperties(result, cached, new String[] { "profiles" });
		return cached;
	}

	// API
	@Cacheable(value = Constants.Cache.UNIT_OF_MEASURE, unless="#result == null")
	public String getUnitOfMeasureName(Integer code) {
		MeasureUnitType type = null;
		try {
			type = measureUnitTypeDao.findByCode(code);
		} catch (BackendServiceException e) {
		}
		return (type != null) ? type.getName() : String.valueOf(code);
	}

	@Cacheable(value = Constants.Cache.UNIT_OF_MEASURE_CODE, unless="#result == null")
	public MeasureUnitType getUnitOfMeasure(Integer code) {
		MeasureUnitType type = null;
		try {
			type = measureUnitTypeDao.findByCode(code);
		} catch (BackendServiceException e) {
		}
		return type;
	}

	@Transactional
	public MeasureUnitType lookUpBacnetCode(int code) {
		String value = BacNet.lookUp(code);
		if (value == null && code != BacNet.NOT_DEF)
			return null;
		MeasureUnitType type = getUnitOfMeasure(code);
		if (type == null) {
			type = new MeasureUnitType(value, code);
			measureUnitTypeDao.create(type);
		}
		return type;
	}

	@Deprecated
	public Device findBySerialCached(String serial) {
		return deviceDao.findBySerial(serial);
		//return deviceDao.findBySerialCached(serial);
	}

	public Device findBySerial(String serial) {
		return deviceDao.findBySerial(serial);
	}

	public Channel findActiveChannel(String serial, String channelNumber) {
		try {
			return deviceDao.findActiveChannel(serial, channelNumber);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;
	}

	public DeviceModel getDeviceModel(String name) {
		try {
			return deviceModelDao.findByName(name);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;
	}

	public List<DeviceModel> getDeviceModels() {
		return deviceModelDao.findAll();
	}

	@Transactional
	public void trace(Device entity, TracingAction action, String message, String user, String json) {
		if (entity.isTracing()) {
			if (json != null) {
				Logger logger = LoggerFactory.getLogger(Constants.REST.LOG4J_CATEGORY);
				logger.info("{} {} {}", action.name(), entity.getSerial(), StringUtils.replace(json, "\n", ""));
			}
			String network = null;
			if (entity.getNetwork() != null) {
				network = entity.getNetwork().getName();
			}
			tracingDao.trace(action, user, entity.getOwner(), network, entity.getSerial(), message);
		}
	}

	@Transactional
	public void create(Device entity) {
		deviceDao.create(entity);
	}

	@Transactional(rollbackFor = BackendServiceException.class)
	@CacheEvict(value = Constants.Cache.DEVICE, key="#entity.serial" )
	public void update(Device entity) throws BackendServiceException {
		try {
			deviceDao.update(entity);
			this.fireUpdatedEvent(entity);
		} catch (RuntimeException cause) {
			deviceDao.evictFromJpaCache(entity);
			throw new BackendServiceException(cause);
		}
	}
	
	public void fireUpdatedEvent(Device entity) {
		String owner = entity.getOwner();
		String networkId = entity.getNetwork() != null ? entity.getNetwork().getId() : "";
		String serial = entity.getSerial();
		//eventBus.post(new DeviceUpdatedEvent(serial, owner, networkId));
	}

	public Device findOne(String id) {
		return deviceDao.findOne(id);
	}

	public List<Device> findAll() {
		return deviceDao.findAll();
	}

	public List<Device> findAllFtpAccessActive() {
		return deviceDao.findAllFtpAccessActive();
	}

	public List<Device> findByOwner(String owner) {
		return deviceDao.findByOwner(owner);
	}

	@Transactional
	public void delete(Device entity) {
		FtpAccess ftpAccess = entity.getFtpAccess();
		if (ftpAccess != null && ftpAccess.getType() == FtpType.LOCAL) {
			// ftpDao.deleteUser(entity.getId());
		}
		removeSlaves(entity);
		removeReferences(entity, true);
		deviceDao.deleteById(entity.getId());
		this.fireUpdatedEvent(entity);
	}

	@Transactional
	public void removeSlaves(Device entity) {
		List<Device> slaves = deviceDao.findSlaves(entity);
		for (Device slave : slaves) {
			removeReferences(slave, true);
			deviceDao.deleteById(slave.getId());
			this.fireUpdatedEvent(slave);
		}
	}

	@Transactional
	public Device deleteChannels(Device entity, List<Channel> channels) {
		for (Channel channel : channels) {
			removeReferences(channel);
			channelDao.deleteById(channel.getId());
		}
		return deviceDao.findOne(entity.getId());
	}

	/**
	 * manage automatic visualizations
	 * 
	 * @param entity
	 * @param network
	 * @return
	 */
	@Transactional
	public boolean connect(Device entity, Network network) {
		if (entity == null)
			return false;
		if (network == null)
			return false;
		entity = deviceDao.findOne(entity.getId());
		entity.setGroups(new HashSet<NetworkGroup>());
		connectReferences(entity, network);
		entity.addGroup(network.getDefaultGroup());
		deviceDao.update(entity);
		deviceDao.merge(entity);
		List<Device> slaves = deviceDao.findSlaves(entity);
		for (Device slave : slaves) {
			slave.setGroups(new HashSet<NetworkGroup>());
			connectReferences(slave, network);
			slave.addGroup(network.getDefaultGroup());
			deviceDao.update(slave);
			deviceDao.merge(slave);
		}

		return true;
	}

	private void connectReferences(Device entity, Network network) {
		if (entity == null)
			return;
		if (network == null)
			return;
		for (GroupWidget widget : groupWidgetDao.findExclusiveVisualizations(entity.getSerial())) {
			if (widget.getGroup() == null) {
				NetworkGroup group = new NetworkGroup();
				group.setName(widget.getExternalId());
				group.setExternalId(widget.getExternalId());
				group.setExclusive(true);
				group.setGroupType(NetworkGroupType.GROUP_WIDGET);
				group.setNetwork(network);
				groupDao.create(group);
				group = groupDao.merge(group);
				widget.setGroup(group);
				groupWidgetDao.update(widget);
				groupWidgetDao.merge(widget);
				entity.addGroup(group);
			}
		}
	}

	@Transactional
	public GroupWidget createConnectedWidget(GroupWidget widget, Network network) {
		if (network != null) {
			NetworkGroup group = new NetworkGroup();
			group.setName(widget.getExternalId());
			group.setExternalId(widget.getExternalId());
			group.setExclusive(true);
			group.setGroupType(NetworkGroupType.GROUP_WIDGET);
			group.setNetwork(network);
			groupDao.create(group);
			group = groupDao.merge(group);
			widget.setGroup(group);
		}
		Device device = deviceDao.findBySerial(widget.getDevice());
		if (device != null) {
			Map<String, Channel> map = new HashMap<String, Channel>();
			for (Channel chnl : device.getChannels()) {
				if (chnl.getMetaData() != null) {
					map.put(chnl.getMetaData(), chnl);
				}
			}
			for (GraphicWidget gwidget : widget.getWidgets()) {
				for (GraphicFeed feed : gwidget.getFeeds()) {
					if (feed.getChannel() == null) {
						Channel chnl = map.get(feed.getMetaData());
						if (chnl != null) {
							feed.setChannel(chnl);
							feed.setMeasure(chnl.getDefaultMeasure());
						}
					}
				}
			}
		}
		groupWidgetDao.create(widget);
		return widget;
	}

	@Transactional
	public void disconnect(Device entity, boolean remove) {
		if (entity == null)
			return;
		removeReferences(entity, remove);
		entity = deviceDao.findOne(entity.getId());
		entity.setGroups(new HashSet<NetworkGroup>());
		deviceDao.update(entity);

		List<Device> slaves = deviceDao.findSlaves(entity);
		for (Device slave : slaves) {
			disconnect(slave, remove);
		}

	}

	// Bug #1959
	private void removeReferences(Device entity, boolean remove) {
		
		// b10xqq1601R1-2-49303
		if (entity == null)
			return;
		// finds all graph feeds which have channels belonging to a device
		//List<GraphicFeed> feeds = groupWidgetDao.findGraphFeedByDevice(entity);
		
		Set<GroupWidget> widgets = new HashSet<GroupWidget>();
		Set<NetworkGroup> groups = new HashSet<NetworkGroup>();
		List<GroupWidget> orphans = new ArrayList<GroupWidget>();
		List<GroupWidget> allwidgets = groupWidgetDao.findByDevice(entity);
		for (GroupWidget widget : allwidgets) {
			if (widget.getAssociatedDevices().size() <= 1 ) {
				widgets.add(widget);
				if (widget.getGroup() != null) {
					groups.add(widget.getGroup());
				}
			}
			else {
				orphans.add(widget);
			}			
		}



		// removed groups before removing exclusive visualizations
		NetworkGroup exclusive = null;
		try {
			exclusive = groupDao.findByExternalId(entity.getSerial(), entity.getNetwork());
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
			return;
		}

		if (exclusive != null) {
			groups.add(exclusive);
		}

		NetworkGroup alarms = getDeviceAlarmGroup(entity);
		if (alarms != null) {
			groups.add(alarms);
		}

		// remove members from dedicated groups: users, devices, groupwidgets
		for (NetworkGroup group : groups) {
			groupDao.removeMembers(group);
			groupDao.deleteById(group.getId());
		}

		if (remove) {
			// remove exclusive visualizations
			for (GroupWidget widget : widgets) {
				logger.debug("remove exclusive visualizations {} {} ", widget.getDevice(), widget.getName());
				groupWidgetDao.deleteById(widget.getId());
			}
		}


		// remove feeds that use some device parameters
		for (GroupWidget widget : orphans) {
			for (GraphicWidget graphWidget : widget.getWidgets()) {
				for (Iterator<GraphicFeed> iterator = graphWidget.getFeeds().iterator(); iterator.hasNext();) {
					GraphicFeed feed = iterator.next();
				    if (feed.getChannel() != null && feed.getChannel().getDevice().equals(entity)) {
				        iterator.remove();
				    }
				}
			}
			// update visualizations
			groupWidgetDao.update(widget);
		}

	}

	private void removeReferences(Channel entity) {
		// finds all feeds which have been linked to channel
		List<GraphicFeed> feeds = groupWidgetDao.findGraphFeedByChannel(entity);
		Set<GroupWidget> grps = new HashSet<GroupWidget>();
		// remove feeds
		for (GraphicFeed feed : feeds) {
			GroupWidget grp = feed.getWidget().getGroupWidget();
			if (grp != null) {
				grps.add(grp);
				for (GraphicWidget graphWidget : grp.getWidgets()) {
					logger.debug("removed feed {} from groupwidget {}", feed.getKey(), grp.getName());
					graphWidget.getFeeds().remove(feed);
					if (graphWidget.getFeeds().isEmpty()) {
						grp.getWidgets().remove(graphWidget);
					}
				}
			}
		}
		// update visualizations
		for (GroupWidget grp : grps) {
			groupWidgetDao.update(grp);
		}

	}

	public List<Device> findByGroup(NetworkGroup group) {
		return deviceDao.findByGroup(group);
	}

	public List<Device> findByNetwork(Network network) {
		return deviceDao.findByNetwork(network);
	}

	public List<Device> findByNetworkId(String networkId) {
		return deviceDao.findByNetwork(networkDao.findOne(networkId));
	}

	/**
	 * add a new channel checking if exist another with same id (number) new channel
	 * is added only if (uniqueKey number) is different existing channel is
	 * re-activated
	 * 
	 */
	public boolean manageChannelByNumber(Device device, Channel newChannel) {
		if (newChannel == null) {
			return false;
		}
		if (newChannel.getMeasures().isEmpty()) {
			return false;
		}
		if (newChannel.getId() != null) {
			return false;
		}
		if (newChannel.getNumber() == null || newChannel.getNumber().isEmpty()) {
			return false;
		}
		if (newChannel.getUniqueKey() == null || newChannel.getUniqueKey().isEmpty()) {
			return false;
		}

		// find if channel already exists
		Collection<Channel> channels = device.getChannels();
		Channel sameChannel = null;
		for (Channel channel : channels) {
			if (channel.getNumber().equals(newChannel.getNumber())) {
				if (channel.getUniqueKey().equals(newChannel.getUniqueKey())) {
					sameChannel = channel;
					migrateChannel(sameChannel, newChannel);
				} 
				// Feature #1885 Bug #2140
				else if (channel.getOid() != null && channel.getOid().equals(newChannel.getOid())) {
					//logger.error("Feature #1885 found same oid on channel {}", newChannel.getNumber());
					sameChannel = channel;
					migrateChannel(sameChannel, newChannel);
				}
				else {
					//  Bug #1604 firmware not supporting qualifier prior 0.3.4
					if (channel.getConfiguration().getQualifier() == MeasureQualifier.ONE.getValue() && //
							newChannel.getConfiguration().getQualifier() == MeasureQualifier.AVG.getValue()) {
						sameChannel = channel;
						migrateChannel(sameChannel, newChannel);
					}
					else {
						// de-activate channel with same number
						if (channel.getConfiguration().isActive()) {
							channel.getConfiguration().setActive(false);
							channel.getConfiguration().setDeactivationDate(new Date());
						}						
					}
				}
				break;
			}
			
		}

		// an existing channel must be updated / activated
		if (sameChannel != null) {
			if (!sameChannel.getConfiguration().isActive()) {
				sameChannel.activateChannel(newChannel.getConfiguration().getActivationDate());
			}
		} else {
			// a new channel will be created
			device.addChannel(newChannel);
		}
		return true;

	}

	private void migrateChannel(Channel sameChannel, Channel newChannel) {
		if (newChannel.getOid() != null) {
			sameChannel.setOid(newChannel.getOid());
		}
		if (newChannel.getMetaData() != null) {
			sameChannel.setMetaData(newChannel.getMetaData());
		}
		if (newChannel.getConfiguration().getLabel() != null) {
			sameChannel.getConfiguration().setLabel(newChannel.getConfiguration().getLabel());			
		}
		sameChannel.getConfiguration().setSensor(newChannel.getConfiguration().getSensor());
		
		for (MeasureUnit mu : sameChannel.getMeasures()) {
			for (MeasureUnit nmu : newChannel.getMeasures()) {
				if (nmu.getType().equals(mu.getType()) && nmu.getScale().equals(mu.getScale())
						&& nmu.getOffset().equals(mu.getOffset())) {
					mu.setFormat(nmu.getFormat());
				}
			}
		}
	}

	/**
	 * add a new channel checking if exist another with same tuple (uniqueKey
	 * number) new channel is added only if (uniqueKey number) is different existing
	 * channel is re-activated
	 * 
	 * @param device
	 * @param newChannel
	 * @param grouped    = true, do not create a new channel if number id is changed
	 * @return
	 */
	@Deprecated
	public boolean manageChannelByGroup(Device device, Channel newChannel, boolean grouped) {
		boolean added = false;
		if (newChannel == null) {
			return added;
		}
		if (newChannel.getMeasures().isEmpty()) {
			return added;
		}
		if (newChannel.getId() != null) {
			return added;
		}
		if (newChannel.getNumber() == null || newChannel.getNumber().isEmpty()) {
			return added;
		}
		if (newChannel.getUniqueKey() == null || newChannel.getUniqueKey().isEmpty()) {
			return added;
		}

		// find if channel already exists
		Channel sameChannel = null;

		if (grouped) {
			// Bug #302 ftp import: changing order of a parameter in csv creates
			// an extra parameter
			// exist a channel with same configuration
			// eventually new channel has a different number
			for (Channel channel : device.getChannels()) {
				if (channel.getUniqueKey().equals(newChannel.getUniqueKey())) {
					sameChannel = channel;
					sameChannel.setNumber(newChannel.getNumber());
					break;
				}
			}
		} else {
			// Feature #195 Create device param with different id and same
			// configuration (same unique key)
			for (Channel channel : device.getChannels()) {
				if (channel.internalKey().equals(newChannel.internalKey())) {
					sameChannel = channel;
					break;
				}
			}

		}

		// an existing channel must be updated / activated
		if (sameChannel != null) {
			if (!sameChannel.getConfiguration().isActive()) {
				// Bug #198 (In Progress): [FTP IMPORT] some imported data
				// timestamps do not match with device config
				sameChannel.activateChannel(newChannel.getConfiguration().getActivationDate());
			} else {
				// Bug #245 D32 ftp imported data causes issues on configuration
				// validity date
				Date newActivation = newChannel.getConfiguration().getActivationDate();
				if (sameChannel.getConfiguration().getActivationDate().after(newActivation)) {
					sameChannel.getConfiguration().setActivationDate(newActivation);
				}
				// Feature #247 Introduzione dei codici SENSOR per i parametri
				// dello strumento
				sameChannel.getConfiguration().setSensor(newChannel.getConfiguration().getSensor());
			}
		} else {
			// a new channel will be created
			device.addChannel(newChannel);
			added = true;
		}
		return added;
	}

	/**
	 * activate channel and eventually backup previous activity period
	 * 
	 * @param channel
	 */
//	public void activateChannel(Channel channel, Date activationDate) {
//		if (!channel.getConfiguration().isActive()) {
//			Date start = channel.getConfiguration().getActivationDate();
//			Date end = channel.getConfiguration().getDeactivationDate();
//			if (start != null && end != null) {
//				ValidityInterval validity = new ValidityInterval();
//				validity.setStartDate(start);
//				validity.setEndDate(end);
//				channel.addValidity(validity);
//			}
//			channel.getConfiguration().setActive(true);
//		}
//		channel.getConfiguration().setActivationDate(activationDate);
//		channel.getConfiguration().setDeactivationDate(null);
//	}

//	@Transactional
//	public Device merge(Device device) {
//		return deviceDao.merge(device);
//	}

	public long count(DeviceCriteria criteria) {
		return deviceDao.count(criteria);
	}

	public List<Device> search(DeviceCriteria criteria, int offset, int limit) {
		return deviceDao.search(criteria, offset, limit);
	}

	public MeasureSensorType getSensor(int type) {
		try {
			return measureSensorTypeDao.findByCode(type);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;
	}

	@Transactional
	public boolean addDeviceToGroup(Device device, NetworkGroup group) {
		device = deviceDao.findOne(device.getId());
		group = groupDao.merge(group);
		if (device.getNetwork() != null) {
			if (!group.getNetwork().equals(device.getNetwork())) {
				return false;
			}
		}
		device.addGroup(group);
		deviceDao.update(device);
		return true;
	}

	@Transactional
	public boolean removeDeviceFromGroup(Device device, NetworkGroup group) {
		device = deviceDao.findOne(device.getId());
		group = groupDao.merge(group);
		if (!device.getGroups().contains(group)) {
			return false;
		}
		device.getGroups().remove(group);
		deviceDao.update(device);
		return true;
	}

	@Transactional
	public NetworkGroup getDeviceAlarmGroup(Device device) {
		if (device.getNetwork() == null) {
			return null;
		}
		String externalId = device.getSerial() + "-alarms";

		try {
			NetworkGroup group = groupDao.findByExternalId(externalId, device.getNetwork());
			if (group == null) {
				group = new NetworkGroup();
				group.setName(externalId);
				group.setExternalId(externalId);
				group.setExclusive(true);
				group.setGroupType(NetworkGroupType.ALARMS);
				group.setNetwork(device.getNetwork());
				groupDao.create(group);
				group = groupDao.merge(group);
			}
			return group;
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;

	}
	
	
	@Transactional
	public NetworkGroup getDeviceExportGroup(Device device) {
		if (device.getNetwork() == null) {
			return null;
		}
		String externalId = device.getSerial() + "-exports";

		try {
			NetworkGroup group = groupDao.findByExternalId(externalId, device.getNetwork());
			if (group == null) {
				group = new NetworkGroup();
				group.setName(externalId);
				group.setExternalId(externalId);
				group.setExclusive(true);
				group.setGroupType(NetworkGroupType.EXPORTS);
				group.setNetwork(device.getNetwork());
				groupDao.create(group);
				group = groupDao.merge(group);
			}
			return group;
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;

	}
	

	public Device findByActivationKey(String value) {
		try {
			return deviceDao.findByActivationKey(value);
		} catch (BackendServiceException e) {
			logger.error("BackendServiceException", e);
		}
		return null;
	}

	public List<Device> findByModel(DeviceModel model) {
		return deviceDao.findByModel(model);
	}

	public List<Device> findSlaves(Device master) {
		return deviceDao.findSlaves(master);
	}

	public List<Device> findWithInactivityCheck() {
		return deviceDao.findInactive();
	}

	public ModbusRegister getModbusRegister(String id) {
		if (id == null)
			return null;
		return modbusRegisterDao.findOne(id);
	}

	public ChannelAlarm getChannelAlarmFromModbusRegister(Channel chnl) {
		String oid = chnl.getOid();
		if (oid == null) {
			String meta = chnl.getMetaData();
			if (meta != null) {
				String[] parts = StringUtils.split(meta, "|");
				if (parts.length > 2) {
					oid = parts[parts.length - 1];
				}
			}
		}
		ModbusRegister register = getModbusRegister(oid);
		if (register == null)
			return null;
		if (!register.getTypeVar().equals(TypeVar.ALARM)) {
			return null;
		}
		return chnl.buildChannelAlarm(register);
	}


	public String provisioningChecksum(Device master) {
		StringBuffer sb = new StringBuffer();
		sb.append(master.getSerial());
		List<Device> slaves = deviceDao.findSlaves(master);
		for (Device slave : slaves) {
			for (ModbusProfile profile : slave.getProfiles()) {
				sb.append(profile.getCreationDate());
				sb.append(profile.getConsistencyVersion());
				for (ModbusRegister register : profile.getRegisters()) {
					sb.append(register.getMetaData());
					sb.append(register.isAvailable());
				}
			}
		}
		return EncryptUtils.digest(sb.toString());
	}

	public Map<Device, Set<GroupWidget>> findMappableDevices(Collection<NetworkGroup> groups) {
		Set<GroupWidget> groupWidgets = new HashSet<GroupWidget>();
		Map<Device, Set<GroupWidget>> map = new HashMap<Device, Set<GroupWidget>>();
		if (groups.isEmpty()) {
			return map;
		}
		for (NetworkGroup group : groups) {
			if (group.getGroupType() != null) {
				switch (group.getGroupType()) {
				case ALARMS:
					Collection<Device> devices = deviceDao.findByGroup(group);
					for (Device device : devices) {
						if (!device.getChannels().isEmpty()) {
							map.put(device, new HashSet<GroupWidget>());
						}
					}
					break;
				case DEVICE:
				case GROUP_WIDGET:
					groupWidgets.addAll(groupWidgetDao.findByNetworkGroup(group));
					break;
				}
			}
		}

		for (GroupWidget groupWidget : groupWidgets) {
			Collection<Device> devices = deviceDao.findByGroup(groupWidget.getGroup());
			for (Device device : devices) {
				if (map.containsKey(device)) {
					map.get(device).add(groupWidget);
				} else {
					map.put(device, new HashSet<GroupWidget>());
					map.get(device).add(groupWidget);
				}
			}
		}
		return map;
	}

	@Transactional
	public void createOrUpdateChannel(Channel entity) {
		if (entity.isNew()) {
			channelDao.create(entity);
		} else {
			channelDao.update(entity);
		}
	}

//	@Transactional
//	public void updateOnConfiguration(Device entity) {
//		deviceDao.updateOnConfiguration(entity);
//	}

	@Transactional
	public boolean deactivateDevice(Device device, boolean blocked) {
		boolean changed = deviceDao.deactivateDevice(device, blocked);
		if (changed && device.getMaster() == null) {
			if (blocked) {
				tracingDao.trace(TracingAction.DEVICE_DEACTIVATED, null, device.getOwner(), "", device.getSerial(),
						"the block date has arrived");
			}
			this.fireUpdatedEvent(device);
			List<Device> slaves = deviceDao.findSlaves(device);
			for (Device slave : slaves) {
				deviceDao.deactivateDevice(slave, blocked);
				this.fireUpdatedEvent(device);
			}
		}
		return changed;
	}
	
	@Transactional
	public int updateLastExportDate(String serial) {
		return deviceDao.updateLastExportDate(serial, new Date());
	}
	

}
