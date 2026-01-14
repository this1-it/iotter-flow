package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum NetworkType implements Internationalizable {
	GEOGRAPHIC(0),CUSTOM(1),LIST(2);

	@SuppressWarnings("unused")
	private int value;

	@Override
	public String getI18nKey() {
        return "enum.networktype." + name().toLowerCase();        
    }

	private NetworkType(int value) {
		this.value = value;
	}

}
