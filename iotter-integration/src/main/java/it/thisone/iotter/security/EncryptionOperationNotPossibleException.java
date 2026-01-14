package it.thisone.iotter.security;

public class EncryptionOperationNotPossibleException extends RuntimeException {

	public EncryptionOperationNotPossibleException(String message) {
		super(message);
	}

	public EncryptionOperationNotPossibleException() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
