package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum ExportFileMode implements Internationalizable {
	SINGLE,
	MULTI;

	@Override
	public String getI18nKey() {
        return "enum.exportfilemode." + name().toLowerCase();        
    }

}
