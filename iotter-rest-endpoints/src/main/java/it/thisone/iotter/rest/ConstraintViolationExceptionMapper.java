package it.thisone.iotter.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import it.thisone.iotter.config.Constants;
import it.thisone.iotter.rest.model.RestErrorMessage;

/*

https://www.baeldung.com/jersey-bean-validation
https://github.com/RameshMF/jersey-tutorial/wiki/Jersey-Bean-Validation-Support

*/
@Provider
public class ConstraintViolationExceptionMapper
               implements ExceptionMapper<ConstraintViolationException> {

  @Override
  public Response toResponse(final ConstraintViolationException exception) {
	  RestErrorMessage error = new RestErrorMessage(Response.Status.NOT_ACCEPTABLE.getStatusCode(), Constants.Error.GENERIC_APP_ERROR_CODE, prepareMessage(exception));
      return Response.status(Response.Status.NOT_ACCEPTABLE)
                     .entity(error)
                     .build();
  }

  private String prepareMessage(ConstraintViolationException exception) {
      String msg = "";
      for (ConstraintViolation<?> cv : exception.getConstraintViolations()) {
          msg+=" "+cv.getMessage()+";";
      }
      return msg;
  }
}