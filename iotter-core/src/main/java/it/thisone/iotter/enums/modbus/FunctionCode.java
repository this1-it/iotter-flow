package it.thisone.iotter.enums.modbus;

import it.thisone.iotter.common.Internationalizable;

/*
Il modbus prevede dei codici funzione che identificano il tipo di operazione (lettura/scrittura e singola/multipla). Gli slave prevedono solo alcune di questi valori. Per esempio alcuni slave prevedono la lettura singola e non multipla quindi quando si configura dei registri per la lettura il router deve sapere se per i registri contigui si può usare una lettura multipla o singola.
FC3 (Read Holding single
FC4 (non sono sicuro) default
FC5 (Write Single Coil)
FC6 (Write Single Holding register)
F16 (Write multiple Register)


 */
public enum FunctionCode implements Internationalizable {
	SINGLE("Single","S"),
	MULTIPLE("Multiple","M"),
	;
	


	private String shortName;
	private String displayName;

	private FunctionCode(String displayName, String shortName) {
		this.displayName = displayName;
		this.shortName = shortName;
	}

	public String getDisplayName() {
		return displayName;
	}
	public String getShortName() {
		return shortName;
	}

	
    // Optionally and/or additionally, toString.
    @Override public String toString() { return displayName; }

	@Override
	public String getI18nKey() {
        return "enum.modbus.functioncode." + name().toLowerCase();        
    }
    
}
