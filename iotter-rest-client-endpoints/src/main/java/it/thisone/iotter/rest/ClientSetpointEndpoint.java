package it.thisone.iotter.rest;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.cassandra.CassandraAlarms;
import it.thisone.iotter.cassandra.CassandraFeeds;
import it.thisone.iotter.cassandra.model.Feed;
import it.thisone.iotter.cassandra.model.FeedKey;
import it.thisone.iotter.cassandra.model.MeasureRaw;
import it.thisone.iotter.concurrent.RateLimitControl;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.modbus.Permission;
import it.thisone.iotter.mqtt.MqttOutboundService;
import it.thisone.iotter.mqtt.MqttServiceException;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.ModbusRegister;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.IotSetpoint;
import it.thisone.iotter.rest.model.client.IotSetpointWrite;
import it.thisone.iotter.util.BacNet;

@Path("/v1/client/setpoint/{serial}")
@Api(value = "/v1/client/setpoint/{serial}", tags = { "v1-client-setpoint" })
@Component
public class ClientSetpointEndpoint extends ClientEndpoint {

	private static final int LOOP_COUNT = 4;
	private static final int LOOP_PAUSE = 5;

	private static Logger logger = LoggerFactory.getLogger(ClientSetpointEndpoint.class);

	@Autowired
	private CassandraFeeds cassandraFeeds;

	@Autowired
	private CassandraAlarms cassandraAlarms;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private MqttOutboundService outboundService;

	@Autowired
	@Qualifier("rateLimitControl")
	private RateLimitControl rateLimitControl;

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "write-setpoint", value = "RW setpoint write", 
			notes = "this endpoint behaves like the remote control user interface: "
			+ "the value must be within the min / max limits and is converted to be compatible with aernet device; "
			+ "after the change request has been published on the mqtt broker, "
			+ "it is checked whether the aernet device applies the requested change;"
			+ "if change does not occur in 20 secs, a timeout response is returned")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "successful setpoint write"),
			@ApiResponse(code = 408, message = "timeout", response = RestErrorMessage.class),
			@ApiResponse(code = 401, message = "unathorized", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "setpoint not found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "setpoint value not acceptable", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "internal server error") })
	public Response setpointWrite(
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial, //
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey, //
			@ApiParam(value = "setpoint write", required = true, name = "message") @RequestBody @Valid IotSetpointWrite message) {

		try {
			checkAuthorization(apiKey, serial);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(), e.getCode(),
					e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		acquireRateLimitPermit(serial);
		Channel channel = null;
		try {
			channel = findChannel(serial, message.getId());
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), e.getCode(),
					e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).entity(error).build();
		}
		try {
			if (!handleMessage(message, channel)) {
				RestErrorMessage error = new RestErrorMessage(Response.Status.REQUEST_TIMEOUT.getStatusCode(), "");
				return Response.status(Response.Status.REQUEST_TIMEOUT).entity(error).build();
			}
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					e.getMessage());
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		logger.debug("POST /v1/client/setpoint/{} with {}", serial, message.getId());
		return Response.status(Response.Status.CREATED).build();
	}

	private boolean handleMessage(IotSetpointWrite message, Channel channel) throws RestServiceException {
		if (message.getValue() > channel.getRemote().getMax() || message.getValue() < channel.getRemote().getMin()) {
			String msg = String.format("RW setpoint parameter %s invalid value", message.getId());
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_CONF_ERROR_CODE, msg);
		}
		MeasureUnit unit = channel.getDefaultMeasure();
		BigDecimal value = unit.calculateRaw(message.getValue());
		FeedKey feedKey = new FeedKey(channel.getDevice().getSerial(), channel.getKey());
		MeasureRaw measure = cassandraFeeds.getLastMeasure(feedKey);
		if (measure != null && measure.getValue() != null) {
			if (measure.getValue().equals(value.floatValue())) {
				return true;
			}
		}

		try {
			outboundService.setValue(channel.getRemote().getTopic(), value);
		} catch (MqttServiceException e) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_CONF_ERROR_CODE, e.getMessage());
		}
		Permission permission = getPermission(channel);
		int i = 0;
		boolean done = false;

		while (i <= LOOP_COUNT) {
			i++;
			try {
				Thread.sleep(LOOP_PAUSE * 1000);
			} catch (InterruptedException e) {
			}
			if (permission.equals(Permission.READ_WRITE)) {
				measure = cassandraFeeds.getLastMeasure(feedKey);
				if (measure != null && measure.getValue() != null) {
					if (measure.getValue().equals(value.floatValue())) {
						done = true;
						break;
					}
				}
			} else if (permission.equals(Permission.WRITE)) {
				int count = cassandraAlarms.countActiveAlarms(feedKey.getSerial());
				if (count == 0) {
					done = true;
					break;
				}
			}
		}
		return done;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "read-setpoint", value = "RW setpoint read", 
	notes = "this endpoint returns the remote control configuration and the last applied value", 
	response = IotSetpoint.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "successful setpoint read", response = IotSetpoint.class),
			@ApiResponse(code = 401, message = "unathorized", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "setpoint not found", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "internal server error") })
	public Response setpointRead(
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial, //
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey, //
			@ApiParam(name = "id", value = "parameter id", required = true) @QueryParam(value = "id") String id) {

		IotSetpoint result = new IotSetpoint();
		try {
			checkAuthorization(apiKey, serial);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(), e.getCode(),
					e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		try {
			Channel channel = findChannel(serial, id);
			result.setId(id);
			result.setSerial(serial);
			result.setTopic(channel.getRemote().getTopic());
			result.setMin(channel.getRemote().getMin());
			result.setMax(channel.getRemote().getMax());
			result.setPermission(getPermission(channel).getShortName());
			Feed feed = cassandraFeeds.getFeed(serial, channel.getKey());
			if (feed != null) {
				MeasureUnit unit = channel.getDefaultMeasure();
				Float value = unit.convert(feed.getValue());
				result.setUnit(BacNet.lookUp(unit.getType()));
				if (feed.getDate() != null) {
					result.setValue(value);
					result.setTs(feed.getDate().getTime() / 1000);
				}
			}
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), e.getCode(),
					e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).entity(error).build();
		}
		logger.debug("GET /v1/client/setpoint/{} with {}", serial, id);
		return Response.status(Response.Status.OK).entity(result).build();
	}

	private Channel findChannel(String serial, String id) throws RestServiceException {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			String msg = String.format("device not found with serial %s", serial);
			throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(),
					Constants.Error.DEVICE_NOT_AVAILABLE, msg);
		}
		for (Channel channel : device.getChannels()) {
			if (channel.getNumber().equals(id) && channel.getConfiguration().isActive()
					&& channel.getRemote().isValid()) {
				return channel;
			}
		}
		String msg = String.format("RW setpoint parameter %s not found with serial %s", id, serial);
		throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.DEVICE_NOT_AVAILABLE, msg);
	}

	private Permission getPermission(Channel channel) {
		Permission permission = Permission.READ_WRITE;
		if (channel.getRemote().getPermission() != null) {
			ModbusRegister register = deviceService.getModbusRegister(channel.getOid());
			if (register != null) {
				String sectionId = register.getAdditionalProperties().get("aernetpro");
				if (sectionId != null) {
					if (sectionId.contains("reset")) {
						return Permission.WRITE;
					}
				}
				return register.getPermission();
			} else {
				String literal = channel.getRemote().getPermission().toUpperCase();
				for (Permission value : Permission.values()) {
					if (value.getShortName().equals(literal)) {
						return value;
					}
				}
			}

		}
		return permission;
	}

	/**
	 * Acquires a permit from rate limiter, blocking until one is available, or the
	 * thread is interrupted.
	 **/
	private void acquireRateLimitPermit(String clientId) {
		rateLimitControl.getRateLimiter(clientId, 1, LOOP_COUNT * LOOP_PAUSE, TimeUnit.SECONDS).get().acquire();
	}

}
