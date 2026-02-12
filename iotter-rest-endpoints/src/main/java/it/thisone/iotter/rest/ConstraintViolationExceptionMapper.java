package it.thisone.iotter.rest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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