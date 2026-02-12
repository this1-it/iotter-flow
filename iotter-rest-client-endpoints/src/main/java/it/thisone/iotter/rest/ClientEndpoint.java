package it.thisone.iotter.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import it.thisone.iotter.backend.ClientUserService;
import it.thisone.iotter.cassandra.CassandraAuth;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.model.DataSink;
import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.DeviceType;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.IotDevice;
import it.thisone.iotter.rest.model.client.IotDeviceDetails;
import it.thisone.iotter.rest.model.client.IotDeviceId;
import it.thisone.iotter.rest.model.client.IotNetwork;
import it.thisone.iotter.rest.model.client.IotNetworkDetails;
import it.thisone.iotter.rest.model.client.IotSuperUserRegistration;
import it.thisone.iotter.rest.model.client.IotUser;
import it.thisone.iotter.rest.model.client.IotUserCreation;

@Component
public abstract class ClientEndpoint {
	public static final String NOT_YET_IMPLEMENTED = "not yet implemented";
	public static final boolean HIDDEN = false;

	@Autowired
	protected CassandraFeeds cassandraFeeds;
	@Autowired
	protected DeviceService deviceService;
	@Autowired
	protected UserService userService;
	@Autowired
	protected ClientUserService clientUserService;
	@Autowired
	protected CassandraAuth cassandraAuth;
	@Autowired
	protected NetworkService networkService;
	
	
	
	protected void checkAuthorization(String apiKey, String serial) throws RestServiceException {
		if (apiKey.startsWith("[") && apiKey.endsWith("]")) {
			User user = validateUserToken(apiKey);
			if (user != null && user.hasRole(Constants.ROLE_SUPERVISOR)) {
				return;
			}
		}
		
		if (!cassandraAuth.checkAuthorization(apiKey, serial)) {
			String msg = String.format("invalid user api key %s serial %s", apiKey, serial);
			throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, msg);
		}
	}

	protected SessionAuthentication validateSessionToken(String apiKey) throws RestServiceException {
		SessionAuthentication session = cassandraAuth.findAuthenticatedUser(apiKey);
		String msg = "";
		if (session == null) {
			msg = String.format("user token key %s", apiKey);
			throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, msg);
		}
		
		if (session.isExpired()) {
			msg = String.format("user token has expired %s", apiKey);
			throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, msg);
		}
		

		return session;
	}
	
	
	protected User validateUserToken(String apiKey) throws RestServiceException {
		String msg = "";
		SessionAuthentication tkn = validateSessionToken(apiKey);
		String username = tkn.getUsername();
		User user = userService.findByName(username);
		// it needs roles and network
		if (user == null) {
			msg = String.format("user not found %s", username);
			throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_FOUND, msg);
		}
		if (!user.getAccountStatus().equals(AccountStatus.ACTIVE)) {
			msg = String.format("user not active %s", username);
			throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, msg);
		}
		return user;
	}

	
	protected Device validateActivation(User user, IotDeviceId entity) throws RestServiceException{
		Device device = deviceService.findBySerial(entity.getSerial());
		if (device == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "device not found");
		}
		if (!device.getOwner().equals(user.getOwner()) && !device.isActivated()) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_NOT_AVAILABLE, "device not available for activation");
		}
		if (device.getNetwork() != null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_NOT_AVAILABLE, "device belongs to a network");
		}
		if (!device.getActivationKey().equals(entity.getActivationKey())) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_NOT_AVAILABLE, "wrong activation key");
		}
		return device;
	}
	

	protected void validateSuperUser(User user) throws RestServiceException {
		if (user.hasRole(Constants.ROLE_SUPERVISOR)) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED,
					"not an superuser");
		}
		else if (user.hasRole(Constants.ROLE_USER)) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED,
					"not an superuser");
		}
		else if (user.hasRole(Constants.ROLE_SUPERUSER)) {
			Network network = user.getNetwork();
			if (!network.getName().equals(user.getUsername())) {
				throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED,
						"user cannot manage this network:" + network.getName());
			}
		}
	}

	
	protected void validateReset(User user, IotUser entity) throws RestServiceException{
		if (entity == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED,
					"missing IotUser entity");
			
		}
		if (!entity.getUsername().equals(user.getUsername())) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED,
					"user cannot manage this user:" + entity.getUsername());
		}
	}
	
	protected void validate(IotSuperUserRegistration registration) throws RestServiceException {
		User tenant = userService.findByName(registration.getTenant());
		if (tenant == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.TENANT_NOT_FOUND, "tenant not found");
		}
		if (!tenant.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.TENANT_NOT_FOUND, "tenant not an administrator");
		}
		if (registration.getDevice() == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "device not found");
		}
		if (registration.getUser() == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_NOT_FOUND, "user not found");
		}
	}
	
	protected List<IotDevice> findDevices(User user) {
		List<IotDevice> items = new ArrayList<>();
		List<Device> devices = clientUserService.findDevices(user);
		for (Device device : devices) {
			IotDevice item = response(device);
			items.add(item);
		}
		
		return items;
	}

	protected IotDevice response(Device device) {
		IotDevice item = new IotDevice();
		item.setSerial(device.getSerial());
		IotDeviceDetails details = new IotDeviceDetails();
		details.setLabel(device.getLabel());
		details.setStatus(device.getStatus().name());
		if (device.getNetwork() != null) {
			IotNetwork network = new IotNetwork();
			network.setName(device.getNetwork().getName());
			network.setTenant(device.getNetwork().getOwner());
			IotNetworkDetails ndetails = new IotNetworkDetails();
			ndetails.setTimeZone(device.getNetwork().getTimeZone());
			network.setDetails(ndetails);
			item.setNetwork(network);
		}
		if (device.getMaster() != null) {
			item.setMaster(device.getMaster().getSerial());
			details.setType(DeviceType.VIRTUAL.name());
		}
		else {
			details.setType(DeviceType.REAL.name());
		}
		if (device.getModel() != null) {
			details.setModel(device.getModel().getName());
		}
		if (!device.getLocation().isUndefined()) {
			details.setLatitude(device.getLocation().getLatitude());
			details.setLongitude(device.getLocation().getLongitude());
		}
		item.setDetails(details);
		DataSink sink = cassandraFeeds.getDataSink(device.getSerial());
		if (sink != null && sink.getLastContact() != null) {
			item.getDetails().setLastContact(sink.getLastContact().getTime() / 1000);
		}

		return item;
	}
	
	
	protected IotUser userRegistration(IotUserCreation registration) throws RestServiceException {
		IotUser user = null;
		Network network = null;

//		if (network == null) {
//			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
//					Constants.Error.USER_INVALID_INPUT_ERROR_CODE, "user cannot be associated to a network");
//			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
//			
//			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
//					Constants.Error.TENANT_NOT_FOUND, "tenant not found");
//			
//			
//		}
//
//		try {
//			String email = request.getUsername();
//			User entity = new User();
//			entity.setUsername(request.getUsername());
//			entity.setEmail(email);
//			entity.setPassword(request.getPassword());
//			clientUserService.userRegistration(entity, network);
//		} catch (BackendServiceException e) {
//			logger.error(e.getMessage());
//			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(), e.getCode(), e.getMessage());
//			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
//		}		
		
		return user;
	
	}

	protected void validate(IotUserCreation registration) throws RestServiceException {
		User tenant = userService.findByName(registration.getNetwork().getTenant());
		if (tenant == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.TENANT_NOT_FOUND, "tenant not found");
		}
		if (!tenant.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.TENANT_NOT_FOUND, "tenant not an administrator");
		}
		if (registration.getUser() == null) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_NOT_FOUND, "user not found");
		}
		
	}	
	

	
	
}
