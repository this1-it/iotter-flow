package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum FtpType implements Internationalizable {
	DISABLE(0),
	LOCAL(1),
	REMOTE(2);

	@SuppressWarnings("unused")
	private int value;

	@Override
	public String getI18nKey() {
        return "enum.ftpaccess." + name().toLowerCase();        
    }

	private FtpType(int value) {
		this.value = value;
	}

}
