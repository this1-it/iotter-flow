package it.thisone.iotter.rest;


import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.client.IotNetwork;
import it.thisone.iotter.rest.model.client.IotNetworkSet;

@Path("/v1/client/assets/network")
@Api(value="/v1/client/assets/network", tags={"v1-client-assets-network"}, hidden=ClientEndpoint.HIDDEN)
@Component
public class ClientAssetsNetworkEndpoint extends ClientEndpoint {

	private static Logger logger = LoggerFactory.getLogger(ClientAssetsNetworkEndpoint.class);

	@DELETE
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="delete-network",value = "client deletes network", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful network removal"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response deleteNetwork(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(value = "network identification", required = true, name = "entity") 
			@RequestBody @Valid	
			IotNetwork entity) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}
	

	@PUT
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="update-network",value = "client updates network details", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "successfull details updated"),
		    @ApiResponse(code = 401, message = "unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 404, message = "not found", response= RestErrorMessage.class),
		    @ApiResponse(code = 406, message = "invalid configuration", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "internal server error")
		    })
	public Response networkUpdate(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(value = "network details", required = true, name = "entity") 
			@RequestBody @Valid	
			IotNetwork request) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}

	
	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="create-network",value = "client updates network details", notes = NOT_YET_IMPLEMENTED)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successfull network login created"),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 406, message = "Invalid User", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response networkCreate(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey, 
			@ApiParam(value = "new network", required = true, name = "entity") 
			@RequestBody @Valid	
			IotNetwork request) {
		
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}
	
	
	
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname="list-network",value = "client lists its managed networks", notes = NOT_YET_IMPLEMENTED, response= IotNetworkSet.class)
	@ApiResponses(value= {
		    @ApiResponse(code = 200, message = "Successful list retrieving", response= IotNetworkSet.class),
		    @ApiResponse(code = 401, message = "Unauthorized", response= RestErrorMessage.class),
		    @ApiResponse(code = 500, message = "Internal server error")
		    })
	public Response networks(
			@ApiParam(name = "api-key", value = "authorization token", required = true)
			@HeaderParam("api-key") String apiKey) {
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}

	
}
