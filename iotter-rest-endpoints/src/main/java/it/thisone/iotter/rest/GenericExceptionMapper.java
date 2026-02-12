package it.thisone.iotter.rest;

import java.net.SocketTimeoutException;

import javax.persistence.RollbackException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.persistence.service.TracingService;
import it.thisone.iotter.rest.model.RestErrorMessage;
import it.thisone.iotter.util.Utils;

@Provider
@Component
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

	private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);
	
	private static final String UNEXPECTED_EOF = "Unexpected EOF";
	private static final String CONNECTION_RESET_BY_PEER = "Connection reset by peer";
	private static final int IGNORE_ERROR_CODE = 5000;
	
	@Autowired
	TracingService tracingService;

	@Override
	public Response toResponse(Throwable ex) {
		RestErrorMessage errorMessage = new RestErrorMessage();
		setHttpStatus(ex, errorMessage);
		setErrorCode(ex, errorMessage);
		
		if (errorMessage.getCode() != IGNORE_ERROR_CODE) {
			String trace = Utils.logStackTrace(ex);
			if (! trace.toLowerCase().contains("timeout")) {
				//logger.error("unchecked rest error {} {}", ex.getMessage(), trace);
				tracingService.traceRestError(ex.getMessage(), null, null, null, trace, null);
			}
		}
		return Response.status(errorMessage.getStatus()).entity(errorMessage).type(MediaType.APPLICATION_JSON).build();
	}

	private void setErrorCode(Throwable ex, RestErrorMessage errorMessage) {
		//logger.error("rest error cause", ex.getCause());
		if (ex.getCause() != null && ex.getCause() instanceof SocketTimeoutException) {
			errorMessage.setCode(IGNORE_ERROR_CODE);
			return;
		}
		if (ex.getCause() != null && ex.getCause() instanceof RollbackException) {
			errorMessage.setCode(IGNORE_ERROR_CODE);
			return;
		}
		if (ex.getMessage() != null && ex.getMessage().contains(CONNECTION_RESET_BY_PEER)) {
			errorMessage.setCode(IGNORE_ERROR_CODE);
			return;
		}		
		if (ex.getMessage() != null && ex.getMessage().contains(UNEXPECTED_EOF)) {
			errorMessage.setCode(IGNORE_ERROR_CODE);
			return;
		}
		if (ex.getMessage() == null) {
			errorMessage.setCode(IGNORE_ERROR_CODE);
			return;
		}
		errorMessage.setCode(Constants.Error.GENERIC_APP_ERROR_CODE);
	}

	
	private void setHttpStatus(Throwable ex, RestErrorMessage errorMessage) {
		if (ex instanceof WebApplicationException) {
			errorMessage.setStatus(((WebApplicationException) ex).getResponse().getStatus());
		} else {
			// default to error 500
			errorMessage.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}
}