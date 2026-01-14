package it.thisone.iotter.rest.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;


@ApiModel(value="RestErrorMessage",description="error message for unsuccessfull operations")
public class RestErrorMessage implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5406036329322641979L;


	@JsonProperty("status")
	public int getStatus() {
		return status;
	}

    @JsonProperty("code")
	public int getCode() {
		return code;
	}

    @JsonProperty("message")
	public String getMessage() {
		return message;
	}

    @JsonProperty("developerMessage")
	public String getDeveloperMessage() {
		return developerMessage;
	}

	
	/** contains the same HTTP Status code returned by the server */
    @JsonIgnore
	private int status;
	
	/** application specific error code */
    @JsonIgnore
	private int code;
	
	/** message describing the error*/
    @JsonIgnore
	private String message;

	/** contains stack trace */
    @JsonIgnore
	private String developerMessage;

	
	public RestErrorMessage(int status, String message){
		this.status = status;
		this.message = message;
	}

	public RestErrorMessage(int status, int code, String message) {
		super();
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public RestErrorMessage() {
		super();
	}



	public void setStatus(@JsonProperty("status") int status) {
		this.status = status;
	}


	public void setCode(@JsonProperty("code") int code) {
		this.code = code;
	}


	public void setMessage(@JsonProperty("message") String message) {
		this.message = message;
	}


	public void setDeveloperMessage(@JsonProperty("developerMessage") String developerMessage) {
		this.developerMessage = developerMessage;
	}
}