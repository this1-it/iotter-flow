package it.thisone.iotter.rest;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.config.SATClientConfig;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.IotNetwork;
import it.thisone.iotter.rest.model.client.IotUser;
import it.thisone.iotter.rest.model.client.SATUserCreation;


@Path("/v1/client/sat")
@Api(value = "/v1/client/sat", tags = { "v1-client-sat" }, hidden=true)
@Component
public class SATClientEndpoint extends ClientEndpoint {

	@Path("/create_user")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "create_user", value = "User Creation", notes = "User Creation")
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "Successful User creation", response = IotUser.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "Invalid registration", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response create( //
			@ApiParam(name = "api-key", value = "authorization token", required = true) //
			@HeaderParam("api-key") String apiKey, //
			@ApiParam(value = "creation data", required = true, name = "creation") //
			@RequestBody @Valid SATUserCreation creation //
	) {
		
		try {
			SessionAuthentication session = validateSessionToken(apiKey);
			if (session.getRole().equals(Constants.ROLE_USER)) {
				throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, "");
			}
			try {
				clientUserService.createUser(creation.getLogin(), creation.getUserDetails(), new IotNetwork(SATClientConfig.NETWORK_NAME, SATClientConfig.ADMIN_NAME));
			} catch (BackendServiceException e) {
				throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, "");
			}

		} catch (RestServiceException e) {
			RestErrorMessage error = new RestErrorMessage(e.getStatus(),
					e.getCode(), e.getMessage());
			return Response.status(e.getStatus()).entity(error).build();
		}

		return Response.status(Response.Status.CREATED).entity(new IotUser(creation.getLogin().getUsername())).build();
		
		
	}
	
}
