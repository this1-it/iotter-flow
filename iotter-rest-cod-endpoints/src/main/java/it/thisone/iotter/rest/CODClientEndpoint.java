package it.thisone.iotter.rest;

import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.cassandra.CassandraConfortOnDemand;
import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.config.CODConfig;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.CODUserCreation;
import it.thisone.iotter.rest.model.client.ConfortOnDemandEvent;
import it.thisone.iotter.rest.model.client.IotNetwork;
import it.thisone.iotter.rest.model.client.IotUser;

@Path("/v1/client/cod")
@Api(value = "/v1/client/cod", tags = { "v1-client-cod" }, hidden=true)
@Component
public class CODClientEndpoint extends ClientEndpoint {
	@Autowired
	private ConfortOnDemandService service;

	
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
			@RequestBody @Valid CODUserCreation creation //
	) {
		try {
			SessionAuthentication session = validateSessionToken(apiKey);
			if (session.getRole().equals(Constants.ROLE_USER)) {
				throw new RestServiceException(Response.Status.UNAUTHORIZED.getStatusCode(), Constants.Error.USER_NOT_AUTHORIZED, "");
			}
			try {
				clientUserService.createUser(creation.getLogin(), creation.getUserDetails(), new IotNetwork(CODConfig.NETWORK_NAME,CODConfig.ADMIN_NAME));
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


	@Path("/event")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "createEvent", value = "create Event", notes = "Confort On Demand Event Creation")
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Successful Event creation"),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "Invalid Event", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response createEvent( //
			@ApiParam(name = "api-key", value = "authorization token", required = true) //
			@HeaderParam("api-key") String apiKey, //
			@ApiParam(value = "event data", required = true, name = "event") //
			@RequestBody @Valid ConfortOnDemandEvent event //
	) {

		try {
			SessionAuthentication session = validateSessionToken(apiKey);
			if (event.getTimestamp() == null) {
				event.setTimestamp(new Date());
			}
			event.setUserid(session.getUsername());
			service.process(event);

		} catch (RestServiceException e) {
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(e.getStatus()).entity(error).build();
		}

//		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//		Validator validator = factory.getValidator();
//		Set<ConstraintViolation<ConfortOnDemandEvent>> errors = validator.validate(event);

		return Response.status(Response.Status.CREATED).build();

	}
	
	@GET
	@Path("/aggregate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response aggregate(
			@HeaderParam("api-key") String apiKey, //
			@QueryParam(value = "sn") String sn, //
			@QueryParam(value = "beacon") String beacon //
			) {

		try {
			validateSessionToken(apiKey);
//			List<ConfortOnDemandEvent> items = cassandra.select(sn, beacon);
//
//			cassandra.aggregate(sn, beacon);

		} catch (RestServiceException e) {
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		return Response.status(Response.Status.OK).build();
	}
	


}
