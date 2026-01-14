package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum Protocol implements Internationalizable {
	FTP("Ftp"),
	HTTP("Http"),
	NATIVE("Native")
	;

	private String displayName;

	private Protocol(String value) {
		this.displayName = value;
	}

	public String getDisplayName() {
		return displayName;
	}
	
    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

	@Override
	public String getI18nKey() {
        return "enum.protocol." + name().toLowerCase();        
    }


}
