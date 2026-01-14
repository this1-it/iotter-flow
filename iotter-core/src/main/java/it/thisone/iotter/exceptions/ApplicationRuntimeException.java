package it.thisone.iotter.exceptions;

/**
 * a runtime exception is not recoverable
 * @author tisone
 *
 */
public class ApplicationRuntimeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4745684115887420596L;
	
	public ApplicationRuntimeException() {
		super();
	}
	
	public ApplicationRuntimeException(String message) {
		super(message);
	}
	
	public ApplicationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ApplicationRuntimeException(Throwable cause) {
		super(cause);
	}
	
}
