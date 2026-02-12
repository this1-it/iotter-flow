package it.thisone.iotter.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.backend.BillingService;
import it.thisone.iotter.rest.model.billings.BillingDevice;
import it.thisone.iotter.rest.model.billings.Header;
import it.thisone.iotter.rest.model.billings.ResponseBilling;

@Path("/v1/renew_platform")
@Api(hidden=true, value = "/v1/renew_platform", tags = { "v1-renew_platform" })
@Component

public class BillingEndpoint {

	private static final String DONE = "D";
	
	@Autowired
	private BillingService billingService;

	
	@GET
	@Path("/billings")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "list-billings", value = "list billings", response = ResponseBilling.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful list operation", response = ResponseBilling.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ResponseBilling.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response billings(
			@ApiParam(name = "owner", value = "device owner", required = false) @QueryParam("owner") String owner, //
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey //
	) {
		ResponseBilling response = new ResponseBilling();
		Header header = new Header();
		if (!billingService.validateApiKey(apiKey)) {
			header.setOutput(0);
			header.setType("E");
			header.setMessage("wrong api key");
			response.setHeader(header);
			return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();			
		}
		List<BillingDevice> data = billingService.getActivatedDevices(owner);
		Set<String> owners = data.stream().map(x -> x.getOwner()).collect(Collectors.toSet());
		billingService.scheduleDeactivateJob(owners);
		header.setOutput(1);
		header.setType(DONE);
		if (owner != null) {
			header.setMessage(owner);
		}
		response.setData(data);
		response.setHeader(header);
		return Response.status(Response.Status.OK).entity(response).build();
	}
	
	
	
	
	
	
	
	
	
	

}
