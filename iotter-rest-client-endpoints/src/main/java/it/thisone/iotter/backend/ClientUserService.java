package it.thisone.iotter.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.thisone.iotter.cassandra.CassandraAuth;
import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.ifc.IDeviceDao;
import it.thisone.iotter.persistence.ifc.INetworkDao;
import it.thisone.iotter.persistence.ifc.INetworkGroupDao;
import it.thisone.iotter.persistence.ifc.IRoleDao;
import it.thisone.iotter.persistence.ifc.IUserDao;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.rest.model.client.IotDeviceDetails;
import it.thisone.iotter.rest.model.client.IotDeviceId;
import it.thisone.iotter.rest.model.client.IotLogin;
import it.thisone.iotter.rest.model.client.IotNetwork;
import it.thisone.iotter.rest.model.client.IotUserDetails;

@Service
public class ClientUserService {

	public static Logger logger = LoggerFactory.getLogger(ClientUserService.class);

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private CassandraAuth cassandraAuth;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private INetworkGroupDao groupDao;

	@Autowired
	private IUserDao userDao;

	@Autowired
	private IRoleDao roleDao;

	@Autowired
	private IDeviceDao deviceDao;

	@Autowired
	private INetworkDao networkDao;
	
	@Transactional
	public void createAdministrator(User entity, String defaultNetworkName)
			throws BackendServiceException {
		
		if (userDao.findByName(entity.getUsername()) == null) {
			Role role = roleDao.findByName(Constants.ROLE_ADMINISTRATOR);
			entity.addRole(role);
			entity.setOwner(entity.getUsername());
			entity.setAccountStatus(AccountStatus.ACTIVE);
			userDao.create(entity);
		}
		
		
		
		Network network = networkDao.findDefaultNetwork(entity.getUsername());
		//Network network = networkDao.findByName(defaultNetworkName, entity.getUsername());
		if (network == null) {
			network = new Network();
			network.setName(defaultNetworkName);
			network.setExternalId(defaultNetworkName);
			network.setDefaultNetwork(true);
			network.setOwner(entity.getUsername());
			network.setNetworkType(NetworkType.GEOGRAPHIC);
			networkDao.create(network);			
		}
		else {
			network.setName(defaultNetworkName);
			networkDao.update(network);	
		}
		
	}

	@Transactional
	public Network getNetwork(String networkName, String owner) throws BackendServiceException {
		Network network = networkDao.findByName(networkName, owner);
		if (network == null) {
			throw new BackendServiceException(Constants.Error.NETWORK_NOT_FOUND, String.format("not found network %s with owner %s", networkName, owner));
		}
		return network;
	}		
	
	

	@Transactional
	public void superUserRegistration(User entity, String tenant, IotDeviceId deviceId, IotDeviceDetails details)
			throws BackendServiceException {
		
		String username = entity.getUsername();
		entity.setOwner(tenant);
		validateUser(entity);
		
		Device device = validateDevice(deviceId.getSerial(), deviceId.getActivationKey(), tenant);

		if (device.getNetwork() != null) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_AVAILABLE,
					deviceId.getSerial() + " device not available, since is joined to a network " + device.getNetwork().getName());
		}
		

		String networkName = username;
		if (networkDao.findByName(networkName, tenant) != null) {
			throw new BackendServiceException(Constants.Error.NETWORK_ALREADY_REGISTER,
					String.format("%s network already exists for administrator %s", networkName, tenant));
		}

		
		Network network = new Network();
		network.setName(networkName);
		network.setExternalId(networkName);
		network.setOwner(tenant);
		network.setNetworkType(NetworkType.GEOGRAPHIC);
		networkDao.create(network);

		Role superuser = roleDao.findByName(Constants.ROLE_SUPERUSER);
		// entity.setAccountStatus(AccountStatus.NEED_ACTIVATION);
		entity.setAccountStatus(AccountStatus.ACTIVE);
		entity.setForcePasswordChange(true);
		entity.setOwner(tenant);
		entity.addRole(superuser);
		entity.addGroup(network.getDefaultGroup());

//		Calendar calendar = Calendar.getInstance();
//		calendar.add(Calendar.MINUTE, 30);
//		entity.setExpiryDate(calendar.getTime());
		userDao.create(entity);

		Set<NetworkGroup> groups = new HashSet<NetworkGroup>();
		groups.add(network.getDefaultGroup());
		device.setGroups(groups);
		
		if (details != null) {
			device.setLabel(details.getLabel());
			device.getLocation().setLatitude(details.getLatitude());
			device.getLocation().setLongitude(details.getLongitude());
		}
		deviceDao.update(device);

	}

	public void validateUser(User entity) throws BackendServiceException {
		if (entity.getUsername().trim().length() < Constants.Validators.MIN_USERNAME_LENGTH) {
			throw new BackendServiceException(
					Constants.Error.USER_NOT_FOUND, "username too short");
		}
		if (entity.getPassword().trim().length() < Constants.Validators.MIN_PASSWORD_LENGTH) {
			throw new BackendServiceException(
					Constants.Error.USER_NOT_FOUND, "password too short");
		}
		if (userDao.findByName(entity.getUsername()) != null) {
			throw new BackendServiceException(Constants.Error.USER_ALREADY_REGISTER, "user already exists " + entity.getUsername());
		}

	}

	@Transactional
	public void superUserRemoval(String username) throws BackendServiceException {
		User user = userDao.findByName(username);
		if (!user.hasRole(Constants.ROLE_SUPERUSER)) {
			throw new BackendServiceException("not a superuser !");
		}
		Network network = user.getNetwork();
		if (!network.getName().equals(username)) {
			throw new BackendServiceException("superuser cannot remove network");
		}
		List<User> users = userDao.findByNetwork(network);
		if (users.size() > 1) {
			throw new BackendServiceException("users found ! remove them first");
		}
		List<Device> devices = deviceDao.findByNetwork(network);
		if (!devices.isEmpty()) {
			throw new BackendServiceException("devices found ! deactivate them first");
		}
		List<NetworkGroup> groups = groupDao.findByNetwork(network);
		for (NetworkGroup group : groups) {
			groupDao.removeMembers(group);
		}
		networkDao.deleteById(network.getId());
		userDao.deleteById(user.getId());
	}
	
	@Transactional
	public void userRegistration(User entity, Network network) throws BackendServiceException {
		validateUser(entity);
		Role role = roleDao.findByName(Constants.ROLE_USER);
		entity.addRole(role);
		entity.setAccountStatus(AccountStatus.ACTIVE);
		entity.setOwner(network.getOwner());
		entity.getGroups().add(network.getDefaultGroup());
		userDao.create(entity);
	}
	
	@Transactional
	public void userRemoval(String username) throws BackendServiceException {
		User user = userDao.findByName(username);
		if (!user.hasRole(Constants.ROLE_USER)) {
			throw new BackendServiceException("not a user !");
		}
		userDao.deleteById(user.getId());
	}
	

	public Device validateDevice(String deviceSerial, String deviceActivation, String owner)
			throws BackendServiceException {
		Device device = deviceDao.findBySerial(deviceSerial);
		
		if (device == null) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "device not found");
		}
		
		if (!device.isActivated()) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_AVAILABLE,
					"device has wrong status, not activated");
		}
		if (!device.getOwner().equals(owner) ) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_AVAILABLE,
					"device has wrong owner");
		}

		if (device.getMaster() != null) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_AVAILABLE,
					"device has master ");
		}

		if (deviceActivation == null) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_AVAILABLE, "missing activation key");
		}

		if (deviceActivation != null && !deviceActivation.equals(device.getActivationKey())) {
			throw new BackendServiceException(Constants.Error.DEVICE_NOT_AVAILABLE, "wrong activation key");
		}
		return device;
	}

	public void enableDevice(String deviceSerial, String deviceActivation, String owner, Network network)
			throws BackendServiceException {
		Device device = validateDevice(deviceSerial, deviceActivation, owner);
		deviceService.connect(device, network);
	}

	@Transactional
	public void disableDevice(String deviceSerial, String deviceActivation, String owner)
			throws BackendServiceException {
		Device device = validateDevice(deviceSerial, deviceActivation, owner);
		deviceService.disconnect(device, true);
		
	}
	
	public List<Device> findDevices(User user) {
		List<Device> devices = new ArrayList<>();
		if (user.hasRole(Constants.ROLE_SUPERVISOR)) {
			devices = deviceService.findAll();
		} else if (user.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			devices = deviceService.findByOwner(user.getUsername());
		}
		else {
			if (user.getNetwork() != null) {
				devices = deviceService.findByNetwork(user.getNetwork());
			}
		}
		return devices;
	}
	
	@Transactional
	public void createUser(IotLogin login, IotUserDetails details, IotNetwork network) throws BackendServiceException {
		User user = new User();
		user.setUsername(login.getUsername());
		user.setPassword(login.getPassword());
		user.setEmail(details.getEmail());
		user.setFirstName(details.getFirstName());
		user.setLastName(details.getLastName());
		Network networkEntity = getNetwork(network.getName(), network.getTenant());
		userRegistration(user, networkEntity);
	}

	public void resetData(String serial) {
		Device device = deviceService.findBySerial(serial);
		subscriptionService.resetData(device);
	}

	//	@Async has two limitations:
	//	It must be applied to public methods only
	//	Self-invocation — calling the async method from within the same class — won’t work
	
	@Async
	public void grantAuthorizations(User user, SessionAuthentication tkn, int ttl) {
		List<Device> devices = this.findDevices(user);
		for (Device device : devices) {
			//if (device.getStatus().equals(DeviceStatus.CONNECTED)) {
				cassandraAuth.createAuthorizationGrant(tkn.getToken(), device.getSerial(), ttl);
			//}
			
		}
	}
	
	
}
