package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;

public enum TemplateState implements Internationalizable {
	DISABLED("Disabled"),
	DRAFT("Draft"),
	PUBLIC("Public");


	private String displayName;

	private TemplateState(String value) {
		this.displayName = value;
	}

    @Override public String toString() { return displayName; }

	
	@Override
	public String getI18nKey() {
        return "enum.modbus.templatestate." + name().toLowerCase();        
    }


}
