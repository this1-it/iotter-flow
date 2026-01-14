package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;

public enum Qualifier implements Internationalizable {

	ONE("One","1"), //
	AVG("Avg","2"), //
	MAX("Max","3"), //
	MIN("Min","4"), //
	TOT("Tot","5"), //
	STD("Std","6");


	private String shortName;
	private String displayName;

	private Qualifier(String displayName, String shortName) {
		this.displayName = displayName;
		this.shortName = shortName;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getShortName() {
		return shortName;
	}

	
    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

	@Override
	public String getI18nKey() {
        return "enum.modbus.qualifier." + name().toLowerCase();        
    }
}
