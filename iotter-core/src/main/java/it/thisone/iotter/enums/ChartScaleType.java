package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;



public enum ChartScaleType implements Internationalizable {

    LINEAR("linear"), LOGARITHMIC("logarithmic");
 
    private String type;

    private ChartScaleType(String type) {
        this.type = type;
    }

    @Override
	public String toString() {
        return type;
    }
	

	@Override
	public String getI18nKey() {
        return "enum.graphscale." + name().toLowerCase();        
    }

}
