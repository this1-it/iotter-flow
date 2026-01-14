package it.thisone.iotter.exceptions;


/**
 * 
 * @author tisone
 *
 */
public class BackendServiceException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4745684115887420596L;
	private int code;
	
	public BackendServiceException() {
		super();
	}
	
	public BackendServiceException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public BackendServiceException(String message) {
		super(message);
	}	
	
	public BackendServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BackendServiceException(Throwable cause) {
		super(cause);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
	
	
}
