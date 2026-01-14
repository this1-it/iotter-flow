package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;

/*
Lunghezza del registro (numero di bit)

8bit
16bit (default)
32bit
float

*/
public enum Format implements Internationalizable {
	BIT8("8 bit","8"),
	BIT16("16 bit","16"),
	BIT32("32 bit","32"),
	FLOAT("Float","F")
	;
	

	private String shortName;
	private String displayName;

	private Format(String displayName, String shortName) {
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
        return "enum.modbus.format." + name().toLowerCase();        
    }

    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

}
