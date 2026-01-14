package it.thisone.iotter.rest.model;

import io.swagger.annotations.ApiModel;

@ApiModel(value="RestServiceException",description="RestServiceException")
public class RestServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final int status;
	
	private final int code;
	

	public RestServiceException(int status, int code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}


	public int getStatus() {
		return status;
	}


	public int getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		return String.format("%d%s%s", getStatus(), getCode(), getMessage());
	}



}
