package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;


public enum Permission implements Internationalizable {
	READ("Read","R"),
	WRITE("Write","W"),
	READ_WRITE("Read and Write","RW")
	;
	


	private String shortName;
	private String displayName;

	private Permission(String displayName, String shortName) {
		this.displayName = displayName;
		this.shortName = shortName;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getI18nKey() {
        return "enum.modbus.permission." + name().toLowerCase();        
    }

    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

}
