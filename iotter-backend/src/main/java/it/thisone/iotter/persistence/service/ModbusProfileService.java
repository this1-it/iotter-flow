package it.thisone.iotter.persistence.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.Priority;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.enums.modbus.TemplateState;
import it.thisone.iotter.enums.modbus.TypeRead;
import it.thisone.iotter.enums.modbus.TypeVar;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IModbusProfileDao;
import it.thisone.iotter.persistence.ifc.IModbusRegisterDao;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelAlarm;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GraphicFeed;
import it.thisone.iotter.persistence.model.ModbusConfiguration;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.util.BacNet;
import it.thisone.iotter.util.ChannelProfilesComparator;

@Service
public class ModbusProfileService {
	public static Logger logger = LoggerFactory.getLogger(ModbusProfileService.class);

	@Autowired
	private DatabaseMessageSource messages;

	@Autowired
	private IModbusRegisterDao registerDao;

	@Autowired
	private IModbusProfileDao profileDao;

	public ModbusProfileService() {
		super();
	}

	@Transactional
	public void changeChecksum(ModbusProfile entity) {
		profileDao.updateCreationDate(entity);
	}

	@Transactional
	public void create(ModbusProfile entity) {
		profileDao.create(entity);
	}

	@Transactional
	public void update(ModbusProfile entity) {
		addMessages(entity);
		profileDao.update(entity);
	}

	public ModbusProfile findOne(String id) {
		return profileDao.findOne(id);
	}

	public List<ModbusProfile> findAll() {
		return profileDao.findAll();
	}

	public void removeMessages(ModbusProfile profile) {
		for (ModbusRegister register : profile.getRegisters()) {
			messages.removeMessages(register.getMessages());
		}
	}

	public void addMessages(ModbusProfile profile) {
		for (ModbusRegister register : profile.getRegisters()) {
			messages.addMessages(register.getMessages());
		}
	}

	@Transactional
	public void deleteById(String entityId) {
		ModbusProfile entity = findOne(entityId);
		if (entity != null) {
			removeMessages(entity);
			profileDao.deleteById(entityId);
		}
	}

	public List<ModbusProfile> findTemplates() {
		return profileDao.findTemplates();
	}

	public List<ModbusProfile> findLastTemplates(boolean supervisor) {
		return profileDao.findLastTemplates(supervisor);
	}

	public ModbusProfile findTemplate(String name, String revision) throws BackendServiceException {
		return profileDao.findTemplate(name, revision);
	}
	
	public ModbusProfile copyBuffered(ModbusProfile source) {
		ModbusProfile target = new ModbusProfile();
		BeanUtils.copyProperties(source, target, new String[] { "configuration", "registers" });
		ModbusConfiguration configuration = new ModbusConfiguration();
		BeanUtils.copyProperties(source.getConfiguration(), configuration);
		target.setConfiguration(configuration);
		target.setId(source.getId());

		
		for (ModbusRegister sr : source.getRegisters()) {
			ModbusRegister tr = new ModbusRegister();
			BeanUtils.copyProperties(sr, tr);
			tr.setId(sr.getId());
			target.addRegister(tr);
		}		
		return target;
	}

	public ModbusProfile cloneProfile(ModbusProfile source) {
		ModbusConfiguration configuration = new ModbusConfiguration();
		BeanUtils.copyProperties(source.getConfiguration(), configuration);
		ModbusProfile target = new ModbusProfile();
		target.setConfiguration(configuration);
		target.setDisplayName(source.getDisplayName());
		target.setTemplate(source.getTemplate());
		target.setRevision(source.getRevision());
		target.setCreationDate(source.getCreationDate());
		for (ModbusRegister sr : source.getRegisters()) {
			target.addRegister(cloneRegister(sr));
		}

		target.setId(UUID.randomUUID().toString());
		for (ModbusRegister register : target.getRegisters()) {
			register.setId(UUID.randomUUID().toString());
		}

		return target;
	}

	public ModbusRegister cloneRegister(ModbusRegister source) {
		ModbusRegister target = new ModbusRegister();
		BeanUtils.copyProperties(source, target, new String[] { "id", "profile", "messages" });
		target.setId(UUID.randomUUID().toString());
		return target;
	}

	public void commitChanges(ModbusProfile source, ModbusProfile target) {
		List<ModbusRegister> sregisters = source.getRegisters();
		target.setRegisters(new ArrayList<ModbusRegister>());
		for (int i = 0; i < source.getRegisters().size(); i++) {
			ModbusRegister sr = sregisters.get(i);
			ModbusRegister tr = new ModbusRegister();
			BeanUtils.copyProperties(sr, tr);
			tr.setId(UUID.randomUUID().toString());
			target.addRegister(tr);
		}
		BeanUtils.copyProperties(source.getConfiguration(), target.getConfiguration());
	}
	
	public void copy(ModbusProfile source, ModbusProfile target) {
		List<ModbusRegister> sregisters = source.getRegisters();
		List<ModbusRegister> tregisters = target.getRegisters();
		for (int i = 0; i < source.getRegisters().size(); i++) {
			ModbusRegister sr = sregisters.get(i);
			ModbusRegister tr = tregisters.get(i);
			tr.setActive(sr.getActive());
		}
		BeanUtils.copyProperties(source.getConfiguration(), target.getConfiguration());
	}
	
	

	public String translateDisplayName(ModbusRegister source, Locale locale) {
		String code = messageBundleId(source.getMetaData());
		String message = messages.getDatabaseMessage(code, source.getDisplayName(), locale);
		return message.isEmpty() ? source.getDisplayName() : message;
	}



	public ModbusProfile createMockModbusProfile(int slaveID) {
		ModbusProfile profile = new ModbusProfile();
		profile.setDisplayName(String.format("Modbus %03d", slaveID));
		profile.setRevision("00");
		profile.getConfiguration().setSampleRate(10);
		profile.getConfiguration().setSlaveID(slaveID);
		profile.getConfiguration().setSlaveName(String.format("Slave%03d", slaveID));
		;
		TypeVar[] typevar = { TypeVar.ANALOG, TypeVar.DIGITAL };
		for (int i = 1; i <= 10; i++) {
			ModbusRegister register = new ModbusRegister();
			register.setCrucial(false);
			register.setAddress(i);
			register.setDisplayName(String.format("Address %03d", i));
			TypeVar type = typevar[(int) (Math.random() * typevar.length)];
			register.setTypeVar(type);
			Permission permission = type.equals(TypeVar.DIGITAL) ? Permission.READ_WRITE : Permission.READ;
			register.setPermission(permission);
			register.setActive(true);
			register.setDecimalDigits(0);
			register.setDeltaLogging(0d);
			register.setMax(10d);
			register.setMin(0d);
			int measureUnit = type.equals(TypeVar.DIGITAL) ? BacNet.ADIM : BacNet.DEGREES_CELSIUS;
			register.setMeasureUnit(measureUnit);
			register.setScaleMultiplier(1d);
			register.setOffset(0d);
			Priority priority = type.equals(TypeVar.DIGITAL) ? Priority.NORMAL : null;
			register.setPriority(priority);
			String externalId = String.format("%s.%s.%s", profile.getDisplayName(), profile.getRevision(),
					register.getDisplayName());
			register.setMetaData(externalId.replaceAll(" ", "_").toLowerCase());
			register.setProfile(profile);
			profile.getRegisters().add(register);
		}
		return profile;
	}

	public String messageBundleId(String metadata) {
		if (metadata != null && metadata.contains("|")) {
			int beginIndex = metadata.lastIndexOf("|");
			if (beginIndex > 0) {
				return metadata.substring(beginIndex + 1);
			}
		}
		return null;
	}

	public String getRegisterMetadata(String registerId) {
		return registerDao.getMetadata(registerId);
	}




	@Cacheable(value = Constants.Cache.MODBUS_REGISTERS, key = "{#displayName,#address,#typeRead}", unless = "#result == null")
	public List<ModbusRegister> findCompatibleRegisters(String displayName, Integer address, TypeRead typeRead) {
		return registerDao.findCompatibleRegisters(displayName, address, typeRead);
	}

	
	private List<ModbusRegister> findCompatibleRegisters(Channel chnl) {
		String displayName = chnl.getConfiguration().getDisplayName();
		TypeRead typeRead = null;
		Integer address = null;
		if (chnl.getNumber().contains(":")) {
			String[] parts = chnl.getNumber().split(":");
			address = Integer.parseInt(parts[1]);
			for (TypeRead value : TypeRead.values()) {
				if (value.getShortName().equalsIgnoreCase(parts[2])) {
					typeRead = value;
				}
			}
		}
		List<ModbusRegister> registers = findCompatibleRegisters(displayName, address, typeRead);
		return registers;
	}

	public ModbusProfile rebuildSlaveProfile(Device slave, ModbusProfile template) {
		String[] parts = slave.getSerial().split("-");
		String slaveName = parts[1];
		ModbusProfile target = this.cloneProfile(template);
		// target.populateMetaData();

		target.setDisplayName(String.format("%s - RECOVERY", template.getId()));
		target.getConfiguration().setSlaveName(slaveName);
		target.getConfiguration().setSlaveID(Integer.parseInt(slaveName));
		boolean alarmed = false;
		for (ModbusRegister register : target.getRegisters()) {
			String number = String.format("%s:%s:%s", slaveName, register.getAddress().toString(),
					register.getTypeRead().getShortName().toLowerCase());
			Optional<Channel> match = slave.getChannels().stream().filter(o -> o.getNumber().equals(number))
					.findFirst();
			if (match.isPresent()) {

				match.get().setOid(register.getId());
				match.get().setMetaData(register.getMetaData());

				register.setActive(match.get().getConfiguration().isActive());
				if (register.getTypeVar().equals(TypeVar.ALARM)) {
					match.get().setAlarm(match.get().buildChannelAlarm(register));
					alarmed = true;
				} else {
					match.get().setAlarm(new ChannelAlarm());
				}
			} else {
				register.setActive(false);
			}
		}
		for (Channel chnl : slave.getChannels()) {
			Optional<ModbusRegister> match = target.getRegisters().stream().filter(o -> o.getId().equals(chnl.getOid()))
					.findFirst();
			if (!match.isPresent()) {
				List<ModbusRegister> registers = findCompatibleRegisters(chnl);
				if (!registers.isEmpty()) {
					target.addRegister(cloneRegister(registers.get(0)));
				} else {
					logger.error("{} register not found for param {}",slave.getSerial(), chnl.getNumber());
					chnl.activateChannel(new Date());
				}
			}
		}

		slave.setDescription(target.getDisplayName());
		slave.setProfiles(new HashSet<ModbusProfile>());
		slave.getProfiles().add(target);
		slave.setAlarmed(alarmed);
		return target;
	}

	/*
	 * find most compatible ModbusProfile template
	 */
	public ModbusProfile findCompatibleModbusProfile(Device device) {
		if (device.getMaster() == null) {
			return null;
		}
		if (device.getChannels().isEmpty()) {
			return null;
		}
		SortedSet<Map.Entry<Channel, Set<ModbusProfile>>> sorted = new TreeSet<Map.Entry<Channel, Set<ModbusProfile>>>(
				new Comparator<Map.Entry<Channel, Set<ModbusProfile>>>() {
					@Override
					public int compare(Map.Entry<Channel, Set<ModbusProfile>> e1,
							Map.Entry<Channel, Set<ModbusProfile>> e2) {
						if (e1.getValue().size() >= e2.getValue().size()) {
							return -1;
						} else {
							return 1;
						}
					}
				});

		Map<Channel, Set<ModbusProfile>> map = new HashMap<Channel, Set<ModbusProfile>>();

		for (Channel chnl : device.getChannels()) {
			Set<ModbusProfile> compatible = findCompatibleModbusProfiles(chnl);
			//logger.error("{} compatible profiles found {}", chnl.getNumber(), compatible.size());
			map.put(chnl, compatible);

		}
		
		sorted.addAll(map.entrySet());

		Set<ModbusProfile> found = new HashSet<>();
		Set<ModbusProfile> intersection = new HashSet<>(sorted.first().getValue());

		for (Map.Entry<Channel, Set<ModbusProfile>> entry : sorted) {
			found = new HashSet<>(intersection);
			intersection.retainAll(entry.getValue());
			//logger.error("{} {}", intersection.size(), entry.getValue().size());
			if (intersection.isEmpty()) {
				break;
			}

		}

		//logger.error("{} [{}] compatible profiles found {}", device.getSerial(), device.getOwner(), found.size());

		if (found.isEmpty()) {
			return null;
		}

		List<ModbusProfile> list = new ArrayList<ModbusProfile>();

		list.addAll(found);

		// list.sort((ModbusProfile p1, ModbusProfile p2) ->
		// p1.getRevision().compareTo(p2.getRevision()));

		list.sort((ModbusProfile p1, ModbusProfile p2) -> Integer.compare(p1.getRegisters().size(),
				p2.getRegisters().size()));
		return list.get(list.size() - 1);
	}

	private Set<ModbusProfile> findCompatibleModbusProfiles(Channel chnl) {
		List<ModbusRegister> registers = findCompatibleRegisters(chnl);

		Set<ModbusProfile> compatible = new HashSet<>();
		for (ModbusRegister register : registers) {
			if (register.getProfile().getResource() != null) {
				compatible.add(register.getProfile());
			}
		}
		return compatible;
	}

	public String modbus_address(String slaveId, ModbusRegister register) {
		String number = String.format("%s:%s:%s", slaveId, register.getAddress().toString(), register.getTypeRead().getShortName().toLowerCase());
		return number;
	}

	// Feature #2029
	public List<GraphicFeed> cloneFeeds(List<GraphicFeed> sourceFeeds, ModbusProfile target) {
		List<GraphicFeed> feeds = new ArrayList<GraphicFeed>();
		for (GraphicFeed source : sourceFeeds) {
			Optional<ModbusRegister> match = target.getRegisters().stream().filter(o -> o.getMetaIdentifier().equals(source.getMetaIdentifier()))
					.findFirst();
			if (match.isPresent()) {
				GraphicFeed feed = new GraphicFeed();
				BeanUtils.copyProperties(source, feed, new String[] { "id", "channel", "widget" });
				feeds.add(feed);
			}
			else {
				logger.error("{} register not found {}", target.getDisplayName(), source.getMetaIdentifier());
			}
		}
		return feeds;
	}
	
	// Feature #2029
	public void copyCompatibleRegisters(ModbusProfile source, ModbusProfile target) {
		if (target != null) {
			for (ModbusRegister tr : target.getRegisters()) {
				Optional<ModbusRegister> match = source.getRegisters().stream().filter(ModbusRegister.IS_EQUAL(tr)).findFirst();
				if (match.isPresent()) {
					tr.setActive(match.get().getActive());
				}
			}
			List<ModbusRegister> critical1 = target.getRegisters().stream().filter(s -> s.getCrucial()).collect(Collectors.toList());
			List<ModbusRegister> critical2 = source.getRegisters().stream().filter(s -> s.getCrucial()).collect(Collectors.toList());

			for (ModbusRegister register : critical1) {
				target.getRegisters().remove(register);
			}
			for (ModbusRegister register : critical2) {
				target.addRegister(cloneRegister(register));
			}			
		}
	}
	
	
	/*
	 * find most compatible ModbusProfile template
	 */
//	public ModbusProfile findCompatibleModbusProfile(Device device) {
//		if (device.getMaster() == null) {
//			return null;
//		}
//		if (device.getChannels().isEmpty()) {
//			return null;
//		}
//		Set<ModbusProfile> found = new HashSet<>();
//		for (Channel chnl : device.getChannels()) {
//			found = checkCompatibleProfiles(chnl, found);
//		}
//		logger.error("{} [{}] compatible profiles found {}", device.getSerial(), device.getOwner(), found.size());
//
//		if (found.isEmpty()) {
//			return null;
//		}
//
//		List<ModbusProfile> list = new ArrayList<ModbusProfile>();
//
//		list.addAll(found);
//
//		// list.sort((ModbusProfile p1, ModbusProfile p2) ->
//		// p1.getRevision().compareTo(p2.getRevision()));
//
//		list.sort((ModbusProfile p1, ModbusProfile p2) -> Integer.compare(p1.getRegisters().size(),
//				p2.getRegisters().size()));
//		return list.get(list.size() - 1);
//	}

	/*
	 * accumulates compatible profiles
	 */
//	private Set<ModbusProfile> checkCompatibleProfiles(Channel chnl, Set<ModbusProfile> found) {
//		List<ModbusRegister> registers = findCompatibleChannelRegisters(chnl);
//
//		Set<ModbusProfile> compatible = new HashSet<>();
//		for (ModbusRegister register : registers) {
//			if (register.getProfile().getResource() != null) {
//				ModbusProfile profile = register.getProfile();
//				if (found.isEmpty()) {
//					compatible.add(profile);
//				} else {
//					if (found.contains(profile)) {
//						compatible.add(profile);
//					}
//				}
//			}
//		}
//		return compatible;
//	}

}
