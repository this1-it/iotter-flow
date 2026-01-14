package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum Order implements Internationalizable {
	ASCENDING("Ascending"), //
	DESCENDING("Descending"); //

	private String value;

	@Override
	public String getI18nKey() {
        return "enum.order." + name().toLowerCase();        
    }

	
	@Override
	public String toString() {
		return value;
	}
	
	private Order(String value) {
		this.value = value;
	}

}
