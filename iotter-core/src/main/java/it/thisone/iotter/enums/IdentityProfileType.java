package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum IdentityProfileType implements Internationalizable {
	PRIVATE(0),
	COMPANY(1);
	
	@SuppressWarnings("unused")
	private int value;

	@Override
	public String getI18nKey() {
        return "enum.identityProfileType." + name().toLowerCase();        
    }

	private IdentityProfileType(int value) {
		this.value = value;
	}

}
