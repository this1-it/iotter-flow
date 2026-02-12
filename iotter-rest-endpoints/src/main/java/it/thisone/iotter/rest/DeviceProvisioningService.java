package it.thisone.iotter.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.SubscriptionService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.ModbusProfile;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.provisioning.ProvisionedEvent;
import it.thisone.iotter.rest.model.DeviceProvisioning;
import it.thisone.iotter.rest.model.DeviceProvisioningWrite;
import it.thisone.iotter.rest.model.ModbusProvisioning;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.util.EncryptUtils;

@Api(value="/v1/device/{serial}/provisioning", tags={"v1-device-provisioning"}, hidden=true)
@Path("/v1/device/{serial}/provisioning")
@Component
public class DeviceProvisioningService {
	private static Logger logger = LoggerFactory
			.getLogger(DeviceProvisioningService.class);

	@Autowired
	
	private ObjectMapper mapper;
	
	@Autowired
	private DeviceService deviceService;
	
	@Autowired
	private SubscriptionService subscriptionService;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "provisioning-read", notes = "read device provisioning", response = DeviceProvisioning.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfull read provisioning", response = DeviceProvisioning.class),
			//@ApiResponse(code = 401, message = "Unauthorized api-key", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "Device Not Found", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response provisioning(
			@ApiParam(name = "serial", value = "device serial", required = true)
			@PathParam("serial") String serial ,
			@ApiParam(name = "lang", value = "language for registers labels")
			@QueryParam(value = "lang") String language
			) 
	{
		
		Device master = deviceService.findBySerial(serial);
		if (master == null) {
			logger.error("master not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "serial not found "
							+ serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result)
					.build();
		}
		
		if (master.isDeActivated()) {
			DeviceProvisioning response = new DeviceProvisioning();
			response.setProfiles(new ArrayList<ModbusProvisioning>());
			response.setChecksum(EncryptUtils.digest(System.currentTimeMillis() + ""));
			return Response.status(Response.Status.OK).entity(response).build();
		}
		
		
		Locale locale = Locale.ENGLISH;
		if (language != null) {
			if (Arrays.asList(Locale.getISOLanguages()).contains(language)) {
				locale = new Locale(language);
			}
		}
		
		
		List<ModbusProvisioning> profiles = new ArrayList<ModbusProvisioning>();
		List<Device> slaves = deviceService.findSlaves(master);
		for (Device slave : slaves) {
			for (ModbusProfile profile : slave.getProfiles()) {
				ModbusProvisioning bean = subscriptionService.convertModbusProfile(slave.getSerial(), profile, locale);
				profiles.add(bean);				
			}
		}
		
//		// Feature #1696
//		if (profiles.isEmpty()) {
//			String message = String.format("%s profiles are not configured", serial);
//			logger.error(message);
//			RestErrorMessage result = new RestErrorMessage(
//					Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
//					Constants.Error.DEVICE_PROVISIONING_ERROR_CODE, message);
//			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result)
//					.build();
//		}


		String checksum = deviceService.provisioningChecksum(master);
		DeviceProvisioning response = new DeviceProvisioning();
		response.setProfiles(profiles);
		response.setChecksum(checksum);
		String json = null;
		if (master.isTracing()) {
			try {
				json = mapper.writeValueAsString(response);
			} catch (Exception ex) {
			}			
		}
		
		deviceService.trace(master, TracingAction.DEVICE_PROVISIONING, checksum, null, json);
		return Response.status(Response.Status.OK).entity(response).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response provisioningWrite(@PathParam("serial") String serial, DeviceProvisioningWrite provisioning) {
		Device master = deviceService.findBySerial(serial);
		if (master == null) {
			logger.error("master not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "serial not found " + serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result).build();
		}
		if (!master.getWriteApikey().equals(provisioning.getApiKey())) {
			logger.error("master with serial {} has wrong WriteApikey  {}", serial, master.getWriteApikey());
			RestErrorMessage result = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
					Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE, "wrong api key " + serial);
			return Response.status(Response.Status.UNAUTHORIZED).entity(result).build();
		}
		
		try {
			subscriptionService.writeProvisioning(master, provisioning.getProfiles());
			String checksum = deviceService.provisioningChecksum(master);
			ProvisionedEvent provisioned = new ProvisionedEvent(master, checksum);
			subscriptionService.provisioned(provisioned);
		} catch (BackendServiceException e) {
			logger.error("provisioningWrite " + serial, e);
			RestErrorMessage result = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					e.getCode(), e.getMessage());
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(result).build();
		}

		return Response.status(Response.Status.CREATED).build();
	}

	
	

	
}
