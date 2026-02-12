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
import it.thisone.iotter.rest.model.client.IotUser;
import it.thisone.iotter.rest.model.client.IotUserSet;

@Path("/v1/client/association/device/{serial}")
@Api(value="/v1/client/association/device/{serial}", tags={"v1-client-association-device-user"}, hidden=ClientEndpoint.HIDDEN)
@Component
public class ClientAssociationDeviceEndpoint extends ClientEndpoint {

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="delete-user-association",value = "client removes a user from device", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful association removal"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response removeDeviceAssociation(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(name = "serial", value = "device id", required = true)
			@PathParam("serial") String serial, //
			@ApiParam(value = "device identification", required = true, name = "entity") 
			@RequestBody @Valid	
			IotUser entity) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}

	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="create-user-association",value = "client associates a user to a device", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successfull network association"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 406, message = "Invalid User", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response createDeviceAssociation(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(name = "serial", value = "device id", required = true)
			@PathParam("serial") String serial, //
			@ApiParam(value = "device identification", required = true, name = "entity") 
			@RequestBody @Valid	
			IotUser entity) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="list-user-associations",value = "client lists users associated to device", notes = NOT_YET_IMPLEMENTED, response= IotUserSet.class)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful listing", response= IotUserSet.class),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response deviceAssociations(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey,
			@ApiParam(name = "serial", value = "device id", required = true)
			@PathParam("serial") String serial //
			) {
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}

	
}
