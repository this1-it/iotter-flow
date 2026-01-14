package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum Priority implements Internationalizable {
	URGENT("Urgent","U",1), //
	HIGH("High","H",2), //
	NORMAL("Normal","N",3), //
	LOW("Low","L",5) //
	;

	private int value;
	private String shortName;
	private String displayName;

	private Priority(String displayName, String shortName, int value) {
		this.displayName = displayName;
		this.shortName = shortName;
		this.value = value;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getI18nKey() {
        return "enum.priority." + name().toLowerCase();        
    }
	
    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

	public int getValue() {
		return value;
	}

}
