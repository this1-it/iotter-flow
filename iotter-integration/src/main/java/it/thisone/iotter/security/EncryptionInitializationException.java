package it.thisone.iotter.security;

public class EncryptionInitializationException extends RuntimeException  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EncryptionInitializationException() {
		super();	
	}

	public EncryptionInitializationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public EncryptionInitializationException(String arg0) {
		super(arg0);
	}

	public EncryptionInitializationException(Throwable arg0) {
		super(arg0);
	}

}
