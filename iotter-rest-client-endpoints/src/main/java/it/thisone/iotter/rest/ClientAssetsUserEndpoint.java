package it.thisone.iotter.rest;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.backend.ClientUserService;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.persistence.model.Network;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.IotLogin;
import it.thisone.iotter.rest.model.client.IotUser;
import it.thisone.iotter.rest.model.client.IotUserSet;
import it.thisone.iotter.util.Utils;

@Path("/v1/client/assets/user")
@Api(value = "/v1/client/assets/user", tags = { "v1-client-assets-user" }, hidden=ClientEndpoint.HIDDEN)
@Component
public class ClientAssetsUserEndpoint extends ClientEndpoint {

	private static Logger logger = LoggerFactory.getLogger(ClientAssetsUserEndpoint.class);
	@Autowired
	private ClientUserService clientUserService;

	@DELETE
	@Path("/reset")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "account-reset", value = "client resets itself", notes = "all owned devices must be disabled with a factory reset,"
			+ "login and assets will be removed ")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful user removal"),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response accountReset(
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey,
			@ApiParam(value = "user identification", required = true, name = "entity") @RequestBody @Valid IotUser entity) {
		User user = null;
		try {
			user = validateUserToken(apiKey);
			validateReset(user, entity);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		try {
			if (user.hasRole(Constants.ROLE_SUPERUSER)) {
				clientUserService.superUserRemoval(entity.getUsername());
			} else {
				clientUserService.userRemoval(entity.getUsername());
			}
		} catch (BackendServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
					Constants.Error.USER_NOT_AUTHORIZED, e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		return Response.status(Response.Status.OK).build();
	}

	@PUT
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "update-user", value = "client updates details of its managed user", notes = "")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "successfull details updated"),
			@ApiResponse(code = 401, message = "unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 404, message = "not found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "invalid configuration", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "internal server error") })
	public Response userUpdate(
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey,
			@ApiParam(value = "user details", required = true, name = "entity") @RequestBody @Valid IotUser request) {

		try {
			validateUserToken(apiKey);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		User entity = userService.findByName(request.getUsername());
		if (entity == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_NOT_FOUND, "user not found");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		entity.setFirstName(request.getDetails().getFirstName());
		entity.setLastName(request.getDetails().getLastName());
		entity.setPhone(request.getDetails().getPhone());
		entity.setCountry(request.getDetails().getCountry());
		userService.update(entity);
		return Response.status(Response.Status.OK).build();
	}

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "create-user", value = "client creates managed user", notes = //
	"a user will be created with role USER, user will be associated to client default network if network is available")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfull user login created"),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "Invalid User", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response userCreate(
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey,
			@ApiParam(value = "new user login", required = true, name = "entity") @RequestBody @Valid IotLogin request) {

		Network network = null;
		String owner = null;
		try {
			User operator = validateUserToken(apiKey);
			validateSuperUser(operator);
			network = operator.getNetwork();
			owner = operator.getOwner();
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		if (owner == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_INVALID_INPUT_ERROR_CODE, "user cannot be associated to an administrator");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		if (network == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_INVALID_INPUT_ERROR_CODE, "user cannot be associated to a network");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}



		if (!Utils.isValidEmailAddress(request.getUsername())) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_INVALID_INPUT_ERROR_CODE, "username not an email");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		try {
			String email = request.getUsername();
			User entity = new User();
			entity.setUsername(request.getUsername());
			entity.setEmail(email);
			entity.setPassword(request.getPassword());
			clientUserService.userRegistration(entity, network);
		} catch (BackendServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/exists")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "exists-login", value = "client checks if login exists", notes = //
	NOT_YET_IMPLEMENTED, response = IotLogin.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful list retrieving", response = IotUser.class),
			@ApiResponse(code = 404, message = "Not Found", response = RestErrorMessage.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response exists(
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey,
			@QueryParam(value = "username") String username) {

		try {
			User operator = validateUserToken(apiKey);
			validateSuperUser(operator);
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		User entity = userService.findByName(username);
		if (entity == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
					Constants.Error.USER_NOT_FOUND, "user not found");
			return Response.status(Response.Status.NOT_FOUND).entity(error).build();
		}

		IotUser user = new IotUser();
		user.setUsername(username);

		return Response.status(Response.Status.OK).entity(user).build();
	}

	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "list-users", value = "client lists its managed networks", notes = //
	NOT_YET_IMPLEMENTED, response = IotUserSet.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful list retrieving", response = IotUserSet.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response users(
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey) {
		RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
				Constants.Error.USER_NOT_AUTHORIZED, NOT_YET_IMPLEMENTED);
		return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
	}
	
	@DELETE
	@Path("/auto-reset/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "account-reset", value = "client resets itself", notes = "all owned devices must be disabled with a factory reset,"
			+ "login and assets will be removed ")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful user removal"),
			@ApiResponse(code = 401, message = "Unauthorized", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response autoReset(
			@ApiParam(name = "api-key", value = "authorization token", required = true) @HeaderParam("api-key") String apiKey,
			@ApiParam(value = "user identification", required = true, name = "username") @PathParam("username") String username) {
		User user = null;
		try {
			user = validateUserToken(apiKey);
			validateReset(user, new IotUser(username));
		} catch (RestServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(e.getStatus(), e.getCode(), e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		try {
			if (user.hasRole(Constants.ROLE_SUPERUSER)) {
				clientUserService.superUserRemoval(username);
			} else {
				clientUserService.userRemoval(username);
			}
		} catch (BackendServiceException e) {
			logger.error(e.getMessage());
			RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
					Constants.Error.USER_NOT_AUTHORIZED, e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}
		return Response.status(Response.Status.OK).build();
	}

	

}
