package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;

/*

Questo è tipico del modbus e descrive la tipologia del registro (ridondante in parte con Type Var). Le prime due tipologie sono Digital mentre le altre due Analog

Discrete Input
Coil
Input
Holding (Default)

 */
public enum TypeRead implements Internationalizable {
	DISCRETE_INPUT("Discrete Input","D"), //
	COIL("Coil","C"), //
	INPUT("Input","I"), //
	HOLDING("Holding","H") //
	; //

	private String shortName;
	private String displayName;

	private TypeRead(String displayName, String shortName) {
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
        return "enum.modbus.typeread." + name().toLowerCase();        
    }
	
    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

}
