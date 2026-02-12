package it.thisone.iotter.rest;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.thisone.iotter.cassandra.model.ConfigurationRevision;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GroupWidget;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserToken;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.UserService;
import it.thisone.iotter.rest.model.DeviceConfiguration;
import it.thisone.iotter.rest.model.DeviceConfigurationSet;
import it.thisone.iotter.rest.model.DeviceConfigurationsWrapper;
import it.thisone.iotter.rest.model.RestErrorMessage;

@Path("/v1/client/device/configuration")
@Component
public class ClientDeviceConfigurationEndpoint {

	private static Logger logger = LoggerFactory
			.getLogger(ClientDeviceConfigurationEndpoint.class);

	@Autowired
	private CassandraService cassandraService;

	@Autowired
	private UserService userService;

	@Autowired
	private DeviceService deviceService;


	@Path("/{serial}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response readConfiguration(@PathParam("serial") String serial, //
			@HeaderParam("api-key") String apiKey) {
		UserToken tkn = userService.findToken(apiKey);
		if (tkn == null) {
			String msg = String.format(
					"readConfiguration invalid api key '%s' serial '%s' ",
					apiKey, serial);
			logger.error(msg);
			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(), Constants.Error.USER_NOT_FOUND, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}

		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			logger.error("device not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result)
					.build();
		}

		DeviceConfiguration cfg = cassandraService
				.deviceConfiguration(serial);
		if (cfg.getAttributes().isEmpty()) {
			String msg = String.format("missing device configuration %s",
					serial);
			logger.error(msg);

			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_CONF_NOT_FOUND_ERROR_CODE, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}

		DeviceConfigurationSet result = new DeviceConfigurationSet();
		result.setAttributes(cfg.getAttributes());
		result.setRevision(cfg.getRevision());
		result.setRevisionTime(cfg.getRevisionTime());
		result.setSerial(device.getSerial());
		result.setTz(device.getTimeZone());

		logger.debug("GET /v1/client/device/configuration/{}", serial);
		return Response.status(Response.Status.OK).entity(result).build();
	}

	@Path("/{serial}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response writeConfiguration(@PathParam("serial") String serial, //
			@HeaderParam("api-key") String apiKey, //
			DeviceConfigurationSet cfg) {

		UserToken tkn = userService.findToken(apiKey);
		if (tkn == null) {
			String msg = String.format(
					"writeConfiguration invalid api key '%s' serial '%s' ",
					apiKey, serial);
			logger.error(msg);
			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(), Constants.Error.USER_NOT_FOUND, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}

		DeviceConfiguration config = new DeviceConfiguration();
		config.setAttributes(cfg.getAttributes());
		int revision = cassandraService
				.writeClientConfiguration(serial, config);

		if (revision < 0) {
			String msg = String.format("missing device configuration %s",
					serial);
			logger.error(msg);

			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_CONF_NOT_FOUND_ERROR_CODE, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}

		DeviceConfigurationSet result = new DeviceConfigurationSet();
		result.setRevision(revision);
		logger.debug("POST /v1/client/device/configuration/{}", serial);
		return Response.status(Response.Status.CREATED).entity(result).build();
	}

	@Path("/{serial}/revision")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response revision(@PathParam("serial") String serial, //
			@HeaderParam("api-key") String apiKey) {
		UserToken tkn = userService.findToken(apiKey);
		if (tkn == null) {
			String msg = String.format("revision invalid api key %s serial %s",
					apiKey, serial);
			logger.error(msg);
			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(), Constants.Error.USER_NOT_FOUND, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}

		DeviceConfigurationSet result = new DeviceConfigurationSet();
		result.setRevision(-1);
		result.setAttributes(null);

		ConfigurationRevision cfgRev = cassandraService
				.deviceConfigurationRevision(serial);
		if (cfgRev != null) {
			result.setRevision(cfgRev.getRevision());
			result.setRevisionTime(cfgRev.getDate().getTime() / 1000);
		}
		logger.debug("GET /v1/client/device/configuration/{}/revision", serial);

		return Response.status(Response.Status.OK).entity(result).build();
	}

	@Path("/list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response configurations(@HeaderParam("api-key") String apiKey) {
		UserToken tkn = userService.findToken(apiKey);
		if (tkn == null) {
			String msg = String.format("configurations invalid api key '%s'",
					apiKey);
			logger.error(msg);
			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(), Constants.Error.USER_NOT_FOUND, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}

		User user = userService.findByName(tkn.getOwner());
		if (user == null) {
			String msg = String.format("invalid api key %s", apiKey);
			logger.error(msg);
			RestErrorMessage error = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(), Constants.Error.USER_NOT_FOUND, msg);
			return Response.status(Response.Status.NOT_FOUND).entity(error)
					.build();
		}
		
		Map<Device, Set<GroupWidget>> map = deviceService.findMappableDevices(user.getGroups());
		DeviceConfigurationsWrapper result = new DeviceConfigurationsWrapper();

		for (Device device : map.keySet()) {
			DeviceConfiguration req = cassandraService.deviceConfiguration(device.getSerial());
			if (req.getRevision() > 0) {
				DeviceConfigurationSet set = new DeviceConfigurationSet();
				set.setRevisionTime(req.getRevisionTime());
				set.setRevision(req.getRevision());
				set.setAttributes(req.getAttributes());
				set.setSerial(device.getSerial());
				set.setTz(device.getTimeZone());
				result.getConfigurations().add(set);
			}
		}
		logger.debug("GET /v1/client/device/configuration/list, user: {}", user.getUsername());
		return Response.status(Response.Status.OK).entity(result).build();
	}

	
	
}
