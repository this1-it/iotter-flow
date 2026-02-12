package it.thisone.iotter.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.cassandra.InterpolationUtils;
import it.thisone.iotter.cassandra.model.ConfigurationRegistry;
import it.thisone.iotter.concurrent.RateLimitControl;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.DeviceStatus;
import it.thisone.iotter.enums.MeasureQualifier;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.CassandraService;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Channel;
import it.thisone.iotter.persistence.model.ChannelAlarm;
import it.thisone.iotter.persistence.model.ChannelRemoteControl;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.MeasureUnit;
import it.thisone.iotter.persistence.model.MeasureUnitType;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.NetworkGroup;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.persistence.service.NetworkService;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.rest.model.ConfigAttribute;
import it.thisone.iotter.rest.model.DeviceAcknowledge;
import it.thisone.iotter.rest.model.DeviceConfiguration;
import it.thisone.iotter.rest.model.NetworkConfiguration;
import it.thisone.iotter.rest.model.ParamConfiguration;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.security.EncryptionInitializationException;
import it.thisone.iotter.util.EncryptUtils;
import it.thisone.iotter.util.Utils;

@Path("/v1/device/{serial}/configuration")
@Api(value = "/v1/device/{serial}/configuration", tags = { "v1-device-configuration" }, hidden = true)
@Component
public class DeviceConfigurationService {
	private static final int LOCK_TTL = 86400;
	public static final String MESSAGE_CHARSET = "UTF-8";
	public static final String DEFAULT_ALGORITM = "MD5";

	@Autowired
	@Qualifier("rateLimitControl")
	private RateLimitControl rateLimitControl;

	private static Logger logger = LoggerFactory.getLogger(DeviceConfigurationService.class);

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	
	private ObjectMapper mapper;

//	@Autowired
//	private RecoveryService recoveryService;

	@Autowired
	private DeviceService deviceService;
	@Autowired
	private NetworkService networkService;
	@Autowired
	private TracingService tracingService;
	@Autowired
	private CassandraService cassandraService;

//	@Autowired
//	private EventBus eventBus;

	/**
	 * Acquires a permit from rate limiter, blocking until one is available, or the
	 * thread is interrupted.
	 **/
	private void acquireRateLimitPermit(String clientId) {
		rateLimitControl.getRateLimiter(clientId, 1, 2, TimeUnit.SECONDS).get().acquire();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "configuration-write", notes = "write device configuration", response = DeviceAcknowledge.class)
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successful created configuration", response = DeviceAcknowledge.class),
			@ApiResponse(code = 401, message = "Unauthorized api-key", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "Device Not Found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "Invalid configuration", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response configurationWrite(
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial,
			@ApiParam(value = "device configuration", required = true, name = "config") @RequestBody @Valid DeviceConfiguration config) {
		RestErrorMessage error = new RestErrorMessage();
		Map<String, Device> map = new HashMap<String, Device>();
		String json = null;
		String lockId = serial;
		long elapsed = System.currentTimeMillis();

		String owner = null;
		try {

			// acquireRateLimitPermit(serial);

			if (config == null) {
				throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(),
						Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE, "missing api key " + serial);
			}

			if (config.getApi_key() == null) {
				throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(),
						Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE, "missing api key " + serial);
			}

			if (config.getParams() == null || config.getParams().isEmpty()) {
				throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
						Constants.Error.INVALID_DATA_ERROR_CODE, "empty param list");
			}

			try {
				DeviceConfiguration target = new DeviceConfiguration();
				BeanUtils.copyProperties(config, target, new String[] { "datetime", "revisionTime" });
				json = mapper.writeValueAsString(target);
			} catch (Exception ex) {
				json = EncryptUtils.digest(config.getDatetime() + "");
			}
			lockId = String.format("%s-%s", serial, EncryptUtils.digest(json));
			if (!cassandraService.getRollup().lockSink(lockId, LOCK_TTL)) {
				logger.debug(
						"{} device configuration has not been accepted because json md5 digest is the same as the last valid one {}",
						serial, lockId);
				return Response.status(Response.Status.CREATED)
						.entity(new DeviceAcknowledge(System.currentTimeMillis(), false)).build();
			}

			// Check for device existence
			Device device = deviceService.findBySerial(serial);
			if (device == null) {
				throw new RestServiceException(Response.Status.NOT_FOUND.getStatusCode(),
						Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "serial not found " + serial);
			}

			if (device.isDeActivated()) {
				// throw new
				// RestServiceException(Response.Status.NOT_FOUND.getStatusCode(),
				// Constants.Error.DEVICE_NOT_ACTIVE, "device not active " +
				// serial);
				error = new RestErrorMessage(Response.Status.CREATED.getStatusCode(), Constants.Error.DEVICE_NOT_ACTIVE,
						"device is de-activated " + serial);
				logger.debug(error.getMessage());
				return Response.status(Response.Status.CREATED).entity(error).build();
			}

			// Feature #1696
//			for (ParamConfiguration param : config.getParams()) {
//				if (param.getSerial() == null || param.getSerial().isEmpty()) {
//					param.setSerial(serial);
//				}
//				if (param.getSerial() != serial) {
//					Device slave = deviceService.findBySerial(param.getSerial());
//					if (slave == null) {
//						recoveryService.createRecoverySlave(param.getSerial(), serial);
//						error = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(), 
//								Constants.Error.DEVICE_NOT_AVAILABLE,
//								"slave recovery " + param.getSerial());
//						return Response.status(Response.Status.NOT_FOUND).entity(error).build();
//					}
//				}
//			}

			owner = device.getOwner();
			// Check authorization
			if (device.getWriteApikey() != null) {
				if (!device.getWriteApikey().equals(config.getApi_key())) {
					throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(),
							Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE, "wrong api key " + serial);
				}
			} else {
				throw new RestServiceException(Response.Status.NOT_FOUND.getStatusCode(),
						Constants.Error.DEVICE_WRITEAPIKEY_ERROR_CODE,
						"device with serial {} without WriteApikey " + serial);
			}

//			if (device.isTracing()) {
//				try {
//					json = mapper.writeValueAsString(config);
//				} catch (Exception ex) {
//				}
//			}

			if (device.hasRtc()) {
				Date timestamp = new Date(config.getDatetime() * 1000);
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.add(Calendar.DATE, -7);
				if (timestamp.before(calendar.getTime())) {
					String msg = String.format("device %s rtc enabled, configuration with invalid passed timestamp",
							serial);
					throw new RestServiceException(Response.Status.CREATED.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE, msg);
				}
			}

			// add device to map to manage channel configuration
			// eventually device is re-added during network configuration
			map.put(device.getSerial(), device);

			if (config.getNetwork() != null) {
				manageNetworkConfiguration(config.getNetwork(), device, map);
			}

			Date validity = manageMasterDevice(device, config);
			boolean fullManaged = (config.getVisualization_id() == null);
			if (config.isPartial()) {
				fullManaged = false;
			}

			Map<String, List<String>> configured = manageChannels(validity, serial, config.getParams(), map,
					fullManaged);
			// manageConfigurationRegistries(serial, config, map);
			manageRemoteControls(serial, config, map, configured);
			manageAlarms(map, configured, config.isPartial());
			manageDevices(device, map, configured, config.isPartial());

			int active = 0;
			int inactive = 0;

			for (Device entity : map.values()) {
				for (Channel channel : entity.getChannels()) {
					if (channel.getConfiguration().isActive()) {
						active++;
					} else {
						inactive++;
					}
				}
			}

			elapsed = System.currentTimeMillis() - elapsed;
			String msg = String.format("partial: %s, params: %d, active: %d, inactive: %d", config.isPartial(),
					config.getParams().size(), active, inactive);
			deviceService.trace(device, TracingAction.DEVICE_CONFIGURATION, msg, null, json);
			boolean flag = false;
			long ts = InterpolationUtils.currentTimeMillis(device.getTimeZone());
			logger.debug("{} device configuration has been accepted with following json md5 {}", serial, lockId);
			return Response.status(Response.Status.CREATED).entity(new DeviceAcknowledge(ts, flag)).build();

		} catch (Exception e) {
			cassandraService.getRollup().unlockSink(lockId);

			int status = 500;
			int code = 5000;
			if (e instanceof RestServiceException) {
				status = ((RestServiceException) e).getStatus();
				code = ((RestServiceException) e).getCode();
			}

			String endpoint = String.format("POST /v1/device/%s/configuration ", serial);
			String msg = String.format("%s %s", endpoint, e.getMessage());
			if (tracingService.traceRestError(e.toString(), owner, null, serial, msg, json)) {
				logger.error("{} [{}] {}", status, msg, Utils.logStackTrace(e));
			}

			error = new RestErrorMessage(status, code, e.getMessage());
		}
		Response response = Response.status(Status.fromStatusCode(error.getStatus())).entity(error).build();
		return response;
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "configuration-reset", notes = "reset device configuration", response = DeviceAcknowledge.class)
	@ApiResponses(value = {
			@ApiResponse(code = 202, message = "Successful reset configuration", response = DeviceAcknowledge.class),
			@ApiResponse(code = 401, message = "Unauthorized api-key", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "Device Not Found", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response configurationReset(
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial,
			@ApiParam(value = "device configuration", required = true, name = "config") @RequestBody @Valid DeviceConfiguration config) {

		if (config == null) {
			RestErrorMessage result = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
					Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE, "missing api key " + serial);
			return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();

		}

		Device device = deviceService.findBySerialCached(serial);
		if (device == null) {
			logger.error("device not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "serial not found " + serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result).build();
		}

		if (device.isDeActivated()) {
			logger.error("device not active {}", serial);
			RestErrorMessage error = new RestErrorMessage(Response.Status.CREATED.getStatusCode(),
					Constants.Error.DEVICE_NOT_ACTIVE, "device de-activated " + serial);
			return Response.status(Response.Status.ACCEPTED).entity(error).build();
		}

		Date timestamp = new Date(config.getDatetime() * 1000);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.add(Calendar.DATE, -7);
		if (timestamp.before(calendar.getTime()) && device.hasRtc()) {
			String msg = String.format("device %s, reset configuration with invalid passed timestamp", serial);
			RestErrorMessage error = new RestErrorMessage(Response.Status.ACCEPTED.getStatusCode(),
					Constants.Error.INVALID_DATA_ERROR_CODE, msg);
			logger.error(error.getMessage());
			return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(error).build();
		}

		if (device.getWriteApikey() != null) {
			if (!device.getWriteApikey().equals(config.getApi_key())) {
				logger.error("device with serial {} has wrong WriteApikey  {}", serial, device.getWriteApikey());
				RestErrorMessage result = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
						Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE, "wrong api key " + serial);
				return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();
			}
		}

		boolean flag = false;
		String lockId = String.format("%s-%s", "RESET", serial);
		if (!cassandraService.getRollup().lockSink(lockId, LOCK_TTL)) {
			logger.debug(
					"{} device configuration reset has not been accepted because previous lock ttl has not been expired",
					serial);
			return Response.status(Response.Status.ACCEPTED)
					.entity(new DeviceAcknowledge(System.currentTimeMillis(), flag)).build();
		}

		try {
			cassandraService.getRollup().resetLockSink(serial);
			subscriptionService.manageConfigurationReset(device);
		} catch (BackendServiceException e) {
			cassandraService.getRollup().unlockSink(lockId);
			tracingService.traceRestError(e.toString(), device.getOwner(), null, serial, e.getMessage(), null);
			logger.error(e.getMessage(), e);
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_CONF_ERROR_CODE, null);
			return Response.status(error.getStatus()).entity(error).build();
		}

		long ts = InterpolationUtils.currentTimeMillis(device.getTimeZone());
		return Response.status(Response.Status.ACCEPTED).entity(new DeviceAcknowledge(ts, flag)).build();
	}

	private void manageAlarms(Map<String, Device> map, Map<String, List<String>> configured, boolean partial) {
		for (Device device : map.values()) {
			boolean alarmed = false;
			List<String> keys = configured.get(device.getSerial());
			for (Channel channel : device.getChannels()) {
				if (keys.contains(channel.getUniqueKey())) {
					String metaData = channel.getMetaData();
					boolean isAlarm = (metaData != null)
							&& metaData.toLowerCase().contains(Constants.Provisioning.META_ALARM);
					// Bug #1466
					if (isAlarm) {
						alarmed = true;
						if (!channel.getAlarm().isValid()) {
							channel.setAlarm(deviceService.getChannelAlarmFromModbusRegister(channel));
						}
					} else {
						channel.setAlarm(new ChannelAlarm());
					}
				}
			}
			if (!partial) {
				device.setAlarmed(alarmed);
			}

		}
	}

	@SuppressWarnings("unused")
	private void manageConfigurationRegistries(String serialNumber, DeviceConfiguration config,
			Map<String, Device> map) {
		if (config.getRevision() <= 0) {
			return;
		}
		// add serial number
		for (ConfigAttribute source : config.getAttributes()) {
			if (source.getSerial() == null) {
				source.setSerial(serialNumber);
			}
		}

		for (Device device : map.values()) {
			List<ConfigAttribute> attributes = deviceAttributes(device.getSerial(), config.getAttributes());
			for (ConfigAttribute source : attributes) {
				ConfigurationRegistry target = new ConfigurationRegistry(device.getSerial(), true, source.getId());
				BeanUtils.copyProperties(source, target);
				target.setRevision(config.getRevision());
				target.setSerial(device.getSerial());
				cassandraService.getRegistry().updateConfigurationRegistry(target);
			}
			// remove client configuration
			// cassandraRegistry.deleteConfigurationRegistry(device.getSerial(),
			// false);
		}
	}

	private void manageRemoteControls(String serialNumber, DeviceConfiguration config, Map<String, Device> map,
			Map<String, List<String>> configured) {
//		if (config.getRevision() == 0) {
//			return;
//		}
		// add serial number
		for (ConfigAttribute source : config.getAttributes()) {
			if (source.getSerial() == null) {
				source.setSerial(serialNumber);
			}
		}

		for (Device device : map.values()) {
			Map<String, ConfigAttribute> remotes = deviceConfigAttributes(device.getSerial(), config.getAttributes());
			List<String> keys = configured.get(device.getSerial());
			for (Channel chnl : device.getChannels()) {
				// Bug #985
				if (keys.contains(chnl.getUniqueKey())) {
					ConfigAttribute source = remotes.get(chnl.getNumber());
					if (source != null && source.getTopic() != null && !source.getTopic().isEmpty()) {
						ChannelRemoteControl target = chnl.getRemote();
						target.setMax(source.getMax());
						target.setMin(source.getMin());
						target.setTopic(source.getTopic());
						target.setPermission(source.getPermission());
					} else {
						chnl.setRemote(new ChannelRemoteControl());
					}
				}
			}
		}
	}

	private Date manageMasterDevice(Device device, DeviceConfiguration config) {
		// Change fw release & others
		if (config.getFirmware_release() != null) {
			device.setFirmwareVersion(config.getFirmware_release());
		}
		if (config.getApp_release() != null) {
			device.setProgramVersion(config.getApp_release());
		}
		if (config.getSample_period() > 0) {
			device.setSamplePeriod(config.getSample_period());
		}
		Date validity = new Date();
		if (config.getDatetime() > 0) {
			Date configDate = new Date(config.getDatetime() * 1000);
			if (configDate.before(validity)) {
				validity = configDate;
			}
		}
		device.setConfigurationDate(validity);
		return validity;
	}

	private void manageDevices(Device master, Map<String, Device> map, Map<String, List<String>> configured,
			boolean partial) throws RestServiceException {
		if (master.getStatus().equals(DeviceStatus.PRODUCED)) {
			master.setStatus(DeviceStatus.VERIFIED);
		}
		boolean masterVerified = master.getStatus().equals(DeviceStatus.VERIFIED);
		for (Device entity : map.values()) {
			if (!entity.getOwner().equals(master.getOwner())) {
				String msg = String.format("Device %s does not belong to %s", entity.getSerial(), master.getOwner());
				throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
						Constants.Error.INVALID_CONF_ERROR_CODE, msg);
			}
			try {
				if (entity.getStatus().equals(DeviceStatus.PRODUCED)) {
					entity.setStatus(DeviceStatus.VERIFIED);
				}
				// activate device
				if (entity.isAvailableForActivation() && !masterVerified) {
					entity.setStatus(DeviceStatus.ACTIVATED);
				}

				if (!entity.getSerial().equals(master.getSerial())) {
					entity.setMaster(master);
					entity.setOwner(master.getOwner());
					entity.setConfigurationDate(master.getConfigurationDate());
				}
				entity.setModifier(Constants.SYSTEM);

				if (entity.isNew()) {
					deviceService.create(entity);
				} else {
					try {
						if (!partial) {
							String checkSum = entity.calculateCheckSum();
							if (entity.getCheckSum() != null && checkSum.equals(entity.getCheckSum())) {
								logger.debug("Checksum not changed, not updating {}", entity.getSerial());
							} else {
								entity.setCheckSum(checkSum);
								deviceService.update(entity);
							}
						} else {
							entity.setCheckSum(null);
							deviceService.update(entity);
						}
					} catch (Throwable e) {
						logger.error("handling PersistenceException updating {}", entity.getSerial());
						manageUpdateConflict(entity, configured.get(entity.getSerial()), e);
					}
				}

			} catch (Throwable e) {
				StringWriter errorStackTrace = new StringWriter();
				e.printStackTrace(new PrintWriter(errorStackTrace));
				tracingService.trace(TracingAction.ERROR_REST, null, null, null, null, errorStackTrace.toString());
				String message = String.format("error on create/update %s : %s ", entity.getSerial(), e.getMessage());
				throw new RestServiceException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
						Constants.Error.GENERIC_APP_ERROR_CODE, message);
			}
			// logger.debug("managed device {}", entity);

		}

		subscriptionService.postProcessConfig(map.values(), partial);

	}

	private void manageUpdateConflict(Device entity, List<String> parameterKeys, Throwable originalException)
			throws BackendServiceException {
		Device conflicted = deviceService.findOne(entity.getId());
		List<String> conflictKeys = new ArrayList<>();
		for (Channel chnl : conflicted.getChannels()) {
			conflictKeys.add(chnl.getUniqueKey());
			// logger.debug("conflicted {} {}", chnl.getUniqueKey(),
			// chnl.getConsistencyVersion());
		}

		Map<String, Channel> configured = new HashMap<>();
		for (Channel chnl : entity.getChannels()) {
			if (parameterKeys.contains(chnl.getUniqueKey())) {
				configured.put(chnl.getUniqueKey(), chnl);
				// logger.debug("configured {} {}", chnl.getUniqueKey(),
				// chnl.getConsistencyVersion());
			}
		}

		if (conflicted.getConsistencyVersion() == entity.getConsistencyVersion()) {
			StringWriter errorStackTrace = new StringWriter();
			originalException.printStackTrace(new PrintWriter(errorStackTrace));
			String message = String.format("an update error occurred but same consistency version has been found : %s ",
					entity.getSerial(), errorStackTrace.toString());
			tracingService.trace(TracingAction.ERROR_REST, null, entity.getOwner(), entity.getNetwork().getName(),
					entity.getSerial(), message);
		}

		conflicted.setStatus(entity.getStatus());
		for (Channel conflict : conflicted.getChannels()) {
			if (configured.containsKey(conflict.getUniqueKey())) {
				Channel source = configured.get(conflict.getUniqueKey());
				BeanUtils.copyProperties(source, conflict, new String[] { "consistencyVersion" });
			}
		}
		for (String parameter : parameterKeys) {
			if (!conflictKeys.contains(parameter)) {
				Channel chnl = configured.get(parameter);
				conflicted.addChannel(chnl);
			}
		}
		deviceService.update(conflicted);
		logger.error("manageUpdateConflict done {}: {} {}", entity.toString(), entity.getConsistencyVersion(),
				conflicted.getConsistencyVersion());

	}

	private List<ConfigAttribute> deviceAttributes(String serial, List<ConfigAttribute> config) {
		List<ConfigAttribute> attributes = new ArrayList<>();
		for (ConfigAttribute source : config) {
			if (source.getSerial().equals(serial)) {
				attributes.add(source);
			}
		}
		return attributes;
	}

	private List<String> deviceParams(String serial, List<ParamConfiguration> params) {
		List<String> currentParams = new ArrayList<String>();
		for (ParamConfiguration param : params) {
			if (param.getSerial() == null) {
				param.setSerial(serial);
			}
			if (param.getSerial().equals(serial)) {
				currentParams.add(param.getId());
			}
		}
		return currentParams;
	}

	private Map<String, ConfigAttribute> deviceConfigAttributes(String serial, List<ConfigAttribute> items) {
		Map<String, ConfigAttribute> map = new HashMap<String, ConfigAttribute>();
		for (ConfigAttribute item : items) {
			if (serial.equals(item.getSerial()) && item.getTopic() != null && !item.getTopic().isEmpty()) {
				map.put(item.getId(), item);
			}
		}
		return map;
	}

	private Map<String, List<String>> manageChannels(Date validity, String serial, List<ParamConfiguration> params,
			Map<String, Device> map, boolean fullManaged) throws RestServiceException {

		Map<String, List<String>> configured = new HashMap<String, List<String>>();
		for (Device entity : map.values()) {
			configured.put(entity.getSerial(), new ArrayList<String>());
		}

		int position = 0;
		Device device = null;
		for (ParamConfiguration param : params) {
			position++;
			if (param.getSerial() == null) {
				param.setSerial(serial);
			}

			if (!map.containsKey(param.getSerial())) {
				device = deviceService.findBySerialCached(param.getSerial());
				if (device != null) {
					map.put(param.getSerial(), device);
					configured.put(device.getSerial(), new ArrayList<String>());
				}

			} else {
				device = map.get(param.getSerial());
			}

			if (device != null) {
				if (param.getId() == null || param.getId().isEmpty()) {
					String msg = String.format("device %s empty param id", param.getSerial());
					throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
							Constants.Error.INVALID_CONF_ERROR_CODE, msg);
				}
				if (param.getLabel() == null || param.getLabel().isEmpty()) {
					String msg = String.format("device %s param %s missing param label", param.getSerial(),
							param.getId());
					throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
							Constants.Error.INVALID_CONF_ERROR_CODE, msg);
				}

				if (param.getSublabel() == null) {
					param.setSublabel("");
				}

				if (param.getQual() == 0) {
					param.setQual(MeasureQualifier.ONE.getValue());
				}

				String uniqueKey = paramKey(param);
				Channel channel = new Channel();
				channel.setDevice(device);
				channel.setNumber(param.getId());

				if (param.getOid() == null) {
					channel.setMetaData(String.format("%04d", position));
				} else {
					channel.setOid(param.getOid());
					String metadata = subscriptionService.getMetaData(param.getOid());
					if (metadata != null) {
						channel.setMetaData(metadata);
					}
//					else {
//						channel.setOid(null);
//						channel.setMetaData(param.getOid());
//					}
				}

				channel.getConfiguration().setLabel(param.getLabel());
				channel.getConfiguration().setSubLabel(param.getSublabel());
				channel.getConfiguration().setHideNumber(param.getHide_id() > 0);
				channel.getConfiguration().setQualifier(param.getQual());
				channel.getConfiguration().setSensor(param.getSens());
				channel.getConfiguration().setActive(true);
				channel.getConfiguration().setActivationDate(validity);
				channel.setOwner(device.getOwner());
				channel.setUniqueKey(uniqueKey);

				List<MeasureUnit> measures = manageMeasureUnits(param);
				if (measures.isEmpty()) {
					String msg = String.format("device %s param %s missing measure list", param.getSerial(),
							param.getId());
					throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
							Constants.Error.INVALID_CONF_ERROR_CODE, msg);
				} else {
					for (MeasureUnit measureUnit : measures) {
						MeasureUnitType type = deviceService.lookUpBacnetCode(measureUnit.getType());
						if (type == null) {
							String msg = String.format("device %s param %s missing measure list", param.getSerial(),
									param.getId());
							throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
									Constants.Error.INVALID_CONF_ERROR_CODE, msg);
						}
					}
				}

				channel.setMeasures(measures);
				if (deviceService.manageChannelByNumber(device, channel)) {
					configured.get(device.getSerial()).add(channel.getUniqueKey());
				}

			} else {
				String msg = String.format("device %s not found", param.getSerial());
				throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
						Constants.Error.INVALID_CONF_ERROR_CODE, msg);
			}
		}

		if (fullManaged) { // partial = false
			// de-activate un-configured params
			for (String sn : configured.keySet()) {
				Device entity = map.get(sn);
				List<String> keys = configured.get(entity.getSerial());
				for (Channel channel : entity.getChannels()) {
					if (!keys.contains(channel.getUniqueKey())) {
						channel.getConfiguration().setActive(false);
						channel.getConfiguration().setDeactivationDate(new Date());
						configured.get(entity.getSerial()).add(channel.getUniqueKey());
					}
				}
			}
		}
		// with a provisioned configuration, activation/de-activation is done on server

		return configured;
	}

	/**
	 * create list of measure units to be assigned to device
	 * 
	 * @param paramConf
	 * @return
	 */
	private List<MeasureUnit> manageMeasureUnits(ParamConfiguration paramConf) {
		List<MeasureUnit> values = new ArrayList<MeasureUnit>();
		if (paramConf.getUnit() == null) {
			return values;
		}
		if (paramConf.getOffset() == null || paramConf.getOffset().length != paramConf.getUnit().length)
			return values;
		if (paramConf.getScale() == null || paramConf.getScale().length != paramConf.getUnit().length)
			return values;
		if (paramConf.getFormat() == null || paramConf.getFormat().length != paramConf.getUnit().length)
			return values;

		for (int i = 0; i < paramConf.getUnit().length; i++) {
			MeasureUnit value = new MeasureUnit();
			value.setType(paramConf.getUnit()[i]);
			value.setOffset(paramConf.getOffset()[i]);
			value.setScale(paramConf.getScale()[i]);
			value.setFormat(normalizedFormat(paramConf.getFormat()[i]));
			values.add(value);
		}
		return values;
	}

	private String normalizedFormat(String value) {
		String format = "";
		try {
			String[] ff = value.split("\\.");
			if (ff != null && ff.length == 2) {
				String before = StringUtils.repeat("#", "", Integer.parseInt(ff[0]));
				// Feature #293 Zero decimal padding
				String after = StringUtils.repeat("0", "", Integer.parseInt(ff[1]));
				format = before + "." + after;
			}

		} catch (Exception e) {
			logger.error("unhandled format " + value, e);
		}
		return format;
	}

	/**
	 * 
	 * The algorithm for device management should be: if is present nothing todo if
	 * is present into config but not in network list check if should be activated
	 * and then inserted if is present in network list but not into config list
	 * should be removed
	 * 
	 * @param networkConf
	 * @param deviceStatus
	 * @param deviceOwner
	 * @param map
	 * @throws RestServiceException
	 */
	private void manageNetworkConfiguration(NetworkConfiguration networkConf, Device networkDevice,
			Map<String, Device> map) throws RestServiceException {

		if (networkConf.getName() == null || networkConf.getName().isEmpty()) {
			String msg = "invalid network name, cannot configure network ";
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_DATA_ERROR_CODE, msg);
		}

		if (networkConf.getDevices() == null || networkConf.getDevices().length == 0) {
			String msg = "empty device list, cannot configure network ";
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_DATA_ERROR_CODE, msg);
		}

		boolean verified = networkDevice.getStatus().equals(DeviceStatus.PRODUCED)
				|| networkDevice.getStatus().equals(DeviceStatus.VERIFIED);

		if (verified) {
			if (networkDevice.getStatus().equals(DeviceStatus.PRODUCED)) {
				networkDevice.setStatus(DeviceStatus.VERIFIED);
			}
			for (String devSerial : networkConf.getDevices()) {
				Device device = deviceService.findBySerial(devSerial);
				if (device != null) {
					if (device.getStatus().equals(DeviceStatus.PRODUCED)) {
						device.setStatus(DeviceStatus.VERIFIED);
					}
					map.put(devSerial, device);
				}
			}
			return;
		}

		if (!networkDevice.isActivated()) {
			String msg = networkDevice.getSerial() + " not active, cannot configure network " + networkConf.getName();
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_DATA_ERROR_CODE, msg);
		}

		String networkOwner = networkDevice.getOwner();
		Network network = null;
		try {
			network = networkService.findByName(networkConf.getName(), networkOwner);
		} catch (BackendServiceException e) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_DATA_ERROR_CODE, e.getMessage());
		}

		// Create Device Network
		if (network == null) {
			network = new Network();
			network.setOwner(networkOwner);
			network.setName(networkConf.getName());
			network.setExternalId(networkDevice.getSerial());
			networkService.create(network);
			logger.debug("created network {} and default group", network);
		}

		NetworkGroup defaultGroup = network.getDefaultGroup();
		if (networkDevice.getNetwork() == null) {
			networkDevice.addGroup(defaultGroup);
			networkDevice.setOwner(networkOwner);
		}

		// populate map with devices to be configured
		for (String serial : networkConf.getDevices()) {
			Device device = deviceService.findBySerial(serial);
			if (device != null) {
				if (!device.getOwner().equals(networkOwner)) {
					String msg = String.format("Device %s does not belong to %s", device.getSerial(), networkOwner);
					throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
							Constants.Error.INVALID_DATA_ERROR_CODE, msg);
				}
				// device does not belong to any network / group
				if (device.getNetwork() == null) {
					device.addGroup(defaultGroup);
					device.setOwner(networkOwner);
				}
				map.put(serial, device);
			} else {
				// Feature #364 new rules of insertion/activation for devices
				device = new Device();
				device.setOwner(networkOwner);
				device.addGroup(defaultGroup);
				device.setSerial(serial);
				device.setLabel(serial);
				device.setWriteApikey(EncryptUtils.createWriteApiKey(serial));
				device.setStatus(DeviceStatus.ACTIVATED);
				device.setMaster(networkDevice);
				map.put(serial, device);
			}
		}
	}

	/**
	 * Modifiche a questo metodo incidono sulla retrocompatibiltà delle chiavi Il
	 * parametro è identificato dai seguenti campi id, qual, unit, scale, offset
	 * 
	 * @param param
	 */
	private String paramKey(ParamConfiguration param) {
		StringBuilder sb = new StringBuilder();
		sb.append(param.getId());
		sb.append(param.getQual());
		for (int i = 0; i < param.getUnit().length; i++) {
			sb.append(param.getUnit()[i]);
		}
		for (int i = 0; i < param.getScale().length; i++) {
			sb.append(param.getScale()[i]);
		}
		for (int i = 0; i < param.getOffset().length; i++) {
			sb.append(param.getOffset()[i]);
		}

		return digest(sb.toString());
	}

	private String digest(String key) {
		try {
			byte[] message = key.getBytes(MESSAGE_CHARSET);
			MessageDigest md = MessageDigest.getInstance(DEFAULT_ALGORITM);
			md.update(message);
			byte[] digest = md.digest(message);
			return BaseEncoding.base64().encode(digest);
		} catch (Exception e) {
			throw new EncryptionInitializationException(e);
		}
	}

	private void checkActivatedParams(String serial, DeviceConfiguration config) throws RestServiceException {
		Device entity = deviceService.findBySerial(serial);
		Set<String> activated = new HashSet<String>();
		for (Channel channel : entity.getChannels()) {
			if (channel.getConfiguration().isActive()) {
				activated.add(channel.getNumber());
				// for (MeasureUnit unit : channel.getMeasures()) {
				// logger.error(unit.toString());
				// }
			}
		}
		List<String> missing = new ArrayList<String>();
		for (ParamConfiguration param : config.getParams()) {
			if (param.getSerial().equals(serial) && !activated.contains(param.getId())) {
				missing.add(param.getId());
			}
		}
		if (!missing.isEmpty()) {
			String message = String.format("params not activated: %s", StringUtils.join(missing, ","));
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_CONF_ERROR_CODE, message);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "configuration-read", notes = "read device configuration", response = DeviceConfiguration.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful retrieve configuration", response = DeviceConfiguration.class),
			// @ApiResponse(code = 401, message = "Unauthorized api-key",
			// response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "Device Not Found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "Invalid Request", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response configurationRead(
			@ApiParam(name = "serial", value = "device serial", required = true) @PathParam("serial") String serial,
			@ApiParam(name = "api-key", value = "device read api key", required = true) @HeaderParam("api-key") String apiKey

	) {
		Device device = deviceService.findBySerialCached(serial);
		if (device == null) {
			logger.error("device not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, " serial not found " + serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result).build();
		}

		String deviceApiKey = getApiKey(device);
		// Check authorization
		if (deviceApiKey != null) {
			if ((apiKey != null) && !deviceApiKey.equals(apiKey)) {
				logger.error("configuration request for device with serial {} has wrong read api key", serial);
				RestErrorMessage result = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
						Constants.Error.DEVICE_READAPIKEY_ERROR_CODE, serial);
				return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();
			} else if (apiKey == null) {
				logger.error("configuration request for device with serial {} missing read api key", serial);
				RestErrorMessage result = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
						Constants.Error.DEVICE_READAPIKEY_ERROR_CODE, serial);
				return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();

			}
		}

		// if (device.getMaster() != null) {
		// String message = String.format("virtual device found with serial %s",
		// serial);
		// logger.error(message);
		// RestErrorMessage result = new
		// RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
		// Constants.Error.DEVICE_CONF_NOT_FOUND_ERROR_CODE, message);
		// return
		// Response.status(Response.Status.NOT_FOUND).entity(result).build();
		// }
		if (device.getConfigurationDate() == null) {
			String message = String.format("unconfigured device with serial %s", serial);
			logger.error(message);
			RestErrorMessage result = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.DEVICE_CONF_NOT_FOUND_ERROR_CODE, message);
			return Response.status(Response.Status.NOT_FOUND).entity(result).build();
		}
		DeviceConfiguration response = createDeviceConfiguration(device);
		return Response.status(Response.Status.OK).entity(response).build();
	}

	private DeviceConfiguration createDeviceConfiguration(Device device) {
		DeviceConfiguration config = new DeviceConfiguration();
		config.setFirmware_release(device.getFirmwareVersion());
		config.setApp_release(device.getProgramVersion());
		config.setSample_period(device.getSamplePeriod());
		config.setDatetime(device.getConfigurationDate().getTime() / 1000);

		for (Channel channel : device.getChannels()) {
			if (channel.getConfiguration().isActive()) {
				config.getParams().add(paramConfiguration(channel));
			}
		}

		List<ConfigurationRegistry> regs = cassandraService.getRegistry().getConfigurations(device.getSerial(), true);
		for (ConfigurationRegistry source : regs) {
			ConfigAttribute target = new ConfigAttribute();
			BeanUtils.copyProperties(source, target);
			config.getAttributes().add(target);
		}

		List<Device> slaves = deviceService.findSlaves(device);
		for (Device slave : slaves) {
			for (Channel channel : slave.getChannels()) {
				if (channel.getConfiguration().isActive()) {
					config.getParams().add(paramConfiguration(channel));
				}
			}
			regs = cassandraService.getRegistry().getConfigurations(slave.getSerial(), true);
			for (ConfigurationRegistry source : regs) {
				ConfigAttribute target = new ConfigAttribute();
				BeanUtils.copyProperties(source, target);
				config.getAttributes().add(target);
			}

		}

		return config;
	}

	private ParamConfiguration paramConfiguration(Channel channel) {
		ParamConfiguration param = new ParamConfiguration();
		param.setId(channel.getNumber());
		param.setLabel(channel.getConfiguration().getLabel());
		param.setSublabel(channel.getConfiguration().getSubLabel());
		param.setSens(channel.getConfiguration().getSensor());
		MeasureUnit unit = channel.getDefaultMeasure();
		String[] ff = unit.getFormat().split("\\.");
		int count0 = 0;
		int count1 = 0;
		if (ff.length == 2) {
			count0 = StringUtils.countMatches(ff[0], "#");
			if (ff[1] != null)
				count1 = StringUtils.countMatches(ff[1], "#");
		}
		if (ff.length == 1) {
			count0 = StringUtils.countMatches(ff[0], "#");
		}
		param.setFormat(new String[] { count0 + "." + count1 });
		param.setOffset(new float[] { unit.getOffset() });
		param.setScale(new float[] { unit.getScale() });
		param.setUnit(new int[] { unit.getType() });
		param.setQual(channel.getConfiguration().getQualifier());
		param.setSerial(channel.getDevice().getSerial());
		param.setHide_id(1);
		return param;
	}

	private String getApiKey(Device device) {
		Device master = device.getMaster();
		if (master == null) {
			return device.getActivationKey();
		}
		return master.getActivationKey();
	}

}
