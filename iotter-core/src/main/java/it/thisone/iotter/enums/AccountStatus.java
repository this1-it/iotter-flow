package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;


public enum AccountStatus implements Internationalizable {
	ACTIVE(0), //
	NEED_ACTIVATION(1), //
	EXPIRED(2), //
	SUSPENDED(3), //
	LOCKED(4), //
	HIDDEN(5); //

	@SuppressWarnings("unused")
	private int value;

	private AccountStatus(int value) {
		this.value = value;
	}

	@Override
	public String getI18nKey() {
        return "enum.accountstatus." + name().toLowerCase();        
    }
}
