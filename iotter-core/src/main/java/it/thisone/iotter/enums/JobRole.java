package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum JobRole implements Internationalizable {
	SOLE_PROPRIETORSHIP(0),
	SYSTEM_INTEGRATOR(1),
	SOLUTION_BUILDER(2);

	@SuppressWarnings("unused")
	private int value;

	@Override
	public String getI18nKey() {
        return "enum.jobrole." + name().toLowerCase();        
    }

	private JobRole(int value) {
		this.value = value;
	}

}
