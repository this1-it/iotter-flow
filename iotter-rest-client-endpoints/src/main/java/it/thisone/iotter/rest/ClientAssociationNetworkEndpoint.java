package it.thisone.iotter.rest;


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

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.client.IotDeviceId;
import it.thisone.iotter.rest.model.client.IotDeviceSet;

@Path("/v1/client/association/network/{network}")
@Api(value="/v1/client/association/network/{network}", tags={"v1-client-association-network-device"}, hidden=ClientEndpoint.HIDDEN)
@Component
public class ClientAssociationNetworkEndpoint extends ClientEndpoint {

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="delete-device-association",value = "client removes a device from network", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful association removal"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response removeNetworkAssociation(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(name = "network", value = "network id", required = true)
			@PathParam("network") String networkId, //
			@ApiParam(value = "device identification", required = true, name = "entity") 
			@RequestBody @Valid	
			IotDeviceId entity) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}
	


	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="create-device-association",value = "client associates a device to a network", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successfull network association"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 406, message = "Invalid User", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response createNetworkAssociation(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(name = "network", value = "network id", required = true)
			@PathParam("network") String networkId, //
			@ApiParam(value = "device identification", required = true, name = "entity") 
			@RequestBody @Valid	
			IotDeviceId entity) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="list-device-associations",value = "client lists devices belonging to network", notes = NOT_YET_IMPLEMENTED, response= IotDeviceSet.class)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful listing", response= IotDeviceSet.class),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response networkAssociations(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey,
			@ApiParam(name = "network", value = "network id", required = true)
			@PathParam("network") String networkId
			) {
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}

	
}
