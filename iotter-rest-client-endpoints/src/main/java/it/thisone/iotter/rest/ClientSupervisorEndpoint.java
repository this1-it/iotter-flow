package it.thisone.iotter.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//import it.thisone.iotter.cassandra.CassandraDsbulk;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.NetworkType;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.RecoveryService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.model.Role;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.RoleService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.util.EncryptUtils;

/*
 * this service is used for testing purpose
 * http://www.restapitutorial.com/lessons/httpmethods.html
 * http://www.codingpedia.org/ama/error-handling-in-rest-api-with-jersey
 */

@Path("/v1/client/supervisor")
@Component
public class ClientSupervisorEndpoint extends ClientEndpoint {

	// @Autowired
	// private CassandraDsbulk cassandraDsbulk;

	
	@Autowired
	private RecoveryService recoveryService;

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private NetworkService networkService;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/create/admin/")
	// String username, String password, String email, String firstName, String
	// lastName
	public Response createAdministrator(@QueryParam("owner") String owner, @QueryParam("password") String password,
			@QueryParam("email") String email, @QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName

	) {

		if (password == null || password.isEmpty()) {
			password = owner;
		}
		if (email == null || email.isEmpty()) {
			email = owner;
		}

		User admin = userService.findByName(owner);
		if (admin != null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("user " + owner + " already exists").build();
		}

		Role role = roleService.findByName(Constants.ROLE_ADMINISTRATOR);
		admin = userService.safeCreateUser(owner, password, email, AccountStatus.ACTIVE, firstName, lastName, role,
				null, owner);

		if (admin == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("user " + owner + " not created").build();
		}

		try {
			networkService.createDefaultNetwork(admin);
		} catch (BackendServiceException e) {
		}

		return Response.status(Response.Status.CREATED).entity(owner + " created").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/delete/admin/")
	public Response deleteAdministrator(@QueryParam("owner") String owner) {
		userService.deleteOwner(owner);
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/device/read/")
	public Response readDevice(@QueryParam("serial") String serial) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("serial " + serial + " not found").build();
		}
		return Response.status(Response.Status.OK).entity(response(device)).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/device/create/")
	public Response createDevice(@QueryParam("label") String label, @QueryParam("activation") String activation,
			@QueryParam("serial") String serial, @QueryParam("owner") String owner) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			device = new Device();
			device.setLabel(label);
			device.setSerial(serial);
			device.setActivationKey(activation);
			device.setOwner(owner);
			device.setStatus(DeviceStatus.PRODUCED);
			String writeApike = EncryptUtils.createWriteApiKey(serial);
			device.setWriteApikey(writeApike);
			device.setReadApikey(new StringBuilder(writeApike).reverse().toString());

			deviceService.create(device);
		}
		return Response.status(Response.Status.CREATED).entity(response(device)).build();
	}

	@GET
	@Path("/device/delete/")
	public Response deleteDevice(@QueryParam("serial") String serial) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			return Response.status(Response.Status.OK).build();
		}
		deviceService.delete(device);
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/device/activate/")
	public Response activateDevice(@QueryParam("serial") String serial, @QueryParam("owner") String owner,
			@QueryParam("activation") String activation, @QueryParam("label") String label

	) {
		Device device = deviceService.findBySerial(serial);

		if (label == null || label.isEmpty()) {
			label = serial;
		}

		if (device == null) {
			return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s not found", serial)).build();
		}
		if (device.getOwner().equals(owner)) {
			return Response.status(Response.Status.OK).entity(String.format("%s already owned by %s", serial, owner))
					.build();
		}

		User admin = userService.findByName(owner);
		if (admin == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(String.format("%s not found", owner)).build();
		} else {
			if (!admin.hasRole(Constants.ROLE_ADMINISTRATOR)) {
				return Response.status(Response.Status.BAD_REQUEST).entity(String.format("%s not administrator", owner))
						.build();
			}
		}

		try {

			if (!device.getOwner().equals(Constants.ROLE_PRODUCTION.toLowerCase())) {
				device.setGroups(new HashSet<NetworkGroup>());
				deviceService.update(device);
				subscriptionService.factoryReset(serial, activation);
			}

			Network network = networkService.findByName(Constants.DEFAULT_NETWORK, owner);
			if (network == null) {
				network = new Network();
				network.setName(Constants.DEFAULT_NETWORK);
				network.setOwner(owner);
				network.setNetworkType(NetworkType.GEOGRAPHIC);
				networkService.create(network);
			}

			device = userService.deviceActivation(serial, activation, owner, network);
			device.setLabel(label);
			deviceService.update(device);
		} catch (BackendServiceException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(String.format("%s not activated: %s ", serial, e.getMessage())).build();
		}

		return Response.status(Response.Status.OK).entity(String.format("%s activated by %s", serial, owner)).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/create/user/")
	public Response createUser(@QueryParam("user") String userName, @QueryParam("owner") String owner,
			@QueryParam("role") String roleName, @QueryParam("network") String networkName) {
		// Role role = roleService.findByName(Constants.ROLE_DEMO);
		if (roleName == null) {
			roleName = Constants.ROLE_USER;
		}
		if (networkName == null) {
			networkName = Constants.DEFAULT_NETWORK;
		}
		Role role = roleService.findByName(roleName);
		if (role == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("no role, user " + userName + " not created")
					.build();
		}
		NetworkGroup group = null;
		Network network = null;
		try {
			network = networkService.findByName(Constants.DEFAULT_NETWORK, owner);
		} catch (BackendServiceException e) {
		}
		if (network != null) {
			group = network.getDefaultGroup();
		} else {
			return Response.status(Response.Status.NOT_FOUND).entity("network " + networkName + " not found").build();
		}
		User user = userService.safeCreateUser(userName, userName, null, AccountStatus.ACTIVE, userName, userName, role,
				group, owner);
		if (user == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("user " + userName + " not created").build();
		}
		return Response.status(Response.Status.CREATED).entity(owner).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/delete/user/")
	public Response deleteUser(@QueryParam("user") String userName) {
		User user = userService.findByName(userName);
		if (user == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(String.format("%s not found", userName)).build();
		}

		if (user.hasRole(Constants.ROLE_ADMINISTRATOR)) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(String.format("%s has administrator role", userName)).build();
		}

		userService.deleteById(user.getId());
		return Response.status(Response.Status.OK).entity(String.format("%s has been deleted", userName)).build();
	}

	@GET
	@Path("/device/reset/")
	public Response resetDevice(@QueryParam("serial") String serial) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s serial not found", serial))
					.build();
		}
		try {
			device.setGroups(new HashSet<NetworkGroup>());
			deviceService.update(device);
			subscriptionService.factoryReset(serial, device.getActivationKey());
		} catch (BackendServiceException e) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(String.format("%s reset failure: %s ", serial, e.getMessage())).build();
		}
		return Response.status(Response.Status.OK).entity(String.format("%s reset in progress", serial)).build();
	}

	@GET
	@Path("/device/rebuild/")
	public Response rebuildDevice(@QueryParam("serial") String serial) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s serial not found", serial))
					.build();
		}
		subscriptionService.rebuildMasterDeviceAsync(serial);
		return Response.status(Response.Status.OK).entity(String.format("%s rebuild in progress", serial)).build();
	}

	@GET
	@Path("/device/checksum/")
	public Response checkSumDevice(@QueryParam("serial") String serial) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s serial not found", serial))
					.build();
		}
		subscriptionService.checkSumDevice(device);
		return Response.status(Response.Status.OK).entity(String.format("%s checksum in progress", serial)).build();
	}

//	@GET
//	@Path("/device/aernetpro/")
//	public Response aernetpro(@QueryParam("serial") String serial) {
//		Device device = deviceService.findBySerial(serial);
//		if (device == null) {
//			return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s serial not found", serial)).build();
//		}
//		if (device.getMaster() != null) {
//			recoveryService.aernetpro(serial);
//		}
//		else {
//			List<Device> slaves = deviceService.findSlaves(device);
//			for (Device slave : slaves) {
//				recoveryService.aernetpro(slave.getSerial());
//			}
//		}
//		return Response.status(Response.Status.OK).entity(String.format("%s aernetpro checked", serial)).build();
//	}

	@GET
	@Path("/device/deactivate/")
	public Response deactivateDevice(@QueryParam("serial") String serial, @QueryParam("blocked") boolean blocked) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			return Response.status(Response.Status.NOT_FOUND).entity(String.format("%s serial not found", serial))
					.build();
		}
		boolean changed = deviceService.deactivateDevice(device, blocked);
		return Response.status(Response.Status.OK).entity(String.format("deactivate %s  changed %s", serial, changed))
				.build();
	}

	@GET
	@Path("/fix_users_visualizations/")
	public Response fix_users_visualizations() {
		recoveryService.fix_users_visualizations();
		return Response.status(Response.Status.OK).entity("fix_users_visualizations in progress").build();
	}

	@GET
	@Path("/all_connected/")
	public Response all_connected() {
		recoveryService.all_connected();
		return Response.status(Response.Status.OK).entity("all_connected in progress").build();
	}

	@GET
	@Path("/fix_alarmed/")
	public Response fix_alarmed() {
		recoveryService.fix_alarmed();
		return Response.status(Response.Status.OK).entity("fix_alarmed in progress").build();
	}

	@GET
	@Path("/fix_aernetpro/")
	public Response fix_aernetpro() {
		recoveryService.fix_aernetpro();
		return Response.status(Response.Status.OK).entity("fix_aernetpro in progress").build();
	}

	@POST
	@Path("/restore_user_visualizations/")
	public Response restore_user_visualizations(@FormParam("username") String username,
			@FormParam("groups") String groups) {
		recoveryService.restore_user_visualizations(username, Arrays.asList(groups.split(",")));
		return Response.status(Response.Status.OK).entity(String.format("restore_user_visualization %s  ", username))
				.build();
	}
	
	// Bug #2035
	@POST
	@Path("/fix_alarm_status/")
	public Response fix_alarm_status(@FormParam("owner") String owner) {
		recoveryService.fix_alarm_status(owner);
		return Response.status(Response.Status.OK).entity(String.format("fix_alarm_status %s  ", owner))
				.build();
	}


	/*
	 * curl http://localhost:8080/iotter/rest/v1/client/supervisor/ehcache?mode=clear
	 */
	@GET
	@Path("/ehcache")
	public Response cacheStatistics(@QueryParam("mode") String mode) {
		return Response.status(Response.Status.OK).entity(subscriptionService.cacheStatistics(mode)).build();
	}

	// @GET
	// @Path("/device/dsbulk_unload/")
	// public Response dsbulk_unload(@QueryParam("serial") String serial,@QueryParam("lower") String lowerString, @QueryParam("upper") String upperString) {
	// 	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// 	Date lower = null;
	// 	Date upper = new Date();
		
	// 	try {
	// 		lower = sdf.parse(lowerString);
	// 	} catch (ParseException e) {
	// 	}
	// 	try {
	// 		upper = sdf.parse(upperString);
	// 	} catch (ParseException e) {
	// 	}		

	// 	String command = cassandraDsbulk.unload_device(serial, lower, upper);
	// 	return Response.status(Response.Status.OK).entity(String.format("dsbulk_unload: %s  ", command))
	// 			.build();
	// }
	
}
