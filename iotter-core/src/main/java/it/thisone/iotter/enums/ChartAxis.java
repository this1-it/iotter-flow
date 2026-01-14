package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum ChartAxis implements Internationalizable {
	X("x"), //
	Y("y"), //
	Z("z"); //

	private String value;

	@Override
	public String getI18nKey() {
        return "enum.graphaxis." + name().toLowerCase();        
    }

	
	@Override
	public String toString() {
		return value;
	}
	
	private ChartAxis(String value) {
		this.value = value;
	}

}
