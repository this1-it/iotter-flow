package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;


public enum AlarmStatus implements Internationalizable {
	FIRE_UP("Fire Up"), //
	FIRE_DOWN("Fire Down"), //
	REENTER("Reenter"), //
	ONLINE("Online"), //
	OFFLINE("Offline"), //
	RESET("Reset"), // used only for initialization
	UNDEFINED("Undefined"), // used only for initialization
	CHANGED("Changed"), // used only for initialization
	ON("ON"), // used only for notifications
	OFF("OFF"); //

	private String displayName;

	private AlarmStatus(String value) {
		this.displayName = value;
	}

	@Override
	public String getI18nKey() {
        return "enum.alarmstatus." + name().toLowerCase();        
    }
	
    @Override public String toString() { return displayName; }
	
}
