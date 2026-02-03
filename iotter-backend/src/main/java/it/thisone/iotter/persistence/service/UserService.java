package it.thisone.iotter.persistence.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.NetworkGroupType;
import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.ifc.IGroupWidgetDao;
import it.thisone.iotter.persistence.ifc.INetworkDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.ifc.IRoleDao;
import it.thisone.iotter.persistence.ifc.ITracingDao;
import it.thisone.iotter.persistence.ifc.IUserDao;
import it.thisone.iotter.persistence.ifc.IUserTokenDao;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserCriteria;
import it.thisone.iotter.persistence.model.UserToken;

@Service
public class UserService {
	public static Logger logger = LoggerFactory.getLogger(UserService.class);
	@Autowired
	private ITracingDao tracingDao;

	@Autowired
	private IUserTokenDao tokenDao;

	@Autowired
	private IUserDao userDao;

	@Autowired
	private IRoleDao roleDao;

	@Autowired
	private INetworkGroupDao groupDao;

	@Autowired
	private IDeviceDao deviceDao;

	@Autowired
	private INetworkDao networkDao;

	@Autowired
	private IGroupWidgetDao groupWidgetDao;

	public UserService() {
		super();
	}

	// API

	@Transactional
	public void create(User entity) {
		userDao.create(entity);
	}

	@Transactional
	public void update(User entity) {
		userDao.update(entity);
	}

	@Transactional(readOnly=true)
	public User findOne(String id) {
		User user = userDao.findOne(id);
		if (user != null) {
			user.getRoles().size();
			user.getGroups().size(); 
		}
		return user;
	}

	public List<User> findAll() {
		return userDao.findAll();
	}

	public List<User> findByOwner(String owner) {
		return userDao.findByOwner(owner);
	}

	@Transactional
	public void deleteById(String entityId) {
		userDao.deleteById(entityId);
	}

	// @Transactional(readOnly=true)
	// @ReadOnlyConnection
	public User findByName(String userName) {
		return userDao.findByName(userName);
	}

	@Transactional
	public void registerLogin(User entity) {
		// TODO register last login date, reset failures
	}

	@Transactional
	public void registerLoginFailure(User entity) {
		// increase failure counter, lock user if exceed failure count
	}

	/**
	 * create a user with administration role create default network and a
	 * default group activate an existing device associating with default group
	 * 
	 * @param entity
	 * @param deviceSerial
	 * @param deviceActivation
	 * @param networkName
	 * @throws BackendServiceException
	 */
	@Transactional
	public void administrationRegistration(User entity, String deviceSerial, String deviceActivation,
			String networkName) throws BackendServiceException {

		String username = entity.getUsername();
		if (userDao.findByName(username) != null) {
			throw new BackendServiceException("user already exists " + username);
		}

		Device device = deviceDao.findBySerial(deviceSerial);
		if (device == null) {
			throw new BackendServiceException("device not found" + deviceSerial);
		}
		
		boolean statuOk = device.isAvailableForActivation();
		if (!statuOk && !device.getActivationKey().equals(deviceActivation)) {
			throw new BackendServiceException("device cannot be activated"
					+ device.getSerial());
		}
		
		if (networkName == null || networkName.isEmpty()) {
			networkName = Constants.DEFAULT_NETWORK;
		}
		if (networkDao.findByName(networkName, username) != null) {
			throw new BackendServiceException(networkName + " network already exists for user " + username);
		}
		
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.HOUR_OF_DAY, 1);

		Role administrator = roleDao.findByName(Constants.ROLE_ADMINISTRATOR);
		entity.setAccountStatus(AccountStatus.NEED_ACTIVATION);
		entity.setForcePasswordChange(true);
		entity.setUsername(username);
		entity.setOwner(username);
		entity.addRole(administrator);
		entity.setExpiryDate(calendar.getTime());
		userDao.create(entity);
		
		Network network = new Network();
		network.setName(networkName);
		network.setOwner(username);
		network.setNetworkType(NetworkType.GEOGRAPHIC);
		network.setDefaultNetwork(true);
		networkDao.create(network);
		
		deviceDao.activate(device, deviceActivation, username, network);
	}

	/**
	 * activate an existing device associating with default group
	 * 
	 * @param deviceSerial
	 * @param deviceActivation
	 * @param owner
	 * @param groups
	 * @return
	 * @throws BackendServiceException
	 */
	@Transactional
	public Device deviceActivation(String deviceSerial, String deviceActivation, String owner, Network network)
			throws BackendServiceException {
		if (userDao.findByName(owner) == null) {
			throw new BackendServiceException("user not exists " + owner);
		}

		Device entity = deviceDao.findBySerial(deviceSerial);
		if (entity == null) {
			throw new BackendServiceException("device not found" + deviceSerial);
		}

		deviceDao.activate(entity, deviceActivation, owner, network);
		String networkName = "";
		if (network != null) {
			networkName = network.getName();
		}
		tracingDao.trace(TracingAction.DEVICE_ACTIVE, null, entity.getOwner(), networkName, entity.getSerial(),
				entity.toString());

		return entity;

	}

	public List<User> findUsers(String owner, String name, AccountStatus status) {
		return userDao.findUsers(owner, name, status);
	}

	public List<User> findByRole(String role) {
		return userDao.findByRoleName(role);
	}

	public List<User> findByGroup(NetworkGroup group) {
		return userDao.findByGroup(group);
	}

	@Transactional
	public UserToken createUserToken(String owner, String action, String token, int hours) {
		UserToken tkn = tokenDao.findCurrentToken(owner, action);
		if (tkn == null) {
			tkn = new UserToken(owner, action, token);
			if (hours > 0) {
				tkn.setExpiryDate(DateUtils.addHours(new Date(), hours));
			}
			tokenDao.create(tkn);
		}
		return tkn;
	}

	@Transactional
	public boolean validateToken(String owner, String action, String value) {
		UserToken token = tokenDao.findCurrentToken(owner, action);
		return (token != null) ? token.getToken().equals(value) : false;
	}

	@Transactional
	public void deleteToken(String owner, String action) {
		tokenDao.deleteCurrentToken(owner, action);
	}

	@Transactional
	public UserToken findToken(String value) {
		return tokenDao.findByValue(value);
	}

	@Transactional
	public User changePassword(String username, String password) {
		// TODO extends check
		User entity = userDao.findByName(username);
		if (entity != null) {
			entity.setPassword(password);
			userDao.update(entity);
			// eventually remove all otp tokens
			//tokenDao.deleteByOwner(username);
		}
		return entity;
	}
	


	@Transactional
	public User changeAccountStatus(String username, AccountStatus accountStatus) {
		// TODO extends check
		User entity = userDao.findByName(username);
		if (entity != null) {
			entity.setAccountStatus(accountStatus);
			if (accountStatus.equals(AccountStatus.ACTIVE)) {
				entity.setExpiryDate(null);
			}
			userDao.update(entity);
			// eventually remove all otp tokens
			tokenDao.deleteByOwner(username);
		}
		return entity;
	}

	

	
	@Transactional
	public User safeCreateUser(String username, String password, String email, AccountStatus status, String firstName, String lastName,
			Role role, NetworkGroup group, String owner) {
		String street = "";
		String city = "";
		String zipCode = "";
		String phoneNumber = "";
		String country = "";

		User entity = findByName(username);
		if (entity == null) {
			entity = new User(username, password, status, firstName, lastName, email, street, city,
					zipCode, phoneNumber, country);
			entity.setOwner(owner);
			if (role != null) {
				entity.addRole(role);
			}
			if (group != null) {
				entity.addGroup(group);
			}
			create(entity);
		} else {
			if (role != null) {
				if (!entity.hasRole(role.getName())) {
					entity.addRole(role);
				}
			}
			update(entity);
		}
		return entity;
	}

	public void merge(User user) {
		userDao.merge(user);
	}

	/**
	 * delete everything created by owner
	 * 
	 * @param owner
	 */
	@Transactional
	public void deleteOwner(String owner) {
		User admin = userDao.findByName(owner);
		if (admin == null) {
			return;
		}

		List<GroupWidget> widgets = groupWidgetDao.findByOwner(owner);
		for (GroupWidget widget : widgets) {
			groupWidgetDao.deleteById(widget.getId());
		}

		List<Device> devices = deviceDao.findByOwner(owner);
		for (Device device : devices) {
			
			if (device.getMaster() != null) {
				deviceDao.delete(device);
			}
			else {
				device.setGroups(new HashSet<NetworkGroup>());
				device.setOwner(Constants.ROLE_PRODUCTION.toLowerCase());
				deviceDao.update(device);
			}
		}


		List<User> users = userDao.findByOwner(owner);
		for (User user : users) {
			userDao.deleteById(user.getId());
		}
		
		List<NetworkGroup> groups = groupDao.findByOwner(owner);
		for (NetworkGroup group : groups) {
			groupDao.deleteById(group.getId());
		}

		List<Network> networks = networkDao.findByOwner(owner);
		for (Network network : networks) {
			networkDao.deleteById(network.getId());
		}
		
		userDao.deleteById(admin.getId());

	}

	
	@Transactional
	public void deleteOwnership(String owner) {
		try {
			User admin = userDao.findByName(owner);
			if (admin == null) {
				return;
			}

			List<GroupWidget> widgets = groupWidgetDao.findByOwner(owner);
			for (GroupWidget widget : widgets) {
				groupWidgetDao.deleteById(widget.getId());
			}

			List<Device> devices = deviceDao.findByOwner(owner);
			for (Device device : devices) {
				if (device.getMaster() != null) {
					deviceDao.delete(device);
				}
				else {
					device.setGroups(new HashSet<NetworkGroup>());
					device.setOwner(Constants.ROLE_PRODUCTION.toLowerCase());
					deviceDao.update(device);
				}
			}


			List<User> users = userDao.findByOwner(owner);
			for (User user : users) {
				if (!user.getUsername().equals(owner)) {
					userDao.deleteById(user.getId());
				}
			}

			List<NetworkGroup> groups = groupDao.findByOwner(owner);
			for (NetworkGroup group : groups) {
				groupDao.deleteById(group.getId());
			}
			
			List<Network> networks = networkDao.findByOwner(owner);
			for (Network network : networks) {
				networkDao.deleteById(network.getId());
			}
			
		} catch (Throwable t) {
			logger.error("delete ownership" + owner, t);
		}
		

	}

	
	@Transactional
	public boolean addUserToGroup(User user, NetworkGroup group) {
		if (!user.getOwner().equals(group.getOwner())) {
			logger.error("{} cannot add to group {} which belongs to owner {}", user.getUsername(), group.getName(), group.getOwner());
			return false;
		}
		user = userDao.findOne(user.getId());
		group = groupDao.merge(group);
		// Feature #1884
		if (user.getNetwork() != null && !group.getGroupType().equals(NetworkGroupType.GROUP_WIDGET)) {
			if (!group.getNetwork().equals(user.getNetwork())) {
				logger.error("{} cannot add to group {} which belongs to network {}", user.getUsername(), group.getName(), group.getNetwork().getName());
				return false;
			}
		}
		user.addGroup(group);
		userDao.update(user);
		return true;
	}

	@Transactional
	public boolean removeUserFromGroup(User user, NetworkGroup group) {
		user = userDao.findOne(user.getId());
		group = groupDao.merge(group);
//		if (!user.getGroups().contains(group)) {
//			return false;
//		}
		user.getGroups().remove(group);
		userDao.update(user);
		return true;
	}

	public List<User> findByNetwork(Network network) {
		return userDao.findByNetwork(network);
	}

	// @Transactional(readOnly=true)
	// @ReadOnlyConnection
	public List<User> search(UserCriteria criteria, int offset, int limit) {
		return userDao.search(criteria, offset, limit);
	}

	@Transactional
	public void userRegistration(User entity, String serial) throws BackendServiceException {
		Device device = deviceDao.findBySerial(serial);
		if (device == null) {
			throw new BackendServiceException(Constants.Error.USER_WITH_INVALID_SERIAL_NUM, "invalid serial number");
		}
		if (!device.isActivated()) {
			throw new BackendServiceException(Constants.Error.USER_WITH_INVALID_SERIAL_NUM, "device not connected");
		}

		GroupWidget widget = groupWidgetDao.findByExternalId(serial, serial);
		if (widget == null) {
			throw new BackendServiceException(Constants.Error.USER_WITH_INVALID_SERIAL_NUM,
					"missing default visualization");
		}
		NetworkGroup group = widget.getGroup();
		if (group == null) {
			throw new BackendServiceException(Constants.Error.USER_WITH_INVALID_SERIAL_NUM,
					"missing group visualization");
		}
		NetworkGroup defaultGroup = widget.getNetwork().getDefaultGroup();
		if (defaultGroup == null) {
			throw new BackendServiceException(Constants.Error.USER_WITH_INVALID_SERIAL_NUM, "missing default group");
		}
		if (userDao.findByName(entity.getUsername()) != null) {
			throw new BackendServiceException(Constants.Error.USER_ALREADY_REGISTER,
					"user already exists " + entity.getUsername());
		}
		Role role = roleDao.findByName(Constants.ROLE_USER);
		entity.setAccountStatus(AccountStatus.ACTIVE);
		entity.setForcePasswordChange(false);
		entity.setOwner(device.getOwner());
		entity.addRole(role);
		entity.addGroup(group);
		entity.addGroup(defaultGroup);
		userDao.create(entity);
	}
	
	
	@Transactional
	public void restoreUserVisualizations(String userName, List<String> groups) {
//		logger.error("{} removing {} ", userName, groups);
		User user = userDao.findByName(userName);		

		

		if (user != null) {
//			for (NetworkGroup group: user.getGroups()) {
//				logger.error("{} {} {} {} {}", userName, group.getName(), group.getGroupType(), group.getId(), groups.contains(group.getId()));
//			}
			
			
			for (Iterator<NetworkGroup> iter = user.getGroups().iterator(); iter.hasNext(); ) {
				NetworkGroup group = iter.next();
			    if (group.getGroupType() != null && !groups.contains(group.getId()) && group.getGroupType().equals(NetworkGroupType.GROUP_WIDGET)) {
			        iter.remove();
			        logger.error("{} removed {} ", userName, group.getId());
			    }
			}
			userDao.update(user);			
		}


	}
	
	

}
