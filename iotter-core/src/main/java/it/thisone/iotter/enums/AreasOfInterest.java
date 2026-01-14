package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum AreasOfInterest implements Internationalizable {
	
	COMMERCIAL(0), //
	GOVERNMENT(1), //
	PERSONAL(2), //
	EDUCATION(3); //

	@SuppressWarnings("unused")
	private int value;

	private AreasOfInterest(int value) {
		this.value = value;
	}

	@Override
	public String getI18nKey() {
        return "enum.areasofinterest." + name().toLowerCase();        
    }
}
