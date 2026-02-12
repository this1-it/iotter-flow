package it.thisone.iotter.rest;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.thisone.iotter.cassandra.model.SessionAuthentication;
import it.thisone.iotter.config.Constants;
import it.thisone.iotter.enums.AccountStatus;
import it.thisone.iotter.enums.TracingAction;
import it.thisone.iotter.enums.TrackingOperation;
import it.thisone.iotter.exceptions.BackendServiceException;
import it.thisone.iotter.integration.AuthManager;
import it.thisone.iotter.integration.NotificationService;
import it.thisone.iotter.persistence.model.Device;
import it.thisone.iotter.persistence.model.User;
import it.thisone.iotter.persistence.model.UserToken;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.rest.model.RestServiceException;
import it.thisone.iotter.rest.model.client.IotChangePassword;
import it.thisone.iotter.rest.model.client.IotLogin;
import it.thisone.iotter.rest.model.client.IotSuperUserRegistration;
import it.thisone.iotter.rest.model.client.IotTicket;
import it.thisone.iotter.rest.model.client.IotToken;
import it.thisone.iotter.rest.model.client.IotUser;
import it.thisone.iotter.rest.model.client.IotUserActivation;
import it.thisone.iotter.security.UserDetailsAdapter;
import it.thisone.iotter.util.Utils;

@Path("/v1/client/user")
@Api(value = "/v1/client/user", tags = { "v1-client-user" })
@Component
public class ClientUserEndpoint extends ClientEndpoint {
	private static Logger logger = LoggerFactory.getLogger(ClientUserEndpoint.class);

	@Autowired
	
	private ObjectMapper mapper;

	@Autowired
	private TracingService tracingService;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private AuthManager authManager;

	@Path("/login")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(nickname = "login", value = "login", notes = "user authentication / authorization", response = IotToken.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfull user authentication", response = IotToken.class),
			@ApiResponse(code = 401, message = "User not found", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response login(
			@ApiParam(value = "user credential", required = true, name = "login") @RequestBody @Valid IotLogin login) {
		IotToken authorized = new IotToken();
		try {
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(login.getUsername(),
					login.getPassword());
			Authentication result = authManager.authenticate(auth);
			UserDetailsAdapter user = (UserDetailsAdapter) result.getPrincipal();
			String role = user.getRoles().iterator().next();
			User entity = (User) result.getDetails();
			authorized = authorize(entity, role);
			tracingService.trace(TracingAction.LOGIN, entity.getUsername(), entity.getOwner(), null, null,
					"/v1/client/user/login");
		} catch (Throwable e) {
			tracingService.trace(TracingAction.LOGIN_FAILED, login.getUsername(), null, null, null,
					"/v1/client/user/login");
			RestErrorMessage error = new RestErrorMessage(Response.Status.UNAUTHORIZED.getStatusCode(),
					Constants.Error.USER_NOT_AUTHORIZED, e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
		}

		return Response.status(Response.Status.OK).entity(authorized).build();
	}

	@Path("/register")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(hidden=ClientEndpoint.HIDDEN, nickname = "register", value = "Registration", notes = "User Registration", response = IotTicket.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Successful user creation", response = IotTicket.class),
			@ApiResponse(code = 406, message = "Invalid registration", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response register(
			@ApiParam(value = "registration data", required = true, name = "registration") @RequestBody @Valid IotSuperUserRegistration registration) {
		IotTicket ticket = new IotTicket();
		try {
			validate(registration);
			ticket = superUserRegistration(registration);
		} catch (RestServiceException e) {
			String json = null;
			try {
				json = mapper.writeValueAsString(registration);
			} catch (Exception ex) {
				// ignoring exception
			}
			String message = String.format("%s %s", "/v1/client/user/register", e.getMessage());
			tracingService.traceRestError(e.toString(), null, registration.getUser().getUsername(),
					registration.getDevice().getSerial(), message, json);
			logger.error(message, e);

			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(), e.getCode(),
					e.getMessage());
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}
		return Response.status(Response.Status.CREATED).entity(ticket).build();
	}

	@Path("/activation")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(hidden=ClientEndpoint.HIDDEN, nickname = "activation", value = "activation", notes = "User Activation", response = IotTicket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfull user activation", response = IotTicket.class),
			@ApiResponse(code = 404, message = "User Not Found", response = RestErrorMessage.class),
			@ApiResponse(code = 406, message = "Invalid Activation Token", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response activation(
			@ApiParam(value = "activation data", required = true, name = "activation") @RequestBody @Valid IotUserActivation activation) {

		String action = TrackingOperation.ACTIVATION.name();
		IotTicket ticket = new IotTicket();
		ticket.setOperation(action);

		boolean valid = userService.validateToken( //
				activation.getUsername(), //
				action, //
				activation.getToken());
		if (!valid) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_USER_TOKEN, "invalid/expired activation PIN");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		userService.deleteToken(activation.getUsername(), action);
		User user = userService.changeAccountStatus(activation.getUsername(), AccountStatus.ACTIVE);

		if (user == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_NOT_FOUND, "user not activated");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		return Response.status(Response.Status.OK).entity(ticket).build();

	}

	@Path("/forgot_password")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(hidden=ClientEndpoint.HIDDEN, nickname = "forgot-password", value = "forgot password", notes = "a token will be created to change password", response = IotTicket.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Successful token creation", response = IotTicket.class),
			@ApiResponse(code = 406, message = "Invalid user", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response forgotPassword(
			@ApiParam(value = "user identifier", required = true, name = "user") @RequestBody @Valid IotUser entity) {

		User user = userService.findByName(entity.getUsername());

		if (user == null) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_NOT_FOUND, "user not found");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		if (!user.getAccountStatus().equals(AccountStatus.ACTIVE)) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_NOT_AUTHORIZED, "user not active");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		String action = TrackingOperation.CHANGE_PASSWORD.name();
		String login = user.getUsername();
		String email = user.getEmail();
		String displayName = user.getDisplayName();

		String token = RandomStringUtils.random(4, false, true);
		UserToken tkn = userService.createUserToken(login, action, token, 1);
		notificationService.resetPassword(email, null, displayName, login, null, tkn.getToken());

		IotTicket ticket = new IotTicket();
		ticket.setId(tkn.getToken());
		ticket.setExpires(tkn.getExpiryDate().getTime() / 1000);
		ticket.setOperation(action);
		return Response.status(Response.Status.CREATED).entity(ticket).build();

	}

	@Path("/change_password")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(hidden=ClientEndpoint.HIDDEN, nickname = "change-password", value = "change password", notes = "change user password", response = IotTicket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfull password change", response = IotTicket.class),
			@ApiResponse(code = 406, message = "Invalid password token", response = RestErrorMessage.class),
			@ApiResponse(code = 500, message = "Internal server error") })
	public Response changePassword(
			@ApiParam(value = "change user password", required = true, name = "token") @RequestBody @Valid IotChangePassword request) {

		String action = TrackingOperation.CHANGE_PASSWORD.name();
		IotTicket ticket = new IotTicket();
		ticket.setOperation(action);

		boolean valid = userService.validateToken( //
				request.getUsername(), //
				action, //
				request.getToken());
		if (!valid) {
			RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.INVALID_USER_TOKEN, "invalid PIN");
			return Response.status(Response.Status.NOT_ACCEPTABLE).entity(error).build();
		}

		userService.deleteToken(request.getUsername(), action);
		userService.changePassword(request.getUsername(), request.getPassword());

		return Response.status(Response.Status.OK).entity(ticket).build();

	}

	private IotTicket superUserRegistration(IotSuperUserRegistration registration) throws RestServiceException {
		if (!Utils.isValidEmailAddress(registration.getUser().getUsername())) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(),
					Constants.Error.USER_INVALID_INPUT_ERROR_CODE, "username not a valid email");
		}

		User user = new User();
		user.setUsername(registration.getUser().getUsername());
		user.setEmail(registration.getUser().getUsername());
		user.setPassword(registration.getUser().getPassword());

		String login = user.getUsername();
		String email = user.getEmail();
		String displayName = user.getDisplayName();

		if (registration.getUserDetails() != null) {
			user.setFirstName(registration.getUserDetails().getFirstName());
			user.setLastName(registration.getUserDetails().getLastName());
			user.setPhone(registration.getUserDetails().getPhone());
			user.setCountry(registration.getUserDetails().getCountry());
			displayName = user.getDisplayName();
		}

		try {
			clientUserService.superUserRegistration(user, registration.getTenant(), registration.getDevice(),
					registration.getDeviceDetails());
		} catch (BackendServiceException e) {
			throw new RestServiceException(Response.Status.NOT_ACCEPTABLE.getStatusCode(), e.getCode(), e.getMessage());
		}

		String token = RandomStringUtils.random(4, false, true);
		UserToken tkn = userService.createUserToken(login, TrackingOperation.ACTIVATION.name(), token, 1);
		notificationService.registration(email, null, displayName, login, null, tkn.getToken());

		IotTicket ticket = new IotTicket();
		ticket.setId(tkn.getToken());
		// ticket.setExpires(tkn.getExpiryDate().getTime() / 1000);
		ticket.setOperation(TrackingOperation.ACTIVATION.name());
		return ticket;
	}

	private IotToken authorize(User user, String role) {
		IotToken authorized = new IotToken();
		SessionAuthentication tkn = cassandraAuth.findAuthenticationToken(user.getUsername());
		int ttl = Constants.USER_TOKEN_HOURS * 60 * 60;
		if (tkn == null) {
			tkn = cassandraAuth.createAuthenticationToken(user.getUsername(), role, ttl);
		} else {
			ttl = (int) (tkn.getExpiryDate().getTime() - System.currentTimeMillis()) / 1000;
		}
		authorized.setToken(tkn.getToken());
		authorized.setExpires(tkn.getExpiryDate().getTime() / 1000);
		authorized.setRole(tkn.getRole());
		if (!role.equalsIgnoreCase(Constants.ROLE_SUPERVISOR)) {
			clientUserService.grantAuthorizations(user, tkn, ttl);
		}
		return authorized;
	}


	


}
