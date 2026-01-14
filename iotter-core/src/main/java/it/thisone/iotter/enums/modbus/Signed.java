package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;

/*
 * E' la tipologia del parametro. 
 * E' una informazione ridontante ma chiarificatrice per Aermec. La sola sa che viene automatizzata dal router è che per le grandezze Digital viene imposta l'unita di misura a boolean.

 */
public enum Signed implements Internationalizable {
	YES("Yes","Y"),
	NO("No","N");

	private String shortName;
	private String displayName;

	private Signed(String displayName, String shortName) {
		this.displayName = displayName;
		this.shortName = shortName;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getI18nKey() {
        return "enum.modbus.typevar." + name().toLowerCase();        
    }

    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

}
