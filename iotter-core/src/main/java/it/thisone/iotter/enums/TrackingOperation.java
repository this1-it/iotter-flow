package it.thisone.iotter.enums;


public enum TrackingOperation 
{
	CHANGE_PASSWORD("password change"), //
	ACTIVATION("activation"), //
	REGISTRATION("registration");
	
	private String displayName;
	private TrackingOperation(String value) {
		this.displayName = value;
	}
    @Override public String toString() { return displayName; }

}
