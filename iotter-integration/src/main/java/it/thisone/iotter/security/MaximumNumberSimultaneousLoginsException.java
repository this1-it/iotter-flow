package it.thisone.iotter.security;

import org.springframework.security.core.AuthenticationException;

public class MaximumNumberSimultaneousLoginsException extends
		AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576238441074244254L;

	public MaximumNumberSimultaneousLoginsException(String msg) {
		super(msg);
	}

}
