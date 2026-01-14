package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

/*
 * CODE PROCESSSING 
 * "sampleone" =1 //
 * "sampleavg" =2 //
 * "samplemax" =3 //
 * "samplemin" =4 //
 * "sampletot" =5 //
 * "samplestd" =6 //
 * "samplemax instant" =7 //
 * "samplemin instant" =8 //
 * "samplealmhl" =9 //
 * "ETo_S"= 10 // Not coded 
 * "ETo_T"= 11 // Not coded 
 * "ETo_F"= 12 // Not coded
 */

public enum MeasureQualifier implements Internationalizable {
	ONE(1), //
	AVG(2), //
	MAX(3), //
	MIN(4), //
	TOT(5), //
	STD(6), //
	INSTANT_MAX(7), //
	INSTANT_MIN(8), //
	ALM(9); //

	private final int value;

	private MeasureQualifier(int value) {
		this.value = value;
	}

	@Override
	public String getI18nKey() {
        return "enum.measurequalifier." + name().toLowerCase();        
    }

	public int getValue() {
		return value;
	}
}
