package it.thisone.iotter.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.thisone.iotter.cassandra.CassandraRegistry;
import it.thisone.iotter.cassandra.model.ConfigurationRevision;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.service.DeviceService;
import it.thisone.iotter.rest.model.DeviceConfigurationRev;
import it.thisone.iotter.rest.model.RestErrorMessage;

@Path("/v1/device/{serial}/configuration_rev")
@Component
public class DeviceConfigurationRevisionService {

	private static Logger logger = LoggerFactory
			.getLogger(DeviceConfigurationRevisionService.class);

	@Autowired
	private DeviceService deviceService;
	@Autowired
	private CassandraRegistry cassandraRegistry;
	

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response configuration(@PathParam("serial") String serial, @HeaderParam("api-key") String apiKey) {
		Device device = deviceService.findBySerial(serial);
		if (device == null) {
			logger.error("device not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_NOT_FOUND_ERROR_CODE, "serial not found "
							+ serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result)
					.build();
		}
		if (device.getWriteApikey() != null) {
			if (!device.getWriteApikey().equals(apiKey)) {
				logger.error("device with serial {} has wrong WriteApikey  {}",
						serial, device.getWriteApikey());
				RestErrorMessage result = new RestErrorMessage(
						Response.Status.UNAUTHORIZED.getStatusCode(),
						Constants.Error.DEVICE_UNAUTHORIZED_ERROR_CODE,
						"wrong api key " + serial);
				return Response.status(Response.Status.UNAUTHORIZED)
						.entity(result).build();
			}
		}
		
		
		DeviceConfigurationRev response = getConfigurationRev(serial);
		
		if (response == null) {
			logger.error("device configuration not found with serial {}", serial);
			RestErrorMessage result = new RestErrorMessage(
					Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.DEVICE_CONF_NOT_FOUND_ERROR_CODE, " configuration not found "
							+ serial);
			return Response.status(Response.Status.NOT_FOUND).entity(result)
					.build();
		}		
		
		return Response.status(Response.Status.OK)
				.entity(response).build();
	}

	
	private DeviceConfigurationRev getConfigurationRev(String serial) {
		ConfigurationRevision cfg = cassandraRegistry.getConfigurationRevision(serial, true);
		if (cfg == null ) {
			return null;
		}
		DeviceConfigurationRev rev = new DeviceConfigurationRev();
		rev.setRevision(cfg.getRevision());
		return rev;
	}
	

}
