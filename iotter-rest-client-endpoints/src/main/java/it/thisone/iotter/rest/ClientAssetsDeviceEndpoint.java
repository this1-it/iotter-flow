package it.thisone.iotter.rest;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.backend.ClientUserService;
import it.thisone.iotter.cassandra.CassandraAuth;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.GeoLocation;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.IotDevice;
import it.thisone.iotter.rest.model.client.IotDeviceId;
import it.thisone.iotter.rest.model.client.IotDeviceSet;

@Path("/v1/client/assets/device")
@Api(value="/v1/client/assets/device", tags={"v1-client-assets-device"}, hidden=ClientEndpoint.HIDDEN)
@Component
public class ClientAssetsDeviceEndpoint extends ClientEndpoint {

	private static Logger logger = LoggerFactory.getLogger(ClientAssetsDeviceEndpoint.class);
	@Autowired
	private ClientUserService clientUserService;
	@Autowired
	private CassandraAuth cassandraAuth;
	@Autowired
	private TracingService tracingService;
	
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="list-devices",value = "client lists managed devices", notes = "List of available devices for a client", response= IotDeviceSet.class)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful list retrieving", response= IotDeviceSet.class),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response devices(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey) {
		IotDeviceSet set = new IotDeviceSet();
		try {
			User user = validateUserToken(apiKey);
			set.setValues(findDevices(user));
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(),
					e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		return Response.status(Response.Status.OK).entity(set).build();
	}

	
	@PUT
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="update-device",value = "client updates device details", notes = "A client updates details for one of its managed devices")
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "successfull details updated"),
		    @ApiResponse(code = 401, message = "unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 404, message = "not found", response= RestErrorMessage.class),
		    @ApiResponse(code = 406, message = "invalid configuration", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "internal server error")
		    })
	public Response deviceUpdate(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(value = "device details", required = true, name = "entity") 
			@RequestBody @Valid	
			IotDevice request) {

		User user = null;
		try {
			user = validateUserToken(apiKey);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(),
					e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		Device entity = deviceService.findBySerial(request.getSerial());
		if (entity == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_AVAILABLE,"device not found");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		
		if (request.getDetails().getLabel()!= null && ! request.getDetails().getLabel().isEmpty()) {
			entity.setLabel(request.getDetails().getLabel());
		}
		
		GeoLocation location = new GeoLocation();
		location.setLatitude(request.getDetails().getLatitude());
		location.setLongitude(request.getDetails().getLongitude());
		
		if (!location.isUndefined()) {
			entity.setLocation(location);
		}
		
		try {
			deviceService.update(entity);
			tracingService.trace(TracingAction.DEVICE_UPDATE, user.getUsername(), entity.getOwner(), null, null, "/v1/client/assets/device/update");
		} catch (BackendServiceException e) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					e.getCode(),e.getMessage());
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		
		logger.debug("PUT /v1/client/assets/device/update/{}", request.getSerial());

		return Response.status(Response.Status.OK).build();
	}
	
	
	@PUT
	@Path("/disable")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="disable-device",value = "client disables a device", notes = "factory reset for a device, activation key is mandatory, "
			+ "after device will not be available in listing")
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful disabling"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response disable(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(value = "device identification", required = true, name = "entity") 
			@RequestBody @Valid	IotDeviceId entity) {
		User user = null;
		try {
			user = validateUserToken(apiKey);
			validateSuperUser(user);
		} catch (RestServiceException e) {
			logger.error("validate disable {} failed {} ", entity.getSerial(), e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(),
					e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		try {
			clientUserService.disableDevice(entity.getSerial(), entity.getActivationKey(), user.getOwner());
			clientUserService.resetData(entity.getSerial());
			cassandraAuth.deleteAuthorizationGrant(apiKey,entity.getSerial());		
			tracingService.trace(TracingAction.DEVICE_UPDATE, user.getUsername(), user.getOwner(), null, entity.getSerial(), "/v1/client/assets/device/disable");
		} catch (BackendServiceException e) {
			logger.error("disable {} failed {} ", entity.getSerial(), e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_NOT_AVAILABLE, e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		logger.debug("PUT /v1/client/assets/device/disable/{}", entity.getSerial());

		return Response.status(Response.Status.OK).build();
	}

	


	@PUT
	@Path("/enable")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="enable-device",value = "client enables device", notes = " activation key is mandatory, "
			+ "after device will be available in listing")
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful enabling"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response enable(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(value = "device identification", required = true, name = "entity") 
			@RequestBody @Valid	
			IotDeviceId entity
			) {
		User user = null;
		try {
			user = validateUserToken(apiKey);
			validateActivation(user, entity);
			validateSuperUser(user);
		} catch (RestServiceException e) {
			logger.error("validate enable {} failed {} ", entity.getSerial(), e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		
		try {
			clientUserService.enableDevice(entity.getSerial(), entity.getActivationKey(), user.getOwner(), user.getNetwork());
			int ttl = Constants.USER_TOKEN_HOURS * 60 * 60;
			cassandraAuth.createAuthorizationGrant(apiKey,entity.getSerial(),ttl);
			tracingService.trace(TracingAction.DEVICE_UPDATE, user.getUsername(), user.getOwner(), user.getNetwork().getName(), entity.getSerial(), "/v1/client/assets/device/enable");
		} catch (BackendServiceException e) {
			logger.error("enable {} failed {} ", entity.getSerial(), e.getMessage());
			tracingService.traceRestError(e.toString(), user.getOwner(), user.getNetwork().getName(), entity.getSerial(), e.getMessage(), null);
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.DEVICE_NOT_AVAILABLE, e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		logger.debug("PUT /v1/client/assets/device/disable/{}", entity.getSerial());
		return Response.status(Response.Status.OK).build();

	}
	
	
	
}
