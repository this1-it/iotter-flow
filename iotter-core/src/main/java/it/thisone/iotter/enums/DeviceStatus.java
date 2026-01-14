package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;



public enum DeviceStatus  implements Internationalizable {
	PRODUCED(0), // lo strumento viene creato con questo stato
	VERIFIED(1), // lo strumento ha comunicato la config anche se è in stato prodotto
	ACTIVATED(2), // viene assegnato il proprietario attraverso la procedura web
	CONNECTED(3), // lo strumento ha comunicato dati e ha una configurazione valida 
	DEACTIVATED(4), // lo strumento viene inibito dalla scrittura dati
	ERROR(5); // lo strumento non ha comunicato dati secondo il rate dichiarato

	@SuppressWarnings("unused")
	private int value;

	@Override
	public String getI18nKey() {
        return "enum.devicestatus." + name().toLowerCase();        
    }

	private DeviceStatus(int value) {
		this.value = value;
	}

}
